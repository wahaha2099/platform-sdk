//
//  WebViewJsCaller.m
// Dialog类对页面暴露的参数
//

#import "WebViewJsCaller.h"


@implementation WebViewJsCaller

- (void) call:(NSArray *) str {
    //NSLog(@"call %@" , str);
    NSError *jsonError;
    NSData *objectData = [str[0] dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *json = [NSJSONSerialization JSONObjectWithData:objectData
                                                         options:NSJSONReadingMutableContainers
                                                           error:&jsonError];

    int method = (int)[json[@"method"] integerValue];

    switch (method) {
        case 1://close the webview
            [self hideView];
            [self handle:json];//子类做一些扩展调用
            break;
        default:
            [self handle:json];//其他调用子类方法
            break;
    }
}

- (void) handle:(NSDictionary *) json {
    //子类非公共实现
}
- (void) info:(NSArray *)msg{
    NSLog(@"info %@" , msg);
}

- (void) debug:(NSArray *)msg{
    NSLog(@"info %@" , msg);
}

- (void) error:(NSArray *)msg{
    NSLog(@"info %@" , msg);
}

- (void) warn:(NSArray *)msg{
    NSLog(@"info %@" , msg);
}

- (void) toast:(NSArray *)msg{
    NSLog(@"info %@" , msg);
}

- (void) showWait:(NSArray *)msg{
    NSLog(@"info %@" , msg);
}

- (void) hideWait:(NSArray *)msg{
    NSLog(@"info %@" , msg);
}



@end