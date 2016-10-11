//
//  Alipay.m
//  tbGameDemo
//
//  Created by Apple on 15/4/22.
//  Copyright (c) 2015年 tongbu.com. All rights reserved.
//

#import "UniPay.h"
#import "UPPayPluginDelegate.h"
#import "UPPayPlugin.h"

@interface UniPay()<UPPayPluginDelegate>

@end

@implementation UniPay

static WebViewJsCaller * st_dialog;
static NSObject<KJPayCallback> * st_delege;
//系统默认的orientation
static UIInterfaceOrientation orientation;

//调用银联支付
+(void) pay:(double)amount productName:(NSString *)productName customInfo:(NSString *)customInfo orderInfo:(NSDictionary *)orderInfo delege:(NSObject<KJPayCallback> *)delege dialog:(WebViewJsCaller*) dialog{
    //1.调用kingjoy生成订单
    
    NSString* tn = [orderInfo objectForKey:@"orderId"];
    NSString* mode = @"01";//01=test mode , 00=正式环境
    NSLog(@"tn=%@",tn);
    
    st_dialog = dialog;
    st_delege = delege;
    
    UIViewController * win = [[UIApplication sharedApplication] keyWindow].rootViewController;
    
    //设置竖屏
    [UniPay setOrientationPortrait:YES];
    
    //设置支付
    [UPPayPlugin startPay:tn mode:mode viewController:win delegate:UniPay.self];

     //[self handleResult:resultDic delege:delege dialog:dialog];
}


#pragma mark UPPayPluginResult
-(void)UPPayPluginResult:(NSString*)result{
    //此私有方法不会回调,注册的是static的类方法,这里这么写只是让xcode不告警
    [UniPay UPPayPluginResult:result];
}

+(void)UPPayPluginResult:(NSString*)result
{
    [self setOrientationPortrait:NO];
    NSLog(@"支付结果：%@", result);
    if ([result isEqualToString:@"success"]) {
        NSLog(@"success");
        [st_dialog showToast:@"支付成功"];
        [st_delege payFinished:KJBuySuccess];
    }else if([result isEqualToString:@"fail"]){
        NSLog(@"fail");
        [st_dialog showToast:@"支付失败"];
        [st_delege payFinished:KJBuyFail];
    }else if([result isEqualToString:@"cancel"]){
        NSLog(@"cancel");
    }
}


#pragma mark   ==============解析json==============

//解析json得到Dictionary
+ (NSDictionary *) parseJson : (NSString*) str{
    NSError *jsonError;
    NSData *objectData = [str dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *json = [NSJSONSerialization JSONObjectWithData:objectData
                                                         options:NSJSONReadingMutableContainers
                                                           error:&jsonError];
    return json;
}

//设置竖屏
+(void)setOrientationPortrait:(bool)portrait{
    if(orientation == UIDeviceOrientationUnknown){
        orientation = [[UIApplication sharedApplication] statusBarOrientation];
    }
    UIInterfaceOrientation tochange = orientation;
    if(portrait){
        tochange = UIInterfaceOrientationPortrait;
        
    }
    
    bool IsIOS8 = NO;
    float version = [[[UIDevice currentDevice] systemVersion] floatValue];
    if (version >= 8.0)
    {
        IsIOS8 = YES;
    }
        //改设备方向
    if (IsIOS8) {
        [[UIDevice currentDevice] setValue:[NSNumber numberWithInteger:tochange] forKey:@"orientation"];
    }
    [[UIApplication sharedApplication] setStatusBarOrientation:tochange animated:NO];
}

@end
