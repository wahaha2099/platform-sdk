//
//  WebViewJsBridge.m
//  VoxStudent
//
//  Created by zhaoxy on 14-3-8.
//  Copyright (c) 2014年 17zuoye. All rights reserved.
//

#import "WebViewJsBridge.h"
#import <objc/runtime.h>

@interface WebViewJsBridge ()

@property (nonatomic, weak) id webViewDelegate;
@property (nonatomic, weak) NSBundle *resourceBundle;

@end

@implementation WebViewJsBridge

+ (instancetype)bridgeForWebView:(UIWebView*)webView webViewDelegate:(NSObject<UIWebViewDelegate>*)webViewDelegate {
    return [self bridgeForWebView:webView webViewDelegate:webViewDelegate resourceBundle:nil];
}

+ (instancetype)bridgeForWebView:(UIWebView*)webView webViewDelegate:(NSObject<UIWebViewDelegate>*)webViewDelegate resourceBundle:(NSBundle*)bundle
{
    WebViewJsBridge* bridge = [[[self class] alloc] init];
    [bridge _platformSpecificSetup:webView webViewDelegate:webViewDelegate resourceBundle:bundle];
    return bridge;
}

#pragma mark - init & dealloc

- (void) _platformSpecificSetup:(UIWebView*)webView webViewDelegate:(id<UIWebViewDelegate>)webViewDelegate resourceBundle:(NSBundle*)bundle{
    _webView = webView;
    _webViewDelegate = webViewDelegate;
    _webView.delegate = self;
    _resourceBundle = bundle;
    
    //默认用sdk.bundle
    NSString *mainBundlePath = [[NSBundle mainBundle] resourcePath];
    NSString *wwwBundlePath = [mainBundlePath stringByAppendingPathComponent:@"kingjoy.bundle"];
    NSBundle *wwwBundle = [NSBundle bundleWithPath:wwwBundlePath];
    _resourceBundle = wwwBundle;
}

- (void)dealloc {
    _webView.delegate = nil;
    
    _webView = nil;
    _webViewDelegate = nil;
    
    //NSLog(@"dealloc is running");
}

#pragma mark - UIWebView Delegate

- (void)webViewDidFinishLoad:(UIWebView *)webView {
    if (webView != _webView) { return; }
    
    _timeWebViewDidFinish++;
    if (_timeWebViewDidFinish > 1) {
        return;
    }
    
    //is js insert
    if (![[webView stringByEvaluatingJavaScriptFromString:[NSString stringWithFormat:@"typeof window.%@ == 'object'", kBridgeName]] isEqualToString:@"true"]) {
        //get class method dynamically
        unsigned int methodCount = 0;
        Method *methods = class_copyMethodList([[self class]superclass], &methodCount);//如果这里不是继承，则不用写superclass方法
        NSMutableString *methodList = [NSMutableString string];
        for (int i=0; i<methodCount; i++) {
            NSString *methodName = [NSString stringWithCString:sel_getName(method_getName(methods[i])) encoding:NSUTF8StringEncoding];

            if ([methodName isEqualToString:@".cxx_destruct"]) {
                continue;
            }
            
            //NSLog(@" find methodName %@" , methodName);
            [methodList appendString:@"\""];
            [methodList appendString:[methodName stringByReplacingOccurrencesOfString:@":" withString:@""]];
            [methodList appendString:@"\","];
        }
        if (methodList.length>0) {
            [methodList deleteCharactersInRange:NSMakeRange(methodList.length-1, 1)];
        }
        /*
        NSBundle *bundle = _resourceBundle ? _resourceBundle : [NSBundle mainBundle];
        NSString *filePath = [bundle pathForResource:@"js/WebViewJsBridge" ofType:@"js"];
        NSString *js = [NSString stringWithContentsOfFile:filePath encoding:NSUTF8StringEncoding error:nil];
        */
        
        NSString *js = [self getWebViewJsBridgeJs];
        //NSLog(@"js init : %@" , methodList);
        [webView stringByEvaluatingJavaScriptFromString:[NSString stringWithFormat:js, methodList]];
    }
    
    __strong typeof(_webViewDelegate) strongDelegate = _webViewDelegate;
    if (strongDelegate && [strongDelegate respondsToSelector:@selector(webViewDidFinishLoad:)]) {
        [strongDelegate webViewDidFinishLoad:webView];
    }
    
    CGRect screen = [[UIScreen mainScreen] bounds];

    //NSLog(@" before load %ld , %ld" , (long)webView.frame.size.height , (long)webView.frame.size.width);
    
    //没有设置全屏的,按读取到的内容进行大小设置
    if(!(screen.size.width == webView.frame.size.width && screen.size.height == webView.frame.size.height)){
        
        //reset the width and heigh
        CGRect frame = webView.frame;
        
        frame.size.height = 1;
        webView.frame = frame;
        CGSize fittingSize = [webView sizeThatFits:CGSizeZero];
        
        if(fittingSize.height > 200){//小于的页面，默认用设置的,页面应该加载出错了
            frame.size = fittingSize;
            webView.frame = frame;
            webView.frame = CGRectMake((screen.size.width - fittingSize.width) /2, (screen.size.height - fittingSize.height) /2, frame.size.width, frame.size.height);
        }
    }
    
    //NSLog(@" after load %ld , %ld" , (long)webView.frame.size.height , (long)webView.frame.size.width);
    
    //通知子类完成此方法
    [self afterWebViewDidFinishLoad];
    
    //设置ios 7 下的横竖屏
    [self updateForCurrentOrientationAnimated:NO view:webView];
}

- (void)afterWebViewDidFinishLoad{
    //子类实现
}

- (void)webView:(UIWebView *)webView didFailLoadWithError:(NSError *)error {
    if (webView != _webView) { return; }
    
    __strong typeof(_webViewDelegate) strongDelegate = _webViewDelegate;
    if (strongDelegate && [strongDelegate respondsToSelector:@selector(webView:didFailLoadWithError:)]) {
        [strongDelegate webView:webView didFailLoadWithError:error];
    }
}

- (BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType {
    if (webView != _webView) { return YES; }
    NSURL *url = [request URL];
    __strong typeof(_webViewDelegate) strongDelegate = _webViewDelegate;
    
    NSString *requestString = [[request URL] absoluteString];
    if ([requestString hasPrefix:kCustomProtocolScheme]) {
        NSArray *components = [[url absoluteString] componentsSeparatedByString:@":"];
        
        NSString *function = (NSString*)[components objectAtIndex:1];
        NSString *argsAsString = [(NSString*)[components objectAtIndex:2]
                                  stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
        NSData *argsData = [argsAsString dataUsingEncoding:NSUTF8StringEncoding];
        NSDictionary *argsDic = (NSDictionary *)[NSJSONSerialization JSONObjectWithData:argsData options:kNilOptions error:NULL];
        //convert js array to objc array
        NSMutableArray *args = [NSMutableArray array];
        for (int i=0; i<[argsDic count]; i++) {
            [args addObject:[argsDic objectForKey:[NSString stringWithFormat:@"%d", i]]];
        }
        //ignore warning
#pragma clang diagnostic ignored "-Warc-performSelector-leaks"
        SEL selector = NSSelectorFromString([args count]>0?[function stringByAppendingString:@":"]:function);
        if ([self respondsToSelector:selector]) {
            [self performSelector:selector withObject:args];
        }
        return NO;
    } else if (strongDelegate && [strongDelegate respondsToSelector:@selector(webView:shouldStartLoadWithRequest:navigationType:)]) {
        return [strongDelegate webView:webView shouldStartLoadWithRequest:request navigationType:navigationType];
    } else {
        return YES;
    }
}

- (void)webViewDidStartLoad:(UIWebView *)webView {
    if (webView != _webView) { return; }
    
    __strong typeof(_webViewDelegate) strongDelegate = _webViewDelegate;
    if (strongDelegate && [strongDelegate respondsToSelector:@selector(webViewDidStartLoad:)]) {
        [strongDelegate webViewDidStartLoad:webView];
    }
}

#pragma mark - call js

//执行js方法
- (void)excuteJSWithObj:(NSString *)obj function:(NSString *)function {
    NSString *js = function;
    if (obj) {
        js = [NSString stringWithFormat:@"%@.%@", obj, function];
    }
    //NSLog(@"excuteJS:%@", js);
    [self.webView stringByEvaluatingJavaScriptFromString:js];
}

//返回js绑定相关的js
-(NSString *) getWebViewJsBridgeJs{
    NSString * s = @";(function(){var messagingIframe,bridge='android',CUSTOM_PROTOCOL_SCHEME='jscall';if(window[bridge]){return}function _createQueueReadyIframe(doc){messagingIframe=doc.createElement('iframe');messagingIframe.style.display='none';doc.documentElement.appendChild(messagingIframe)}window[bridge]={};var methods=[%@];for(var i=0;i<methods.length;i++){var method=methods[i];var code=\"(window[bridge])[method] = function \"+method+\"() {messagingIframe.src = CUSTOM_PROTOCOL_SCHEME + ':' + arguments.callee.name + ':' + encodeURIComponent(JSON.stringify(arguments));}\";eval(code)}_createQueueReadyIframe(document);window.android=window[bridge]})();";
    return s ;
}

- (void)updateForCurrentOrientationAnimated:(BOOL)animated view:(UIWebView * ) view {
    // Not needed on iOS 8+, compile out when the deployment target allows,
    // to avoid sharedApplication problems on extension targets
#if __IPHONE_OS_VERSION_MIN_REQUIRED < 80000
    // Only needed pre iOS 7 when added to a window
    BOOL iOS8OrLater = kCFCoreFoundationVersionNumber >= kCFCoreFoundationVersionNumber_iOS_8_0;
    if (iOS8OrLater || ![view.superview isKindOfClass:[UIWindow class]]) return;
    
    UIInterfaceOrientation orientation = [UIApplication sharedApplication].statusBarOrientation;
    CGFloat radians = 0;
    if (UIInterfaceOrientationIsLandscape(orientation)) {
        if (orientation == UIInterfaceOrientationLandscapeLeft) { radians = -(CGFloat)M_PI_2; }
        else { radians = (CGFloat)M_PI_2; }
        
        CGRect screen = [[UIScreen mainScreen] bounds];
        
        //NSLog(@" before load %ld , %ld" , (long)webView.frame.size.height , (long)webView.frame.size.width);
        
        //设置全屏的,需要重新变换位置
        if(screen.size.width == view.frame.size.width){
            // Window coordinates differ!
            view.bounds = CGRectMake(0, 0, view.bounds.size.height, view.bounds.size.width);
        }
        
    } else {
        if (orientation == UIInterfaceOrientationPortraitUpsideDown) { radians = (CGFloat)M_PI; }
        else { radians = 0; }
    }
    CGAffineTransform rotationTransform = CGAffineTransformMakeRotation(radians);
    
    if (animated) {
        [UIView beginAnimations:nil context:nil];
        [UIView setAnimationDuration:0.3];
    }
    [view setTransform:rotationTransform];
    if (animated) {
        [UIView commitAnimations];
    }
#endif
}

@end