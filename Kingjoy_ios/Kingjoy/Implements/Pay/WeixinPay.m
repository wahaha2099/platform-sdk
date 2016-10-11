//
//  Alipay.m
//  tbGameDemo
//
//  Created by Apple on 15/4/22.
//  Copyright (c) 2015年 kingjoy.cn. All rights reserved.
//

#import "WeixinPay.h"

//测试引入的h文件，要删除
#import "payRequsestHandler.h"
//测试引入的h文件，要删除

#define APP_ID          @"wxddd995ea7646419f"               //APPID
//商户号，填写商户对应参数
#define MCH_ID          @"1240525502"
//获取服务器端支付数据地址（商户自定义）
//#define SP_URL          @"http://wxpay.weixin.qq.com/pub_v2/app/app_pay.php"


//!!!!!!!!!!以下数据待删除!!!!!!!!!!!!!!!
//#define APP_SECRET      @"b3ccbadc91617a86e0bb2ff21a847d52" //appsecret
//商户API密钥，填写相应参数
#define PARTNER_ID      @"wxddd995ea7646419f1240525502wxdb"
//支付结果回调页面
//#define NOTIFY_URL      @"http://wxpay.weixin.qq.com/pub_v2/pay/notify.v2.php"
//!!!!!!!!!!以上数据待删除!!!!!!!!!!!!!!!

@implementation WeixinPay

//调用支付宝支付
+(void) pay:(double)amount productName:(NSString *)productName customInfo:(NSString *)customInfo orderInfo:(NSDictionary *)dict delege:(NSObject<KJPayCallback> *)delege dialog:(WebViewJsCaller*) dialog{
    
    //向微信注册
    [WXApi registerApp:APP_ID withDescription:@"KingjoyPay"];
    
    //[WeixinPay sendPay_demo];
    //return ;
    
    NSMutableString *stamp  = [dict objectForKey:@"timeStamp"];
    
    //调起微信支付
    PayReq* req             = [[PayReq alloc] init];
    req.openID              = APP_ID;
    req.partnerId           = MCH_ID;
    req.prepayId            = [dict objectForKey:@"prepay_id"];
    req.nonceStr            = [dict objectForKey:@"nonce_str"];
    req.timeStamp           = stamp.intValue;
    req.package             = @"Sign=WXPay";
    req.sign                = [dict objectForKey:@"sign"];
    [WXApi sendReq:req];
    //日志输出
    NSLog(@"appid=%@\npartid=%@\nprepayid=%@\nnoncestr=%@\ntimestamp=%ld\npackage=%@\nsign=%@",req.openID,req.partnerId,req.prepayId,req.nonceStr,(long)req.timeStamp,req.package,req.sign );
}

+ (void)sendPay_demo
{
    //{{{
    //本实例只是演示签名过程， 请将该过程在商户服务器上实现
    
    //创建支付签名对象
    payRequsestHandler *req = [payRequsestHandler alloc];
    //初始化支付签名对象
    [req init:APP_ID mch_id:MCH_ID];
    //设置密钥
    [req setKey:PARTNER_ID];
    
    //}}}
    
    //获取到实际调起微信支付的参数后，在app端调起支付
    NSMutableDictionary *dict = [req sendPay_demo];
    
    if(dict == nil){
        //错误提示
        NSString *debug = [req getDebugifo];
        
        //[self alert:@"提示信息" msg:debug];
        
        NSLog(@"%@\n\n",debug);
    }else{
        NSLog(@"%@\n\n",[req getDebugifo]);
        //[self alert:@"确认" msg:@"下单成功，点击OK后调起支付！"];
        
        NSMutableString *stamp  = [dict objectForKey:@"timestamp"];
        
        //调起微信支付
        PayReq* req             = [[PayReq alloc] init];
        req.openID              = [dict objectForKey:@"appid"];
        req.partnerId           = [dict objectForKey:@"partnerid"];
        req.prepayId            = [dict objectForKey:@"prepayid"];
        req.nonceStr            = [dict objectForKey:@"noncestr"];
        req.timeStamp           = stamp.intValue;
        req.package             = [dict objectForKey:@"package"];
        req.sign                = [dict objectForKey:@"sign"];
        
        [WXApi sendReq:req];
    }
}

/*
 * 微信请求回调,这里是支付请求回调
 * 发送一个sendReq后，收到微信的回应
 *
 * 收到一个来自微信的处理结果。调用一次sendReq后会收到onResp。
 * 可能收到的处理结果有SendMessageToWXResp、SendAuthResp等。
 * @param resp具体的回应内容，是自动释放的
 */
+(void) onResp:(BaseResp*)resp delege:(NSObject<KJPayCallback>*)delege dialog:(WebViewJsCaller*)dialog
{
    //NSString *strMsg = [NSString stringWithFormat:@"errcode:%d", resp.errCode];
    NSString *strTitle;
    
    if([resp isKindOfClass:[PayResp class]]){
        //支付返回结果，实际支付结果需要去微信服务器端查询
        strTitle = [NSString stringWithFormat:@"支付结果"];
        
        switch (resp.errCode) {
            case WXSuccess:

                //strMsg = @"支付结果：成功！";
                NSLog(@"支付成功－PaySuccess，retcode = %d", resp.errCode);
                
                [dialog showToast:@"支付成功"];
                [delege payFinished:KJBuySuccess];
        
                break;
                
            default:
                //strMsg = [NSString stringWithFormat:@"支付结果：失败！retcode = %d, retstr = %@", resp.errCode,resp.errStr];
                NSLog(@"错误，retcode = %d, retstr = %@", resp.errCode,resp.errStr);
                
                [dialog showToast:@"支付失败"];
                [delege payFinished:KJBuyFail];
                
                break;
        }
    }
    //UIAlertView *alert = [[UIAlertView alloc] initWithTitle:strTitle message:strMsg delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil, nil];
    //[alert show];
}


/*! @brief 收到一个来自微信的请求，第三方应用程序处理完后调用sendResp向微信发送结果
 *
 * 收到一个来自微信的请求，异步处理完成后必须调用sendResp发送处理结果给微信。
 * 可能收到的请求有GetMessageFromWXReq、ShowMessageFromWXReq等。
 * @param req 具体请求内容，是自动释放的
 */
+(void) onReq:(BaseReq*)req
{
    //暂无处理
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
