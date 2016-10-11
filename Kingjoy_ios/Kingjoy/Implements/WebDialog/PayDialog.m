//
//  PayDialog.m
//  tbGameDemo
//
//  Created by Apple on 15/4/22.
//  Copyright (c) 2015年 tongbu.com. All rights reserved.
//

#import "PayDialog.h"

@interface PayDialog ()

@property double amount          ;//商品总价
@property NSString * productName ;//商品名称
@property NSString * customInfo  ;//客户端自定义参数，透传参数
@property NSString * gameOrderId ;//游戏orderId
@property double unitPrice       ;//商品定价
@property int count              ;//数量
@property int payType            ;//支付类型：1支付宝，2银联，3微信

#pragma mark - 返回参数
@property NSDictionary * payTypeResponse    ;//创建订单后，返回的对应支付类型的参数

@end

//=====================

@implementation PayDialog

int const ALIPAY = 1;//支付宝付款
int const UNIPAY = 3;//银联付款
int const WEIXIN = 2;//微信付款

/**
 支付:
 gameOrderId:商品金额
 money:金额
 productName:商品名称
 customInfo:透传参数,客户端自定义参数
 unitPrice:商品单价
 count:商品数量
 */
- (void)pay:(double)amount unitPrice:(double)price count:(int)count productName:(NSString*)productName customInfo:(NSString*)customInfo gameOrderId:(NSString*)gameOrderId delege:(NSObject<KJPayCallback>*)delege{
    _amount = amount;
    _unitPrice = price;
    _count = count;
    _productName = productName;
    _customInfo = customInfo;
    _gameOrderId = gameOrderId;
    _delege = delege;
    [self showPay];
}


/**
 支付:
 gameOrderId:商品金额
 amount:金额
 productName:商品名称
 customInfo:透传参数,客户端自定义参数
 */
- (void)pay:(double)amount productName:(NSString*)productName customInfo:(NSString*)customInfo gameOrderId:(NSString*)gameOrderId delege:(NSObject<KJPayCallback>*)delege{
    _amount = amount;
    _productName = productName;
    _customInfo = customInfo;
    _gameOrderId = gameOrderId;
    _delege = delege;
    [self showPay];
}

//显示支付对话框
-(void) showPay {
    int width = 300;//定义界面宽度
    int height = 300;//定义界面高度
    [self createWebView:@"payment" width:width height:height];
}

//子类实现具体业务逻辑
- (void) handle:(NSDictionary *) json {
    NSInteger const PAY_ACTION = 2;
    
    NSLog(@" handle in login dialog %@" , json);
    int method = (int)[json[@"method"] integerValue];
    
    switch (method) {
        case PAY_ACTION:{//前端ajax请求结束
            [self handlePay:(int)[json[@"args"] integerValue]];
            break;
        }
        default:{
            NSLog(@"not implements yet");
            break;
        }
    }
}

//处理支付
-(void) handlePay:(int) type{
    switch (type) {
        case ALIPAY:
        {
            _payType = ALIPAY;
            [self createGameOrder];
            [self hideView];
            [Alipay pay:_amount productName:_productName customInfo:_customInfo orderInfo:_payTypeResponse delege:_delege dialog:self];
            break;
        }
        case WEIXIN:{
            _payType = WEIXIN;
            [self createGameOrder];
            [self hideView];
            [WeixinPay pay:_amount productName:_productName customInfo:_customInfo orderInfo:_payTypeResponse delege:_delege dialog:self];
            break;
        }
        case UNIPAY:{
            _payType = UNIPAY;
            [self createGameOrder];
            [self hideView];
            [UniPay pay:_amount productName:_productName customInfo:_customInfo orderInfo:_payTypeResponse delege:_delege dialog:self];
            break;
        }
        default:
        break;
    }
    [self hideView];
}

//添加price,productName到页面
- (void)setContextToPage:(NSDictionary *)content{
    [content setValue:_productName forKey:@"productName"];
    [content setValue:[NSString stringWithFormat:@"%0.2f",_amount ] forKey:@"amount"];
}

//1.创建订单
- (void)createGameOrder {

    NSMutableString *post = [NSMutableString string];
    [post appendFormat:@"productName=%@", _productName];
    [post appendFormat:@"&amount=%0.2f" , _amount];
    [post appendFormat:@"&customInfo=%@" , _customInfo];
    [post appendFormat:@"&customOrderId=%@" , _gameOrderId];
    switch (_payType) {
        case ALIPAY:
            [post appendFormat:@"&payType=A1"];
            break;
        case UNIPAY:
            [post appendFormat:@"&payType=U1"];
            break;
        case WEIXIN:
            [post appendFormat:@"&payType=W1"];
            break;
        default:
            break;
    }
    [post appendFormat:@"&unitPrice=%0.2f" , _unitPrice];
    [post appendFormat:@"&count=%i" , _count];
    [post appendFormat:@"&appId=%@" , [[self setting ] appid]];
    [post appendFormat:@"&playerId=%@" , [[KingjoySDK loginUser] userID]];
    [post appendFormat:@"&platformId=%@" , [[self setting] channel]];
    
    NSData *postData = [post dataUsingEncoding:NSUTF8StringEncoding allowLossyConversion:YES];
    
    //log
    NSString * url = [NSString stringWithFormat:@"%@pay/createOrder",[[self setting]baseUrl]];
    NSLog(@"req : %@" , url);
    NSLog(@"data:%@" , post );
    //end log
    
    NSString *postLength = [NSString stringWithFormat:@"%i", (int)[postData length]];
    
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc] init] ;
    [request setURL:[NSURL URLWithString:[NSString stringWithFormat:@"%@pay/createOrder",[[self setting]baseUrl]]]];
    [request setHTTPMethod:@"POST"];
    [request setValue:postLength forHTTPHeaderField:@"Content-Length"];
    [request setValue:@"application/x-www-form-urlencoded" forHTTPHeaderField:@"Content-Type"];
    [request setHTTPBody:postData];
    
    //2.处理http response
    NSURLResponse *response;
    NSError *err;
    NSData *responseData = [NSURLConnection sendSynchronousRequest:request returningResponse:&response error:&err];
    NSString *string = [[NSString alloc] initWithData:responseData encoding:NSASCIIStringEncoding];
    NSLog(@"gameOrder response :%@ " , string);
    
    
    //3.获取返回的订单号
    NSDictionary *json = [self parseJson:string];
    
    NSString* code = [json objectForKey:@"code"];
    if(code!=nil && [code compare:@"0000"] == NSOrderedSame){
        _payTypeResponse = [json objectForKey:@"returnObj"];
    }
    
    //return nil;
}

@end
