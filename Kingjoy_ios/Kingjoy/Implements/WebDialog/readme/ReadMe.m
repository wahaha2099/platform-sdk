//
//  ReadMe.m
//  Kingjoy
//
//  Created by Apple on 16/4/9.
//  Copyright © 2016年 Kingjoy. All rights reserved.
//

#import "ReadMe.h"
#import "DataHolder.h"

@implementation ReadMe

/*打开使用协议
 */
-(void)showReadMe{
    
    int width = 320 ;//定义界面宽度
    int height = 300 ;//定义界面高度
    
    NSString * url = [NSString stringWithFormat:@"%@/sdk/readme.html" , [[self setting]baseUrl]];
    [self createWebView:url width:width height:height];
    
    //设置全屏
    [self SetFullScreen];
}


//子类实现具体业务逻辑
- (void) handle:(NSDictionary *) json {
    NSInteger const CLOSE = 1;
    
    int method = (int)[json[@"method"] integerValue];
    
    switch (method) {
        case CLOSE:{//前端ajax请求结束
            [KingjoySDK login:nil];
            break;
        }
        default:{
//            NSLog(@"not implements yet");
            break;
        }
    }
    
}


@end
