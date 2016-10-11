//
//  WebViewJsSuper.h
//  Kingjoy
//
//  Created by Apple on 15/4/29.
//  Copyright (c) 2015年 Kingjoy. All rights reserved.
//

#import "WebViewJSBridge.h"
#import "KingjoySDK.h"
#import "MBProgressHUD.h"
#import <CoreGraphics/CoreGraphics.h>

@interface WebViewJsSuper : WebViewJsBridge

@property (nonatomic) UIWebView *htmlView;
@property (nonatomic) KingjoySetting *setting;

//根据html生成webView
- (UIWebView*)createWebView :(NSString*) htmlName  width:(int)width height:(int)height;

//隐藏view
- (void) hideView;

//解析json
- (NSDictionary *) parseJson : (NSString*) str;

//设置参数
+ (instancetype)initSetting:(KingjoySetting*) setting ;

//显示提醒
-(void) showToast:(NSString*)msg;

//调用页面setContext,添加参数
- (void) setContextToPage:(NSDictionary*)content;

/** 根据url生成webView **/
- (UIWebView*)createByUrl :(NSString*) url frame:(CGRect) frame;

//设置整个屏幕大小
-(void)SetFullScreen;

//设置一个大小适中的面板
-(void)SetFixScreen;


@end
