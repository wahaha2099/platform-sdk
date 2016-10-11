//
//  BindEmailDialog.m
//完善个人资料,绑定邮件,用于找回密码
//  Kingjoy
//
//  Created by Apple on 15/7/6.
//  Copyright (c) 2015年 Kingjoy. All rights reserved.
//

#import "BindEmailDialog.h"

@implementation BindEmailDialog


-(void)showDialog{
    int width = 300;//定义界面宽度
    int height = 300;//定义界面高度
    [self createWebView:@"update_detail" width:width height:height];
}

//子类实现具体业务逻辑
- (void) handle:(NSDictionary *) json {
    NSInteger const SUCCESS = 2;
    
    NSLog(@" handle in update dialog %@" , json);
    int method = (int)[json[@"method"] integerValue];
    
    switch (method) {
        case SUCCESS:{//前端ajax请求结束
            [self handleUpdate:json];
            break;
        }
        default:{
            NSLog(@"not implements yet");
            break;
        }
    }
    
}

//处理登录回调
- (void) handleUpdate:(NSDictionary *) json{
    //1.检查返回结果是否ok
    NSDictionary * rs = [self parseJson:json[@"args"]];
    
    NSString* code = [rs objectForKey:@"code"];
    NSString* message = [rs objectForKey:@"message"];
    
    [self showToast:[NSString stringWithFormat:@"%@" , message ]];
    
    if(code!=nil && [code compare:@"0000"] == NSOrderedSame){
        [self hideView];
    }
}

@end
