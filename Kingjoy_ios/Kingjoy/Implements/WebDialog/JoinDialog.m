//
//  JoinDialog.m
//
//  Created by Apple on 15/4/22.
//  Copyright (c) 2015年 kingjoy.cn. All rights reserved.

#import "JoinDialog.h"
#import "DataHolder.h"

@implementation JoinDialog

/*打开login dialog
 */
-(void)showJoin:(NSObject<KJLoginCallback> *)delege{
    NSString * url = [NSString stringWithFormat:@"%@/sdk/join.html" , [[self setting]baseUrl]];
    [self createWebView:url width:320 height:300];
    [self SetFixScreen];

    _delege = delege;
}

//子类实现具体业务逻辑
- (void) handle:(NSDictionary *) json {
    NSInteger const CLOSE = 1;
    NSInteger const SUCCESS = 2;
    NSInteger const SHOW_README=5;
    
    //NSLog(@" handle in login dialog %@" , json);
    int method = (int)[json[@"method"] integerValue];
    
    switch (method) {
        case CLOSE:{
            [KingjoySDK login:_delege];
            break ;
        }
        case SUCCESS:{//前端ajax请求结束
            [self handleJoin:json];
            break;
        }
        case SHOW_README:{
            [KingjoySDK showReadMe];
            break;
        }
        default:{
            //NSLog(@"not implements yet");
            break;
        }
    }
    
}

//处理登录回调
- (void) handleJoin:(NSDictionary *) json{
    //1.检查返回结果是否ok
    NSDictionary * rs = [self parseJson:json[@"args"]];
    NSString* code = [rs objectForKey:@"code"];
    if(code!=nil && [code compare:@"0000"] == NSOrderedSame){
        //2.成功获取用户信息
        NSDictionary * user = [rs objectForKey:@"returnObj"];
        NSString* userId = [user objectForKey:@"userId"];
        NSString* name = [user objectForKey:@"userName"];
        NSString* token = [user objectForKey:@"token"];
        //NSLog(@"login token = %@ , name = %@ , id =%@" , token , name ,userId);
        
        //3.登录回调
        /**/
        KJUserInfo *userInfo = [[ KJUserInfo alloc] init ];
        [userInfo setUserID:userId];
        [userInfo setSessionID:token];
        [userInfo setNickName:name];
        [self showToast:[NSString stringWithFormat:@"%@,注册成功" , name]];
        [[DataHolder sharedInstance] saveAccount:user];
        [KingjoySDK setLoginUser:userInfo];
        [_delege loginFinished:userInfo];
        
        
        //3.隐藏面板
        [self hideView];
    }else{
        [self showToast:[NSString stringWithFormat:@"注册失败，%@" , [rs objectForKey:@"message"]]];
        //NSLog(@"call error %@" , [rs objectForKey:@"message"]);
    }
}

@end
