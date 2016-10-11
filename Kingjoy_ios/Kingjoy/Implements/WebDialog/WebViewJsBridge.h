//
//  WebViewJsBridge.h
//  VoxStudent
//
//  Created by zhaoxy on 14-3-8.
//  Copyright (c) 2014年 17zuoye. All rights reserved.
//

#import <Foundation/Foundation.h>

#define kCustomProtocolScheme @"jscall"
#define kBridgeName           @"external"

@interface WebViewJsBridge : NSObject<UIWebViewDelegate>

@property (nonatomic, weak) UIWebView *webView;

//webView finish初始化调用的次数
@property int timeWebViewDidFinish;

+ (instancetype)bridgeForWebView:(UIWebView*)webView webViewDelegate:(NSObject<UIWebViewDelegate>*)webViewDelegate;
+ (instancetype)bridgeForWebView:(UIWebView*)webView webViewDelegate:(NSObject<UIWebViewDelegate>*)webViewDelegate resourceBundle:(NSBundle*)bundle;

- (void) _platformSpecificSetup:(UIWebView*)webView webViewDelegate:(id<UIWebViewDelegate>)webViewDelegate resourceBundle:(NSBundle*)bundle;

- (void)excuteJSWithObj:(NSString *)obj function:(NSString *)function;

//webViewDidFinishLoad后执行
- (void)afterWebViewDidFinishLoad;

@end