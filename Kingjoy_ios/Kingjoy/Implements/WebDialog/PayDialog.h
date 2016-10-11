//
//  PayDialog.h
//  tbGameDemo
//
//  Created by Apple on 15/4/22.
//  Copyright (c) 2015年 tongbu.com. All rights reserved.
//

#import "WebViewJsCaller.h"
#import "Alipay.h"
#import "WeixinPay.h"
#import "UniPay.h"

@interface PayDialog : WebViewJsCaller

@property NSObject<KJPayCallback>* delege;

/**
 支付:
 gameOrderId:商品金额
 money:金额
 productName:商品名称
 customInfo:透传参数,客户端自定义参数
 unitPrice:商品单价
 count:商品数量
 */
- (void)pay:(double)amount unitPrice:(double)price count:(int)count productName:(NSString*)productName customInfo:(NSString*)customInfo gameOrderId:(NSString*)gameOrderId delege:(NSObject<KJPayCallback>*)delege;


/**
 支付:
 gameOrderId:商品金额
 amount:金额
 productName:商品名称
 customInfo:透传参数,客户端自定义参数
 */
- (void)pay:(double)amount productName:(NSString*)productName customInfo:(NSString*)customInfo gameOrderId:(NSString*)gameOrderId delege:(NSObject<KJPayCallback>*)delege;


@end
