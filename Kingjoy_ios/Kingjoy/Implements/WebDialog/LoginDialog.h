//
//  LoginDialog.h
//  tbGameDemo
//
//  Created by Apple on 15/4/21.
//  Copyright (c) 2015年 tongbu.com. All rights reserved.
//


#import "WebViewJsCaller.h"

@interface LoginDialog : WebViewJsCaller

//登录成功的回调
@property NSObject<KJLoginCallback>* delege;

//打开登录
- (void)showLogin:(NSObject<KJLoginCallback>*)delege ;

@end
