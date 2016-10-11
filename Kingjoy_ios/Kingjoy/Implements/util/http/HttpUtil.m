//
//  HttpUtil.m
//  Kingjoy
//
//  Created by Apple on 16/4/8.
//  Copyright © 2016年 Kingjoy. All rights reserved.
//

#import "HttpUtil.h"

@implementation HttpUtil

//发送http请求,返回html
+(void)HttpPost:(NSDictionary *) request url:(NSString*) url type:(HttpPostType)type callback:(NSObject<HttpCallback> * )callback{

    NSMutableURLRequest *storeRequest = [self CreateRequest:request url:url type:type];
    
    // Make a connection to the iTunes Store on a background queue.
    NSOperationQueue *queue = [[NSOperationQueue alloc] init];
    [NSURLConnection sendAsynchronousRequest:storeRequest queue:queue
                           completionHandler:^(NSURLResponse *response, NSData *data, NSError *connectionError)
     {
         NSString * rsp = nil;
         if (connectionError)
         {
             /* ... Handle error ... */
             NSLog(@"http error: %@", connectionError);
         }
         else
         {
             NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse *) response;
             NSLog(@"response status code: %ld", (long)[httpResponse statusCode]);
             
             if ( [httpResponse statusCode] == 200 ){
                 rsp =[[NSString alloc ] initWithData:data encoding:NSUTF8StringEncoding];
                 NSLog(@"http response  : %@",  rsp);
             }
         }
         
         if( callback != nil )
             [callback HandleResponse: rsp ok:rsp!=nil];
     }];
}

//解析json
+(NSDictionary * )ParseJson:(NSString *) json{
    NSError *error;
    
    NSDictionary *jsonResponse = [NSJSONSerialization JSONObjectWithData:[json dataUsingEncoding:NSUTF8StringEncoding] options:0 error:&error];
    
    return jsonResponse;
}

//创建NSMutableURLRequest类
+(NSMutableURLRequest* )CreateRequest:(NSDictionary *) request url:(NSString*) url type:(HttpPostType)type {
    
    NSError *error;
    
    NSURL *storeURL = [NSURL URLWithString: url ];
    NSMutableURLRequest *storeRequest = [NSMutableURLRequest requestWithURL:storeURL];
    [storeRequest setHTTPMethod:@"POST"];
    
    if(type == POST_TEXT ){
        //form method
        NSMutableArray* parametersArray = [[NSMutableArray alloc] init];
        [request enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
            [parametersArray addObject:[NSString stringWithFormat:@"%@=%@", key, obj]];
        }];
        NSString* parameterString = [parametersArray componentsJoinedByString:@"&"];
        NSData * data = [parameterString dataUsingEncoding:NSUTF8StringEncoding];
        
        [storeRequest setHTTPBody: data ];
        [storeRequest setValue:@"application/x-www-form-urlencoded" forHTTPHeaderField:@"Content-Type"];
    }
    
    if( type == POST_JSON){
        //json method
        NSData *requestDataKJ= [NSJSONSerialization dataWithJSONObject: request
                                                               options:0
                                                                 error:&error];
        [storeRequest setHTTPBody:requestDataKJ];
        [storeRequest setValue:@"application/json" forHTTPHeaderField:@"Accept"];
        [storeRequest setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    }
    return storeRequest ;
}


@end
