//
//  KingjoyIOSHelper.m
//  Unity-iPhone
//
//  Created by Apple on 15/4/30.
//
//

#import "KingjoyIOSHelper.h"
#import <Kingjoy/KingjoySDK.h>

#ifndef __ZSZB_IN_UNITY__
#define __ZSZB_IN_UNITY__
#endif



#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 70000
#define IOS7_SDK_AVAILABLE 1
#endif

#if defined(__cplusplus)
extern "C" {
#endif
    extern void       UnitySendMessage(const char* obj, const char* method, const char* msg);
    extern NSString*  AppStoreCreateNSString (const char* string);
#if defined(__cplusplus)
}
#endif

static KingjoyIOSHelper* _instance = nil;

@interface KingjoyIOSHelper()<KJLoginCallback,KJPayCallback>

@property (atomic,retain)NSString* mCallBackObjectName ;//unity 的回调方法名

@end

@implementation KingjoyIOSHelper

+ (KingjoyIOSHelper *)instance
{
    @synchronized(self)
    {
        if (_instance == nil)
        {
            _instance = [[KingjoyIOSHelper alloc] init];
        }
    }
    return _instance;
}

-(void)payFinished:(KJ_BUYGOODS_RESULT *)rs{
    NSLog(@" pay finish ");
}

- (void)loginFinished:(KJUserInfo * )user{
    NSDictionary* dict = [NSDictionary dictionaryWithObjectsAndKeys:
                          @"KuaiYongType", @"type",
                          user.userID, @"id",
                          user.nickName, @"userName",
                          @"", @"password",
                          user.sessionID, @"token", nil];
    
    NSString *usrInfo = nil;
    usrInfo = [self NSDictionaryToNSString:dict];
    NSLog(@"------- usrInfo ----%@", usrInfo);
    
    NSString* callback = self.mCallBackObjectName;
    NSLog(@"callback %@ " , callback );
    
#ifdef __ZSZB_IN_UNITY__
    // 通知 unity 登陆的userinfo
    UnitySendMessage(callback.UTF8String, "SetUserInfo", usrInfo.UTF8String);
    
    UnitySendMessage(callback.UTF8String, "OnMessage", @"loginSuccess".UTF8String);
    
#endif
    
    NSLog(@" haha ok ");
}

// 将字段转换为NSString
- (NSString*)NSDictionaryToNSString:(NSDictionary *)dict
{
    NSString *param = @"";
    if (nil != dict)
    {
        for(NSString *key in dict)
        {
            //NSLog(@"-----key %@ : value %@", key, [dict valueForKey:key]);
            if([param length] == 0)
            {
                param = [param stringByAppendingFormat:@"%@=%@", key, [dict valueForKey:key]];
            }
            else
            {
                param = [param stringByAppendingFormat:@"&%@=%@", key, [dict valueForKey:key]];
            }
            //NSLog(@"------param %@", param);
        }
    }
    return param;
}

// 设置Unity回调的对象名
-(void) KingjoyInit:(NSString *) appId key:(NSString *) appKey  channelID:(NSString *) channelID callback:(NSString *)callback
{
    NSLog(@" now init in KingjoySDK ,callback %@" , callback);
    [KingjoySDK initAppId:appId appkey:appKey channel:channelID];
    self.mCallBackObjectName = callback;
}

-(void )KingjoyStartLogin{
    NSLog(@" now login in KingjoySDK");
    [KingjoySDK login:self];
}

// 开始购买商品
- (void)pay:(NSArray*)buyInfo
{
    
    double price = [[buyInfo objectAtIndex:0] doubleValue];
    NSString * orderId = [buyInfo objectAtIndex:1];
    NSString * productName = [buyInfo objectAtIndex:2];
    NSString * exInfo =  [buyInfo objectAtIndex:4];
    
    [KingjoySDK pay:price productName:productName customInfo:exInfo gameOrderId:orderId delege:self];
    
}

//初始化 设置回调的对象
- (void)initAppStorePay:(NSString*)callBackName payCallbackUrl:(NSString*)payCallbackUrl
{
    //    [KingjoySDK initAppId:@"10001" appkey:@"123456" channel:@"1"];
    //    [KingjoySDK login:self];
}


#if defined(__cplusplus)
extern "C" {
#endif
    /*
     [DllImport("__Internal")]
     private static extern void KingjoyInit(string appId, string appKey, string channelID);
     
     [DllImport("__Internal")]
     private static extern void KingjoyStartLogin();
     
     [DllImport("__Internal")]
     private static extern string KingjoyGetUserName();
     
     [DllImport("__Internal")]
     private static extern string KingjoyGetUserId();
     
     [DllImport("__Internal")]
     private static extern bool KingjoyPay(string orderId, string productName, double productPrice, string userParam);
     */
    
    NSString* AppStoreCreateNSString (const char* string)
    {
        return [NSString stringWithUTF8String:(string ? string : "")];
    }
    
    // 设置Unity回调的对象名
    void KingjoyInit(const char* appId,const char* appKey , const char* channelID , const char* callbackName)
    {
        [[KingjoyIOSHelper instance] KingjoyInit:AppStoreCreateNSString(appId) key:AppStoreCreateNSString(appKey) channelID:AppStoreCreateNSString(channelID) callback:AppStoreCreateNSString(callbackName)];
    }
    
    // 开始购买商品
    void KingjoyPay(const char* buyInfo)
    {
        NSString * s = AppStoreCreateNSString(buyInfo);
        NSArray * a = [s componentsSeparatedByString:NSLocalizedString(@"&", nil)];
        [[KingjoyIOSHelper instance] pay:a];
    }
    
    void KingjoyStartLogin()
    {
        [[KingjoyIOSHelper instance] KingjoyStartLogin];
    }
    
    
    
#if defined(__cplusplus)
}
#endif

@end
