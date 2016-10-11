//
//  LoginDialog.m
//  登录面板
//  Created by Apple on 15/4/21.
//  Copyright (c) 2015年 kingjoy. All rights reserved.
//

#import "LoginDialog.h"
#import "DataHolder.h"
#import "FloatMenu.h"

@implementation LoginDialog

NSInteger const CLOSE = 1 ;
NSInteger const GO_REGISTE = 2;
NSInteger const GO_FAST_REGISTE = 3;
NSInteger const AJAX_CALLBACK = 4;
NSInteger const SHOW_README = 5;

/*打开login dialog
*/
- (void)showLogin:(NSObject<KJLoginCallback>*)delege  {
    
    int width = 320 ;//定义界面宽度
    int height = 300 ;//定义界面高度

    NSString * url = [NSString stringWithFormat:@"%@/sdk/login.html" , [[self setting]baseUrl]];
    [self createWebView:url width:width height:height];
    [self SetFixScreen];
    
    _delege = delege;
}

//子类实现具体业务逻辑
- (void) handle:(NSDictionary *) json {
    
    //NSLog(@" handle in login dialog %@" , json);
    int method = (int)[json[@"method"] integerValue];
    
    switch (method) {
        case CLOSE:{
            [KingjoySDK login:_delege];
            break ;
        }
        case GO_REGISTE:{//跳转去注册
            [KingjoySDK registe:_delege];
            [self hideView];
            break;
        }
        case GO_FAST_REGISTE:{//一键注册
            [self handleLogin:json];
            break;
        }
        case AJAX_CALLBACK:{//前端ajax请求结束
            [self handleLogin:json];
            break;
        }
        case SHOW_README:{
            [KingjoySDK showReadMe];
            break;
        }
        default:{
            //NSLog(@"not implements yet");
            break;
        }
    }

}

//处理登录回调
- (void) handleLogin:(NSDictionary *) json{
    //1.检查返回结果是否ok
    NSDictionary * rs = [self parseJson:json[@"args"]];
    NSString* code = [rs objectForKey:@"code"];
    if(code!=nil && [code compare:@"0000"] == NSOrderedSame){
        //2.成功获取用户信息
        NSDictionary * user = [rs objectForKey:@"returnObj"];
        NSString* userId = [user objectForKey:@"userId"];
        NSString* name = [user objectForKey:@"userName"];
        NSString* token = [user objectForKey:@"token"];
        //NSLog(@"login token = %@ , name = %@ , id =%@" , token , name ,userId);
        
        //3.登录回调
        /**/
        KJUserInfo *userInfo = [[ KJUserInfo alloc] init ];
        [userInfo setUserID:userId];
        [userInfo setSessionID:token];
        [userInfo setNickName:name];
        [self showToast:[NSString stringWithFormat:@"%@,登录成功" , name]];
        [[DataHolder sharedInstance] saveAccount:user];
        [KingjoySDK setLoginUser:userInfo];
        [_delege loginFinished:userInfo];
        
        //3.隐藏面板
        [self hideView];
    }else{
        [self showToast:[NSString stringWithFormat:@"登录失败，%@" , [rs objectForKey:@"message"]]];
        //NSLog(@"call error %@" , [rs objectForKey:@"message"]);
    }
}

@end


/*
 
 //1.获取资源
 NSString *mainBundlePath = [[NSBundle mainBundle] resourcePath];
 NSString *wwwBundlePath = [mainBundlePath stringByAppendingPathComponent:@"sdk.bundle"];
 NSBundle *wwwBundle = [NSBundle bundleWithPath:wwwBundlePath];
 NSURL *baseURL = [NSURL fileURLWithPath:wwwBundlePath];
 
 //2.查找登录的页面
 NSString *htmlFile = [wwwBundle pathForResource:@"login" ofType:@"html" ];
 
 //htmlFile = [wwwBundle pathForResource:@"ios" ofType:@"html" ];
 UIWebView* webView = [[UIWebView alloc] init];
 
 NSString *content = [NSString stringWithContentsOfFile:htmlFile encoding:NSUTF8StringEncoding error:nil];
 [webView loadHTMLString:content baseURL:baseURL ];
 
 //3.定义webview的宽度，高度，等属性
 CGRect screen = [[UIScreen mainScreen] bounds];
 int width = 300;//定义界面宽度
 int height = 300;//定义界面高度
 webView.frame = CGRectMake((screen.size.width - width) /2, (screen.size.height - height) /2, width, height);
 webView.scalesPageToFit = NO;
 webView.scrollView.scrollEnabled = NO;
 webView.scrollView.bounces = NO;
 
 LoginDialog* bridge = [LoginDialog bridgeForWebView:webView webViewDelegate:nil resourceBundle:wwwBundle];
 
 bridge.htmlView = webView;
 */

