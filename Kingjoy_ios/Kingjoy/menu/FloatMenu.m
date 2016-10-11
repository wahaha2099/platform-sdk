//
//  FloatMenu.m
//  Kingjoy
//
//  登录后的悬浮框
//
//  Created by Apple on 15/7/6.
//  Copyright (c) 2015年 Kingjoy. All rights reserved.
//

#import "FloatMenu.h"
#import "FloatView.h"
#import "BindEmailDialog.h"
#import "ForgetPswDialog.h"
#import "KingjoySDK.h"

@implementation FloatMenu

static FloatView * floatView;   //悬浮按钮

static FloatMenu * floatMenuInstance;
static BindEmailDialog * emailDialog ;
static ForgetPswDialog * forgetDialog ;

+(instancetype)Instance{
    if( floatMenuInstance == nil ){
        floatMenuInstance = [[FloatMenu alloc ]init];
    }
    return floatMenuInstance;
}

//创建float menu
-(void) createFloatMenu{
    //初始化
    floatView = [FloatView defaultFloatViewWithButtonImageNameArray:@[@"btn_tutorial",@"avatar"]];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(boardButtonClick:) name:FloatViewClickNotification object:nil];
}

- (void)boardButtonClick:(NSNotification*)center
{
    int index = [[center object]intValue];
    
    switch (index) {
        case 0:{
            //show bind email dialog
            if(forgetDialog != nil)[forgetDialog hideView];
            if(emailDialog == nil)
                emailDialog = [BindEmailDialog initSetting:[KingjoySDK getKingjoySetting]];
            [emailDialog showDialog];
            break;
        }
        case 1:{
            //show forget password dialog
            if(emailDialog != nil)[emailDialog hideView];
            if(forgetDialog == nil)
            forgetDialog = [ForgetPswDialog initSetting:[KingjoySDK getKingjoySetting]];
            [forgetDialog showDialog];
            break;
        }
        default:
            break;
    }
}


@end
