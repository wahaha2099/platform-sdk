//
//  IOSDevicesInfo.h
//  Unity-iPhone
//
//  Created by WarZhan on 14/12/4.
//  Copyright (c) 2014年 kingjoy. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AdSupport/AdSupport.h>
#import "sys/utsname.h"
#import "HttpUtil.h"

@interface IOSDevicesInfo : NSObject
{}

//获取设备信息
+ (NSString * ) GetDevicesInfo:(NSMutableDictionary * )req;

//获取广告idfa码
+(NSString * ) GetIDFA;

//给url上报信息,附带上request信息
+(void)UploadDeviceInfo:(NSMutableDictionary *) request url:(NSString*) url;

@end
