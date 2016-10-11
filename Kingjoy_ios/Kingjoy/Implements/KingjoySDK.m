//
//  KingjoySDK.m
//
//  Created by Apple on 15/4/22.
//  Copyright (c) 2015年 kingjoy. All rights reserved.
//

#import "KingjoySDK.h"
#import "LoginDialog.h"
#import "JoinDialog.h"
//#import "PayDialog.h"
#import "DataHolder.h"
#import "SelectDialog.h"
//#import "WeixinPay.h"
//#import "WXApi.h"
#import "FloatMenu.h"
#import "IOSDevicesInfo.h"
#import "ReadMe.h"

@interface KingjoySDK()


@end

@implementation KingjoySDK

static KingjoySetting * setting;
static LoginDialog * loginDialog;
static JoinDialog * joinDialog;
//static PayDialog * payDialog;
static KJUserInfo * userInfo;
static SelectDialog * selectDialog;
static ReadMe * readMeDialog;

static NSObject<KJPayCallback> * payDelege;

static NSObject<KJLoginCallback> * loginDelege;

/**
 获取登录用户信息*/
+ (KJUserInfo*) loginUser{
    return userInfo;
}

//登录成功后,设置登录用户
+(void) setLoginUser:(KJUserInfo * )user{
    userInfo = user;
    
    //登陆后再显示悬浮按钮
    [[FloatMenu Instance ]createFloatMenu];
    
}

//初始化SDK
+ (void)initAppId:(NSString *)appid appkey:(NSString *)appkey channel:(NSString *)channel{
    
    setting = [[KingjoySetting alloc]init];
    [setting setAppid:appid];
    [setting setAppkey:appkey];
    [setting setChannel:channel];
    [setting setBaseUrl:@"http://pay.kingjoy.cn/platform-web/"];
    //[setting setBaseUrl:@"http://192.168.1.34:8080/platform-web/"];
    
    loginDialog = [LoginDialog initSetting:setting];
    joinDialog = [JoinDialog initSetting:setting];
    //payDialog = [PayDialog initSetting:setting];
    selectDialog = [SelectDialog initSetting:setting];
    readMeDialog = [ReadMe initSetting:setting ];
    
    //keep the instance
    //[[FloatMenu Instance ]createFloatMenu];
}

//返回Kingjoy setting
+(KingjoySetting * ) getKingjoySetting{
    return setting;
}

//打开readme
+(void)showReadMe{
    [readMeDialog showReadMe];
    [KingjoySDK hideOther:readMeDialog];
}

//打开登录
+(void)login:(NSObject<KJLoginCallback> *)delege{
    
    if(delege != nil )loginDelege = delege;
    
    if(delege == nil ) delege = loginDelege;
    
    NSMutableArray * dic = [[DataHolder sharedInstance] loadAccount];
    
    if(dic != nil && !selectDialog.changeOtherAccount){
        [selectDialog showSelect:delege];
        [KingjoySDK hideOther:selectDialog];
    }else{
        [loginDialog showLogin:delege];
        [KingjoySDK hideOther:loginDialog];
    }
}

//打开注册面板
+(void)registe:(NSObject<KJLoginCallback> *)delege{
    
    if(delege != nil )loginDelege = delege;
    
    if(delege == nil ) delege = loginDelege;
    
    [joinDialog showJoin:delege];
    [KingjoySDK hideOther:joinDialog];
}

//隐藏其他dialog
+(void) hideOther:(WebViewJsCaller *) dialog{
    if(dialog != loginDialog)[loginDialog hideView];
    if(dialog != joinDialog)[joinDialog hideView];
    //if(dialog != payDialog)[payDialog hideView];
    if(dialog != selectDialog)[selectDialog hideView];
    if(dialog != readMeDialog )[readMeDialog hideView];
}

/**
 支付:
 gameOrderId:商品金额
 amount:金额
 productName:商品名称
 customInfo:透传参数,客户端自定义参数
 */
+ (void)pay:(double)amount productName:(NSString*)productName customInfo:(NSString*)customInfo gameOrderId:(NSString*)gameOrderId delege:(NSObject<KJPayCallback>*)delege{
    payDelege = delege;
    //[KingjoySDK hideOther:payDialog];
    //[payDialog pay:amount productName:productName customInfo:customInfo gameOrderId:gameOrderId delege:delege];
}

/**
 支付:
 gameOrderId:商品金额
 money:金额
 productName:商品名称
 customInfo:透传参数,客户端自定义参数
 unitPrice:商品单价
 count:商品数量
 */
+ (void)pay:(double)amount unitPrice:(double)price count:(int)count productName:(NSString*)productName customInfo:(NSString*)customInfo gameOrderId:(NSString*)gameOrderId delege:(NSObject<KJPayCallback>*)delege{
    payDelege = delege;
    //[KingjoySDK hideOther:payDialog];
    //[payDialog pay:amount unitPrice:price count:count productName:productName customInfo:customInfo gameOrderId:gameOrderId delege:delege];
}




/**
 微信支付回调,需要在AppDelegate.h中调用以下方法
 - (BOOL)application:(UIApplication *)application handleOpenURL:(NSURL *)url{
 return  [KingjoySDK handleOpenURL:url];
 }
 - (BOOL)application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation
 {
 return  [KingjoySDK handleOpenURL:url ];
 }
 */
+(BOOL) handleOpenURL:(NSURL *)url{
    NSLog(@" weixin handleOpenURL run ");
    //return [WXApi handleOpenURL:url delegate:KingjoySDK.self];
    return true ;
}

//给url上报信息,附带上request信息
+(void)UploadDeviceInfo:(NSMutableDictionary *) request url:(NSString*) url{
    [IOSDevicesInfo UploadDeviceInfo:request url:url];
}

@end

