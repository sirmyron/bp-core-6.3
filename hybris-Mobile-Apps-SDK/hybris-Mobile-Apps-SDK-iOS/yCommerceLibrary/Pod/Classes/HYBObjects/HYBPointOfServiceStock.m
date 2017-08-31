//
// HYBPointOfServiceStock.m
// [y] hybris Platform
//
// Copyright (c) 2000-2016 hybris AG
// All rights reserved.
//
// This software is the confidential and proprietary information of hybris
// ("Confidential Information"). You shall not disclose such Confidential
// Information and shall use it only in accordance with the terms of the
// license agreement you entered into with hybris.
//
// Warning:This file was auto-generated by OCC2Ojbc.
//

#import "HYBPointOfServiceStock.h"
#import "NSValueTransformer+MTLPredefinedTransformerAdditions.h"

#import "HYBStock.h"


@implementation HYBPointOfServiceStock

+ (instancetype)pointOfServiceStockWithParams:(NSDictionary*)params {

NSError *error = nil;
HYBPointOfServiceStock *object = [MTLJSONAdapter modelOfClass:[HYBPointOfServiceStock class] fromJSONDictionary:params error:&error];

if (error) {
    NSLog(@"Couldn't convert JSON to model HYBPointOfServiceStock");
    return nil;
}

return object;
}

+ (NSDictionary *)JSONKeyPathsByPropertyKey {
   return @{
@"stockInfo" : @"stockInfo"
};
}




+ (NSValueTransformer *)stockInfoJSONTransformer {
return [NSValueTransformer mtl_JSONDictionaryTransformerWithModelClass:[HYBStock class]];
}



@end