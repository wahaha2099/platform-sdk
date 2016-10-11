//
//  KingjoyDefines.h
//  tbGameDemo
//
//  Created by Apple on 15/4/22.
//  Copyright (c) 2015年 tongbu.com. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "KingjoySetting.h"

#ifndef tbGameDemo_KingjoyDefines_h
#define tbGameDemo_KingjoyDefines_h

#pragma mark - kingjoy 通知

#pragma mark - kingjoy 登录用户信息    ***********************************************
/**
 *用户的基础信息（登录后获得）
 */
@interface KJUserInfo : NSObject

@property (nonatomic, copy) NSString *sessionID;	/* 登录会话id,用于登录验证 */
@property (nonatomic, copy) NSString *userID;       /* 用户id */
@property (nonatomic, copy) NSString *nickName;     /* 用户昵称 */

@end

#pragma mark - kingjoy回调协议    *******************************************************

@protocol KJLoginCallback<NSObject>

/**
 *  登录结束通知
 */
- (void)loginFinished:(KJUserInfo * )user;

@end

//=======================

@protocol KJPayCallback<NSObject>

typedef enum {
    /*支付成功*/
    KJBuySuccess,
    
    /*支付失败*/
    KJBuyFail
} KJ_BUYGOODS_RESULT;

/**
 *  支付结束通知
 */
- (void)payFinished:(KJ_BUYGOODS_RESULT * )rs;

@end

#pragma mark - end kingjoy



#endif
