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

@interface UniPay : NSObject

//调用支付宝支付
+(void) pay:(double)amount productName:(NSString *)productName customInfo:(NSString *)customInfo orderInfo:(NSDictionary *)orderInfo delege:(NSObject<KJPayCallback> *)delege dialog:(WebViewJsCaller*) dialog;


@end
