#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(ExifModifier, NSObject)

RCT_EXTERN_METHOD(saveImageWithUserComment:(NSString *)base64ImageData
                  userComment:(NSString *)userComment
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(saveImageWithProperties:(NSString *)base64ImageData
                  properties:(NSDictionary *)properties
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)

+ (BOOL)requiresMainQueueSetup {
  return NO;
}

@end
