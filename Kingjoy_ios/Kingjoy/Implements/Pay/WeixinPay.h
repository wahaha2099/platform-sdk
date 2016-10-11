//
//  Alipay.h
//
//  Created by Apple on 15/4/22.
//  Copyright (c) 2015年 kingjoy.cn All rights reserved.
//

#import <Foundation/Foundation.h>

#import "KingjoySDK.h"
#import "Order.h"
#import "DataSigner.h"
#import "PayDialog.h"
#import "WXApi.h"

@interface WeixinPay : NSObject

//调用微信支付
+(void) pay:(double)amount productName:(NSString *)productName customInfo:(NSString *)customInfo orderInfo:(NSDictionary *)orderInfo delege:(NSObject<KJPayCallback> *)delege dialog:(WebViewJsCaller*) dialog;

/*! @brief 收到一个来自微信的请求，第三方应用程序处理完后调用sendResp向微信发送结果
 *
 * 收到一个来自微信的请求，异步处理完成后必须调用sendResp发送处理结果给微信。
 * 可能收到的请求有GetMessageFromWXReq、ShowMessageFromWXReq等。
 * @param req 具体请求内容，是自动释放的
 */
+(void) onReq:(BaseReq*)req ;



/*! @brief 发送一个sendReq后，收到微信的回应
 *
 * 收到一个来自微信的处理结果。调用一次sendReq后会收到onResp。
 * 可能收到的处理结果有SendMessageToWXResp、SendAuthResp等。
 * @param resp具体的回应内容，是自动释放的
 */
+(void) onResp:(BaseResp*)resp delege:(NSObject<KJPayCallback>*)delege dialog:(WebViewJsCaller*)dialog;

@end
