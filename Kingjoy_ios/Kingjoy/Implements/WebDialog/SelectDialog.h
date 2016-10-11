//
//  SelectDialog.h
//  Kingjoy
//
//  Created by Apple on 15/4/28.
//  Copyright (c) 2015年 Kingjoy. All rights reserved.
//

#import "WebViewJsCaller.h"

@interface SelectDialog : WebViewJsCaller

@property NSObject<KJLoginCallback>* delege;

//使用其他账号，用于通知LoginDialog
@property bool changeOtherAccount;

//打开面板
- (void)showSelect:(NSObject<KJLoginCallback>*) delege ;

@end
