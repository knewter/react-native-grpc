#import "Grpc.h"
#import <React/RCTBridge+Private.h>
#import <React/RCTUtils.h>

// Import the Swift-generated header for bridging to Swift
#if __has_include("react_native_grpc-Swift.h")
#import "react_native_grpc-Swift.h"
#elif __has_include(<react_native_grpc/react_native_grpc-Swift.h>)
#import <react_native_grpc/react_native_grpc-Swift.h>
#else
// When built as a framework, the Swift header has a different name
@import react_native_grpc;
#endif

#ifdef RCT_NEW_ARCH_ENABLED
#import <React/RCTBridge+Private.h>
#import <ReactCommon/RCTTurboModule.h>
#endif

@interface Grpc () <GrpcEventDelegate>
@property (nonatomic, strong) RNGrpc *swiftModule;
@end

@implementation Grpc

RCT_EXPORT_MODULE()

- (instancetype)init {
    if (self = [super init]) {
        _swiftModule = [[RNGrpc alloc] init];
        _swiftModule.eventDelegate = self;
    }
    return self;
}

#pragma mark - GrpcEventDelegate

- (void)sendGrpcEventWithName:(NSString *)name body:(NSDictionary *)body {
    [self sendEventWithName:name body:body];
}

+ (BOOL)requiresMainQueueSetup {
    return NO;
}

- (NSArray<NSString *> *)supportedEvents {
    return @[@"grpc-call"];
}

#pragma mark - Native Module Methods

RCT_EXPORT_METHOD(setGrpcSettings:(double)clientId
                  options:(NSDictionary *)options)
{
    [_swiftModule setGrpcSettings:@(clientId) options:options];
}

RCT_EXPORT_METHOD(destroyClient:(double)clientId)
{
    [_swiftModule destroyClient:@(clientId)];
}

RCT_EXPORT_METHOD(unaryCall:(double)callId
                  clientId:(double)clientId
                  path:(NSString *)path
                  obj:(NSDictionary *)obj
                  headers:(NSDictionary *)headers
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [_swiftModule unaryCall:@(callId)
                   clientId:@(clientId)
                       path:path
                        obj:obj
                    headers:headers
                    resolve:resolve
                     reject:reject];
}

RCT_EXPORT_METHOD(serverStreamingCall:(double)callId
                  clientId:(double)clientId
                  path:(NSString *)path
                  obj:(NSDictionary *)obj
                  headers:(NSDictionary *)headers
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [_swiftModule serverStreamingCall:@(callId)
                             clientId:@(clientId)
                                 path:path
                                  obj:obj
                              headers:headers
                              resolve:resolve
                               reject:reject];
}

RCT_EXPORT_METHOD(clientStreamingCall:(double)callId
                  clientId:(double)clientId
                  path:(NSString *)path
                  obj:(NSDictionary *)obj
                  headers:(NSDictionary *)headers
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [_swiftModule clientStreamingCall:@(callId)
                             clientId:@(clientId)
                                 path:path
                                  obj:obj
                              headers:headers
                              resolve:resolve
                               reject:reject];
}

RCT_EXPORT_METHOD(finishClientStreaming:(double)callId
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [_swiftModule finishClientStreaming:@(callId)
                                resolve:resolve
                                 reject:reject];
}

RCT_EXPORT_METHOD(cancelGrpcCall:(double)callId
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [_swiftModule cancelGrpcCall:@(callId)
                         resolve:resolve
                          reject:reject];
}

RCT_EXPORT_METHOD(addListener:(NSString *)eventName)
{
    // Required for RCTEventEmitter, handled by base class
}

RCT_EXPORT_METHOD(removeListeners:(double)count)
{
    // Required for RCTEventEmitter, handled by base class
}

#pragma mark - TurboModule

#ifdef RCT_NEW_ARCH_ENABLED
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeGrpcSpecJSI>(params);
}
#endif

@end
