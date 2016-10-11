//
//  DataHolder.m
//  Kingjoy
//
//  Created by Apple on 15/4/28.
//  Copyright (c) 2015年 Kingjoy. All rights reserved.
//

#import "DataHolder.h"


NSString * const keyAccount = @"accounts";

@implementation DataHolder

- (id) init
{
    self = [super init];
    if (self)
    {
        _level = 0;
        _score = 0;
    }
    return self;
}

+ (DataHolder *)sharedInstance
{
    static DataHolder *_sharedInstance = nil;
    static dispatch_once_t onceSecurePredicate;
    dispatch_once(&onceSecurePredicate,^
                  {
                      _sharedInstance = [[self alloc] init];
                  });
    
    return _sharedInstance;
}

//保存账号
-(void) saveAccount:(NSDictionary*) account
{
    //1.创建accounts
    if(_accounts == nil){
        _accounts = [[NSMutableArray alloc]init];
    }
    
    //2.添加进去
    bool isExist = false;
    for (NSDictionary* acc in _accounts){
        int  uid =  (int)[[acc objectForKey:@"userId" ]integerValue] ;
        int  uid2 =  (int)[[account objectForKey:@"userId"]integerValue];
        
        if(uid == uid2){
            isExist = true;
        }
    }
    if(!isExist){
        //3.本地存储
        [_accounts addObject:account];//add to array
        [[NSUserDefaults standardUserDefaults] setObject:_accounts forKey:keyAccount];//save
        [[NSUserDefaults standardUserDefaults] synchronize];
    }
}

//读取账号
-(NSMutableArray*) loadAccount
{
    if ([[NSUserDefaults standardUserDefaults] objectForKey:keyAccount])
    {
        NSMutableArray* acc = [[NSUserDefaults standardUserDefaults] objectForKey:keyAccount];
        return acc;
    }
    else
    {
        return nil;
    }
}


@end
