//
//  SelectDialog.m
//  Kingjoy
//
//  Created by Apple on 15/4/28.
//  Copyright (c) 2015年 Kingjoy. All rights reserved.
//

#import "SelectDialog.h"
#import "DataHolder.h"

@implementation SelectDialog


/*打开login dialog
 */
-(void)showSelect:(NSObject<KJLoginCallback> *)delege{
    
    int width = 320;//定义界面宽度
    int height = 320 ;//定义界面高度

    NSString * url = [NSString stringWithFormat:@"%@/sdk/select.html" , [[self setting]baseUrl]];
    [self createWebView:url width:width height:height];
    [self SetFixScreen];
    _delege = delege;
    self.changeOtherAccount = false;
}

//设置accounts到页面中
-(void)setContextToPage:(NSDictionary *)content{
    NSMutableArray* acct = [[DataHolder sharedInstance] loadAccount];

    [content setValue:acct forKey:@"accounts"];
}

//子类实现具体业务逻辑
- (void) handle:(NSDictionary *) json {
    NSInteger const CLOSE = 1;
    NSInteger const goResiste = 2;
    NSInteger const goLogin = 3;
    NSInteger const ajax = 5;
    
    //NSLog(@" handle in select dialog %@" , json);
    int method = (int)[json[@"method"] integerValue];
    
    switch (method) {
        case CLOSE:{
            [KingjoySDK login:_delege];
            break ;
        }
        case goResiste:{
            [KingjoySDK registe:_delege];
            [self hideView];
            break;
        }
        case goLogin:{
            self.changeOtherAccount = true;
            [KingjoySDK login:_delege];
            [self hideView];
            break;
        }
        case ajax:{//前端ajax请求结束
            [self handleLogin:json];
            break;
        }
        default:{
            //NSLog(@"not implements yet");
            break;
        }
    }
    
}

//处理登录回调
- (void) handleLogin:(NSDictionary *) json{
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
        [self showToast:[NSString stringWithFormat:@"%@,登录成功" , name]];
        [[DataHolder sharedInstance] saveAccount:user];
        [KingjoySDK setLoginUser:userInfo];
        [_delege loginFinished:userInfo];
        
        //3.隐藏面板
        [self hideView];
        
    }else{
        [self showToast:[NSString stringWithFormat:@"登录失败，%@" , [rs objectForKey:@"message"]]];
        //NSLog(@"call error %@" , [rs objectForKey:@"message"]);
    }
}

@end
