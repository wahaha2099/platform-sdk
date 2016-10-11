//
//  WebViewJsCaller.h
//  tbGameDemo
//
//  Created by Apple on 15/4/20.
//  Copyright (c) 2015年 tongbu.com. All rights reserved.
//

#import "WebViewJsBridge.h"
#import "KingjoySDK.h"
#import "MBProgressHUD.h"
#import "WebViewJsSuper.h"

@interface WebViewJsCaller : WebViewJsSuper

//子类通过此实现业务方法
- (void) handle:(NSDictionary *) str ;

@end
