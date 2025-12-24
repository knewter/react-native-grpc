#import <React/RCTEventEmitter.h>

#ifdef RCT_NEW_ARCH_ENABLED
#import <RNGrpcSpec/RNGrpcSpec.h>

@interface Grpc : RCTEventEmitter <NativeGrpcSpec>
#else
#import <React/RCTBridgeModule.h>

@interface Grpc : RCTEventEmitter <RCTBridgeModule>
#endif

@end
