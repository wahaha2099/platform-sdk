//
//  JoinDialog.h
//  tbGameDemo
//
//  Created by Apple on 15/4/22.
//  Copyright (c) 2015年 tongbu.com. All rights reserved.
//

#import "WebViewJsCaller.h"

@interface JoinDialog : WebViewJsCaller

@property NSObject<KJLoginCallback>* delege;

//打开面板
- (void)showJoin:(NSObject<KJLoginCallback>*) delege ;

@end
