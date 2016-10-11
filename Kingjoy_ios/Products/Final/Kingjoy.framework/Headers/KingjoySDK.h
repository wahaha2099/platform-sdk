//
//  KingjoySDK.h
//
//  Created by Apple on 15/4/22.
//  Copyright (c) 2015年 tongbu.com. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "KingjoyDefines.h"

@interface KingjoySDK : NSObject

//获取kingjoy setting
+(KingjoySetting * ) getKingjoySetting;

//给url上报信息,附带上request信息
+(void)UploadDeviceInfo:(NSMutableDictionary *) request url:(NSString*) url;

//初始化平台
+ (void) initAppId:(NSString * )appid appkey:(NSString *)appkey channel:(NSString*)channel ;

/**
 * 登录，打开登录面板
 */
+ (void) login:(NSObject<KJLoginCallback>*)delege;


/** 打开注册面板 */
+ (void)registe:(NSObject<KJLoginCallback> *)delege;

/** 打开readme*/
+(void)showReadMe;

/**
 获取登录用户信息*/
+ (KJUserInfo*) loginUser;

//设置登录用户
+(void) setLoginUser:(KJUserInfo * )user;

/**
 *  判断玩家是否已经登录平台
- (BOOL)TBIsLogined; */

/**
 支付:
 gameOrderId:商品金额
 amount:金额
 productName:商品名称
 customInfo:透传参数,客户端自定义参数
 */
+ (void)pay:(double)amount productName:(NSString*)productName customInfo:(NSString*)customInfo gameOrderId:(NSString*)gameOrderId delege:(NSObject<KJPayCallback>*)delege;

/**
 支付:
 gameOrderId:商品金额
 money:金额
 productName:商品名称
 customInfo:透传参数,客户端自定义参数
 unitPrice:商品单价
 count:商品数量
 */
+ (void)pay:(double)amount unitPrice:(double)price count:(int)count productName:(NSString*)productName customInfo:(NSString*)customInfo gameOrderId:(NSString*)gameOrderId delege:(NSObject<KJPayCallback>*)delege;

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
+(BOOL) handleOpenURL:(NSURL *)url;

@end

