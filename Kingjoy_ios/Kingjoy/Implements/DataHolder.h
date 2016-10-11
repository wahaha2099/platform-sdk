//
//  DataHolder.h
//  Kingjoy
//
//  Created by Apple on 15/4/28.
//  Copyright (c) 2015年 Kingjoy. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface DataHolder : NSObject

+ (DataHolder *)sharedInstance;

@property (assign) int level;
@property (assign) int score;

//用户的信息
@property NSMutableArray* accounts;

//保存账号
-(void) saveAccount:(NSDictionary*) account;

//读取账号
-(NSMutableArray*) loadAccount;

@end
