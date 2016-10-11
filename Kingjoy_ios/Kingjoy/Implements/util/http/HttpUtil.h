//
//  HttpUtil.h
//  Kingjoy
//
//  Created by Apple on 16/4/8.
//  Copyright © 2016年 Kingjoy. All rights reserved.
//

#import <Foundation/Foundation.h>

#pragma mark - http请求协议返回    *******************************************************

@protocol HttpCallback<NSObject>

/**
 *  调用回调
 */
- (void)HandleResponse:(NSString * )rsp ok:(BOOL)rs;

@end


#pragma mark - http请求协议返回    *******************************************************

typedef enum {
    /*发送form表单*/
    POST_TEXT,
    
    /*发送json对象*/
    POST_JSON
} HttpPostType;

#pragma mark - http请求工具    *******************************************************

@interface HttpUtil : NSObject

//发送http请求,返回html
+(void)HttpPost:(NSDictionary *) request url:(NSString*) url type:(HttpPostType)type callback:(NSObject<HttpCallback> * )callback;

@end

