//
//  IOSDevicesInfo.mm
//  Unity-iPhone
//
//  Created by WarZhan on 14/12/4.
//  Copyright (c) 2014年 kingjoy. All rights reserved.
//

#import "IOSDevicesInfo.h"

@implementation IOSDevicesInfo

//给url上报信息,附带上request信息
+(void)UploadDeviceInfo:(NSMutableDictionary *) request url:(NSString*) url{
    [self GetDevicesInfo:request];
    [HttpUtil HttpPost:request url:url type:POST_TEXT callback:nil];
}

//获取广告idfa码
+(NSString * ) GetIDFA {
    NSString *adid = [[[ASIdentifierManager sharedManager] advertisingIdentifier] UUIDString];
    return adid;
}

//获取设备信息
+ (NSString * ) GetDevicesInfo:(NSMutableDictionary * )req
{
    if( req == nil)//防止没传报错
        req = [[NSMutableDictionary alloc]init];
    
    //设备相关信息的获取
    NSString *strName = [[UIDevice currentDevice] name];
    [req setObject:strName forKey:@"deviceName"];
    NSLog(@"设备名称：%@", strName);//e.g. "My iPhone"
    
    // IOS 6.0以上
    NSString *strId = [[UIDevice currentDevice] identifierForVendor].UUIDString;
    [req setObject:strId forKey:@"uuid"];
    
    NSString *strSysName = [[UIDevice currentDevice] systemName];
    [req setObject:strSysName forKey:@"system"];
    NSLog(@"系统名称：%@", strSysName);// e.g. @"iOS"
    
    NSString *strSysVersion = [[UIDevice currentDevice] systemVersion];
    [req setObject:strSysVersion forKey:@"systemVersion"];
    NSLog(@"系统版本号：%@", strSysVersion);// e.g. @"4.0"
    
    NSString *strModel = [[UIDevice currentDevice] model];
    NSLog(@"设备模式：%@", strModel);// e.g. @"iPhone", @"iPod touch"
    
    NSString *strLocModel = [[UIDevice currentDevice] localizedModel];
    NSLog(@"本地设备模式：%@", strLocModel);// localized version of model

    [req setObject:[self GetIDFA] forKey:@"idfa"];
    [req setObject:[self deviceString] forKey:@"deviceType"];
    NSLog(@"设备型号：%@", [self deviceString]);
    NSString * infoStr = [NSString stringWithFormat:@"UUID=%@|IDFA=%@|DeviceName=%@|SystemName=%@|SystemVersion=%@|DeviceType=%@",
                          strId , [self GetIDFA], strName , strSysName, strSysVersion, [self deviceString]];
    return infoStr;
}

+(NSString*)deviceString
{
    // 需要#import "sys/utsname.h"
    struct utsname systemInfo;
    uname(&systemInfo);
    NSString *deviceString = [NSString stringWithCString:systemInfo.machine encoding:NSUTF8StringEncoding];
    
    NSArray *modelArray = @[
                            
                            @"i386", @"x86_64",
                            
                            @"iPhone1,1",
                            @"iPhone1,2",
                            @"iPhone2,1",
                            @"iPhone3,1",
                            @"iPhone3,2",
                            @"iPhone3,3",
                            @"iPhone4,1",
                            @"iPhone5,1",
                            @"iPhone5,2",
                            @"iPhone5,3",
                            @"iPhone5,4",
                            @"iPhone6,1",
                            @"iPhone6,2",
                            @"iPhone7,1",
                            @"iPhone7,2",
                            
                            @"iPod1,1",
                            @"iPod2,1",
                            @"iPod3,1",
                            @"iPod4,1",
                            @"iPod5,1",
                            
                            @"iPad1,1",
                            @"iPad2,1",
                            @"iPad2,2",
                            @"iPad2,3",
                            @"iPad2,4",
                            @"iPad3,1",
                            @"iPad3,2",
                            @"iPad3,3",
                            @"iPad3,4",
                            @"iPad3,5",
                            @"iPad3,6",
                            
                            @"iPad4,1",
                            @"iPad4,2",
                            @"iPad4,3",
                            
                            @"iPad5,3",
                            @"iPad5,4",
                            
                            @"iPad2,5",
                            @"iPad2,6",
                            @"iPad2,7",
                            
                            @"iPad4,4",
                            @"iPad4,5",
                            @"iPad4,6",
                            
                            @"iPad4,7",
                            @"iPad4,8",
                            @"iPad4,9"
                            ];
    NSArray *modelNameArray = @[
                                
                                @"iPhone Simulator", @"iPhone Simulator",
                                
                                @"iPhone 2G",
                                @"iPhone 3G",
                                @"iPhone 3GS",
                                @"iPhone 4(GSM)",
                                @"iPhone 4(GSM Rev A)",
                                @"iPhone 4(CDMA)",
                                @"iPhone 4S",
                                @"iPhone 5(GSM)",
                                @"iPhone 5(GSM+CDMA)",
                                @"iPhone 5c(GSM)",
                                @"iPhone 5c(Global)",
                                @"iphone 5s(GSM)",
                                @"iphone 5s(Global)",
                                @"iphone 6 Plus",
                                @"iphone 6",
                                
                                @"iPod Touch 1G",
                                @"iPod Touch 2G",
                                @"iPod Touch 3G",
                                @"iPod Touch 4G",
                                @"iPod Touch 5G",
                                
                                @"iPad",
                                @"iPad 2(WiFi)",
                                @"iPad 2(GSM)",
                                @"iPad 2(CDMA)",
                                @"iPad 2(WiFi + New Chip)",
                                @"iPad 3(WiFi)",
                                @"iPad 3(GSM+CDMA)",
                                @"iPad 3(GSM)",
                                @"iPad 4(WiFi)",
                                @"iPad 4(GSM)",
                                @"iPad 4(GSM+CDMA)",
                                
                                @"iPad Air A1475",
                                @"iPad Air A1475",
                                @"iPad Air A1475",
                                
                                @"iPad Air 2 A1566",
                                @"iPad Air 2 A1567",
                                
                                @"iPad mini (WiFi)",
                                @"iPad mini (GSM)",
                                @"ipad mini (GSM+CDMA)",
                                
                                @"iPad mini 2 A1489",
                                @"iPad mini 2 A1490",
                                @"iPad mini 2 A1491",
                                
                                @"iPad mini 3 A1599",
                                @"iPad mini 3 A1600",
                                @"iPad mini 3 A1601"
                                
                                ];
    NSInteger modelIndex = - 1;
    NSString *modelNameString = nil;
    modelIndex = [modelArray indexOfObject:deviceString];
    if (modelIndex >= 0 && modelIndex < [modelNameArray count]) {
        modelNameString = [modelNameArray objectAtIndex:modelIndex];
    }
    
    
    //NSLog(@"----设备类型---%@",modelNameString);
    return modelNameString;
}

/*
#if defined(__cplusplus)
extern "C" {
#endif
    
    NSString* CreateNSString (const char* string)
    {
        return [NSString stringWithUTF8String:(string ? string : "")];
    }
    
    // 设置Unity回调的对象名
    void _InitDevicesInfo(const char* callBackObjectName)
    {
        [[IOSDevicesInfo instance] init:CreateNSString(callBackObjectName)];
    }
    
    void _StartGetDevicesInfo()
    {
        [[IOSDevicesInfo instance] startGetDevicesInfo];
    }
    
#if defined(__cplusplus)
}
#endif
*/

@end
