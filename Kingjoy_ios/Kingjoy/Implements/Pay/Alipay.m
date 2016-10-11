//
//  Alipay.m
//  tbGameDemo
//
//  Created by Apple on 15/4/22.
//  Copyright (c) 2015年 tongbu.com. All rights reserved.
//

#import "Alipay.h"

@implementation Alipay

//调用支付宝支付
+(void) pay:(double)amount productName:(NSString *)productName customInfo:(NSString *)customInfo orderInfo:(NSDictionary *)orderInfo delege:(NSObject<KJPayCallback> *)delege dialog:(WebViewJsCaller*) dialog{
    //1.调用kingjoy生成订单
    
    NSString* orderId = [orderInfo objectForKey:@"orderId"];
    NSString* partner = [orderInfo objectForKey:@"partner"];
    NSString* seller = [orderInfo objectForKey:@"seller"];
    NSString* privateKey = [orderInfo objectForKey:@"privateKey"];
    NSString* notifyURL = [orderInfo objectForKey:@"returnUrl"];
    
    //2.
    /*============================================================================*/
    /*=======================需要填写商户app申请的===================================*/
    /*============================================================================
    *partner = @"2088911385045325";
    *seller = @"2013kingjoy@gmail.com";
    *privateKey = @"MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANUzwHCeXaM4jQRE0QEd8uhtWCsxUHJuBr6PM5Ue3ABr8ivIrzADbWwWejoGGgUHnDtIZOohgZwfAticPXAMkHvL++tND646gwpyVlthle6zwNe2YMYQWCjihZewy/ZkYEXKWwJ4sZH8hPFEL/fx5L+D8+cpL29BpyU5Y7KzSeMfAgMBAAECgYEAk7dSivS+jdsdoSqI4Mw+Q5mW7/4v7cibtaYEKDqEVWp1FK7r6klFpBECyHb3yipWraBgAvQKGABIeYnu61ZZN5kE/W8EfQMUjsP8fywQ7//QFurtYOPsmIIG/QmjPr0VtnYwxZQZy+p6X9vd+4I9gyP3MBJRJbrgqPJ/PEw6cuECQQDq8e4C08fLcLXLCNOx0I5/URljLU3q8RK6pYdzrc1vt/q3XsjYxMNWt1GrPDJacmaLZgOneouALDCryJxiIfcPAkEA6E8AF4tJhbix0oWAjqAMAoeCOwYqCXbTOvwjyZWdFsC6/4zTwoix6ldsSo7kDUjEEkRQXbDDMO8GDtS90PrS8QJAcGMmjees7V6PN/6S9b5kS/cJg+3KFhcX9Hz2XJnJaiXCIzGONlN5dePfh2AWXjnXX9t+6ACw9rEs3x/p3u+MdQJAHVi1g0PmQs1FMXoJ9nqDaeJickBiGRX4sy/c+X87+/7W4KeSrLWF/DuVA4ovhvoVQtV8bYmB7vGTC1aWsAqvkQJAcKGiHy0RPvcB67Okwm9JxV+sZjmW8gTolsgfOHo71gxe18Hth+5Xdzvk3WvvBoJ6HgWBTc/yTQCke4WDCAy8hg==";
     @"http://113.107.150.196:6005/ps/joyany.do"
     */
    /*============================================================================*/
    /*============================================================================*/
    /*============================================================================*/
    
    Order *order = [[Order alloc] init];
    order.partner = partner;
    order.seller = seller;
    //order.tradeNO = [self generateTradeNO]; //订单ID(由商家□自□行制定)
    
    //待添加订单id
    order.tradeNO = orderId;
    order.productName = productName; //商品标题
    order.productDescription = productName; //商品描述
    order.amount = [NSString stringWithFormat:@"%.2f",amount]; //商 品价格
    order.notifyURL = notifyURL; //回调URL
    order.service = @"mobile.securitypay.pay";
    order.paymentType = @"1";
    order.inputCharset = @"utf-8";
    order.itBPay = @"30m";
    //应用注册scheme,在AlixPayDemo-Info.plist定义URL types
    NSString *appScheme = @"kingjoyPay";
    //将商品信息拼接成字符串
    NSString *orderSpec = [order description];
    NSLog(@"orderSpec = %@",orderSpec);
    //获取私钥并将商户信息签名,外部商户可以根据情况存放私钥和签名,只需要遵循 RSA 签名规范, 并将签名字符串 base64 编码和 UrlEncode
    id<DataSigner> signer = CreateRSADataSigner(privateKey);
    NSString *signedString = [signer signString:orderSpec];
    //将签名成功字符串格式化为订单字符串,请严格按照该格式
    NSString *orderString = nil;
    if (signedString != nil) {
        orderString = [NSString stringWithFormat:@"%@&sign=\"%@\"&sign_type=\"%@\"",
                       orderSpec, signedString, @"RSA"];

        [[AlipaySDK defaultService] payOrder:orderString fromScheme:appScheme callback:^(NSDictionary *resultDic) {
            NSLog(@"reslut = %@",resultDic);
            
            [self handleResult:resultDic delege:delege dialog:dialog];
        }];
    }
    //3.
}

+ (void) handleResult:(NSDictionary*)rs delege:(NSObject<KJPayCallback>*)delege dialog:(WebViewJsCaller*)dialog{
    int const PAY_SUCCESS = 9000;
    switch ([[rs objectForKey:@"resultStatus"] integerValue ]) {
        case PAY_SUCCESS:
            [dialog showToast:@"支付成功"];
            [delege payFinished:KJBuySuccess];
            break;
        default:{
            [dialog showToast:@"支付失败"];
            [delege payFinished:KJBuyFail];
            break;
        }
            
    }
}

#pragma mark   ==============产生随机订单号==============

//1.调用服务端生成订单号
+ (NSString *)generateTradeNO
{
    static int kNumber = 15;
    
    NSString *sourceStr = @"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    NSMutableString *resultStr = [[NSMutableString alloc] init];
    srand(time(0));
    for (int i = 0; i < kNumber; i++)
    {
        unsigned index = rand() % [sourceStr length];
        NSString *oneStr = [sourceStr substringWithRange:NSMakeRange(index, 1)];
        [resultStr appendString:oneStr];
    }
    return resultStr;
}


//解析json得到Dictionary
+ (NSDictionary *) parseJson : (NSString*) str{
    NSError *jsonError;
    NSData *objectData = [str dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *json = [NSJSONSerialization JSONObjectWithData:objectData
                                                         options:NSJSONReadingMutableContainers
                                                           error:&jsonError];
    return json;
}

@end
