//
//  WebViewJsSuper.m
// Dialog的父类，不设置
//
//  Created by Apple on 15/4/29.
//  Copyright (c) 2015年 Kingjoy. All rights reserved.
//

#import "WebViewJsSuper.h"
@implementation WebViewJsSuper

//读取远程页面默认显示loading
MBProgressHUD *loadinTip;

//初始化参数
+ (instancetype)initSetting:(KingjoySetting *)setting{
    WebViewJsSuper* bridge = [[[self class] alloc] init];
    bridge.setting = setting;
    return bridge;
}

- (void)afterWebViewDidFinishLoad {
    
    [self hideLoading];
    
    NSMutableDictionary *dictionary = [[NSMutableDictionary alloc] init];
    [dictionary setValue:[[self setting] appid] forKey:@"appid"];
    [dictionary setValue:[[self setting] appkey] forKey:@"appkey"];
    [dictionary setValue:[[self setting] channel] forKey:@"channel"];
    [dictionary setValue:[[self setting] baseUrl] forKey:@"serAddress"];
    
    [self setContextToPage:dictionary];
    
    NSError *error = nil;
    //转成JSON
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dictionary
                                                       options:kNilOptions
                                                         error:&error];
    if (error)
    {
        NSLog(@"dic->%@",error);
    }
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    
    NSString *callStr = [NSString stringWithFormat:@"setContent('%@');",jsonString];
    
    [self excuteJSWithObj:nil function:callStr ] ;
}

//调用页面setContext,添加参数
- (void) setContextToPage:(NSDictionary*)content{
    
}

//根据html生成webView
- (UIWebView*)createWebView :(NSString*) htmlName  width:(int)width height:(int)height {
    //htmlFile = [wwwBundle pathForResource:@"ios" ofType:@"html" ];
    UIWebView* viewTemp = [[UIWebView alloc] init];

    if ( [htmlName hasPrefix:@"http" ]) {//外部网页
        NSURL* nsUrl = [NSURL URLWithString:htmlName];
        
        [self showLoading];
        
        NSURLRequest* request = [NSURLRequest requestWithURL:nsUrl cachePolicy:NSURLRequestReloadIgnoringLocalAndRemoteCacheData timeoutInterval:30];
        
        [viewTemp loadRequest:request];

    }else{//内部网页
        //1.获取资源
        NSString *mainBundlePath = [[NSBundle mainBundle] resourcePath];
        NSString *wwwBundlePath = [mainBundlePath stringByAppendingPathComponent:@"kingjoy.bundle"];//sdk.bundle
        NSBundle *wwwBundle = [NSBundle bundleWithPath:wwwBundlePath];
        NSURL *baseURL = [NSURL fileURLWithPath:wwwBundlePath];
        
        //2.查找登录的页面
        NSString *htmlFile = [wwwBundle pathForResource:htmlName ofType:@"html" ];
        
        NSString *content = [NSString stringWithContentsOfFile:htmlFile encoding:NSUTF8StringEncoding error:nil];
        [viewTemp loadHTMLString:content baseURL:baseURL ];
    }
    
    //3.定义webview的宽度，高度，等属性
    CGRect screen = [[UIScreen mainScreen] bounds];
    
    //设置初始化大小
    viewTemp.frame = CGRectMake((screen.size.width - width) /2, (screen.size.height - height) /2, width, height);
    viewTemp.scalesPageToFit = NO;
    viewTemp.scrollView.scrollEnabled = NO;
    viewTemp.scrollView.bounces = NO;
    //设置透明
    [viewTemp setBackgroundColor:[UIColor clearColor]];
    viewTemp.opaque=NO;
    
    //4.添加到主界面
    //[[[UIApplication sharedApplication] keyWindow] addSubview:viewTemp];
    UIView * view = [[[UIApplication sharedApplication] windows] firstObject];//这里用lastObject的话,会选择悬浮的按钮
    [view addSubview:viewTemp];
    
    //5.初始化JSBridge
    [self _platformSpecificSetup:viewTemp webViewDelegate:nil resourceBundle:nil];
    
    self.htmlView = viewTemp;


    
    return viewTemp;
}

/** 根据url生成webView **/
- (UIWebView*)createByUrl :(NSString*) url frame:(CGRect) frame {
    
    NSURL* nsUrl = [NSURL URLWithString:url];
    
    NSURLRequest* request = [NSURLRequest requestWithURL:nsUrl cachePolicy:NSURLRequestReloadIgnoringLocalAndRemoteCacheData timeoutInterval:30];
    
    UIWebView* viewTemp = [[UIWebView alloc] init];
    [viewTemp loadRequest:request];
    
    //3.定义webview的宽度，高度，等属性
    //CGRect screen = [[UIScreen mainScreen] bounds];
    
    //设置初始化大小
    viewTemp.frame = frame;
    viewTemp.scalesPageToFit = NO;
    //viewTemp.scrollView.scrollEnabled = NO;
    //viewTemp.scrollView.bounces = NO;
    //设置透明
    [viewTemp setBackgroundColor:[UIColor clearColor]];
    viewTemp.opaque=NO;
    
    //4.添加到主界面
    //[[[UIApplication sharedApplication] keyWindow] addSubview:viewTemp];
    //UIView * view = [[[UIApplication sharedApplication] windows] firstObject];//这里用lastObject的话,会选择悬浮的按钮
    //[view addSubview:viewTemp];
    
    //5.初始化JSBridge
    [self _platformSpecificSetup:viewTemp webViewDelegate:nil resourceBundle:nil];
    
    self.htmlView = viewTemp;
    
    return viewTemp;
}


//解析json得到Dictionary
- (NSDictionary *) parseJson : (NSString*) str{
    NSError *jsonError;
    NSData *objectData = [str dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *json = [NSJSONSerialization JSONObjectWithData:objectData
                                                         options:NSJSONReadingMutableContainers
                                                           error:&jsonError];
    
    return json;
}

//显示提示
-(void)showToast:(NSString *)msg{
    
    UIView * view = [[[UIApplication sharedApplication] windows] firstObject];//这里用lastObject的话,会选择悬浮的按钮
    MBProgressHUD *hud = [MBProgressHUD showHUDAddedTo:view animated:YES];
    
    // Configure for text only and offset down
    hud.mode = MBProgressHUDModeText;
    hud.labelText = msg;
    hud.margin = 10.f;
    hud.yOffset = 10.f;
    hud.removeFromSuperViewOnHide = YES;
    
    [hud hide:YES afterDelay:1.5];
}

//加载远程页面,显示loading
-(void)showLoading{
    if(loadinTip == nil ){
        UIView * view = [[[UIApplication sharedApplication] windows] firstObject];//这里用lastObject的话,会选择悬浮的按钮
        loadinTip= [MBProgressHUD showHUDAddedTo:view animated:YES];
        //[hud hide:YES afterDelay:1.5];
    }
    MBProgressHUD *hud = loadinTip;
    // Configure for text only and offset down
    hud.mode = MBProgressHUDModeText;
    hud.labelText = @"加载中 ... ";
    hud.margin = 10.f;
    hud.yOffset = 10.f;
    hud.removeFromSuperViewOnHide = YES;
}

//加载远程页面,关闭loading
-(void)hideLoading{
    [loadinTip hide:YES];
    loadinTip = nil;
}

//隐藏htmlView
- (void) hideView{
    [self.htmlView setHidden:true];
    self.timeWebViewDidFinish = 0;
    /*
    [self.htmlView removeFromSuperview];
    self.htmlView = nil;
    self.webView = nil;*/
}

//设置整个屏幕
-(void)SetFullScreen{
    
    UIWebView * view = self.htmlView;
    CGRect screen = [[UIScreen mainScreen] bounds];
    int width = screen.size.width ;//定义界面宽度
    int height = screen.size.height ;//定义界面高度
    
    //设置初始化大小
    view.frame = CGRectMake((screen.size.width - width) /2, (screen.size.height - height) /2, width, height);
    
    //NSLog(@" full screen 1: %ld , %ld" , (long)width , (long)height);
    //NSLog(@" full screen 2: %ld , %ld" , (long)view.frame.size.height , (long)view.frame.size.width);

    //设置全屏幕的不用设置透明
    view.scrollView.scrollEnabled = YES;
    [view setBackgroundColor:[UIColor whiteColor]];
    view.opaque=YES;
}

//获取一个大小适中的面板
-(void)SetFixScreen {
    
    UIWebView * view = self.htmlView;
    
    CGRect screen = [[UIScreen mainScreen] bounds];
    int width = screen.size.width ;//定义界面宽度
    int height = screen.size.height ;//定义界面高度
    
    int newWidth = width * 0.8 ;
    int newHeight = height * 0.8 ;
    
    if( width < 380 ) newWidth = 320;//iphone 6尺寸
    if( width > 500 ) newWidth = 370;//ipad尺寸?
    
    if( newHeight < 300 ) newHeight = 300;
    if( newHeight > 350 ) newHeight = 350;
    
    //设置初始化大小
    view.frame = CGRectMake((screen.size.width - width) /2, (screen.size.height - height) /2, newWidth, newHeight);
}



@end
