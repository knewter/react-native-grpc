/**
 * Backward-compatible module loader for the Grpc native module.
 *
 * This module provides a unified interface for accessing the Grpc native
 * module regardless of whether the app is using the New Architecture
 * (TurboModules) or the Legacy Architecture (NativeModules bridge).
 *
 * Usage:
 *   import { GrpcNative, GrpcEmitter } from './GrpcModule';
 *
 *   // Use GrpcNative for native method calls
 *   GrpcNative.setGrpcSettings(clientId, settings);
 *
 *   // Use GrpcEmitter for event subscriptions
 *   GrpcEmitter.addListener('grpc-call', handler);
 */

import { NativeModules, NativeEventEmitter } from 'react-native';
import type { Spec as NativeGrpcSpec } from './NativeGrpc';
import { isTurboModuleEnabled } from './turboModuleUtils';

/**
 * The native Grpc module type.
 * Uses the TurboModule Spec type for type safety across both architectures.
 */
export type GrpcModuleType = NativeGrpcSpec;

/**
 * Load the appropriate native module based on architecture.
 *
 * For New Architecture (TurboModules):
 *   - Uses TurboModuleRegistry via NativeGrpc.ts export
 *   - Provides synchronous, type-safe access
 *
 * For Legacy Architecture:
 *   - Falls back to NativeModules.Grpc
 *   - Uses the async bridge
 */
function loadGrpcModule(): GrpcModuleType {
  if (isTurboModuleEnabled()) {
    // New Architecture: Use TurboModule
    // The default export from NativeGrpc.ts returns the TurboModule
    const NativeGrpc = require('./NativeGrpc').default;

    if (NativeGrpc == null) {
      throw new Error(
        '[react-native-grpc] TurboModule is enabled but NativeGrpc module is not available. ' +
          'Ensure the native module is properly linked.'
      );
    }

    return NativeGrpc as GrpcModuleType;
  } else {
    // Legacy Architecture: Use NativeModules bridge
    const { Grpc: LegacyGrpc } = NativeModules;

    if (LegacyGrpc == null) {
      throw new Error(
        '[react-native-grpc] Grpc native module is not available. ' +
          'Ensure you have properly linked the native module. ' +
          'For iOS: Run `pod install` in the ios directory. ' +
          'For Android: Rebuild your app.'
      );
    }

    return LegacyGrpc as GrpcModuleType;
  }
}

/**
 * The Grpc native module instance.
 *
 * This is the primary export for interacting with gRPC native functionality.
 * It provides a unified interface regardless of the underlying architecture.
 */
export const GrpcNative: GrpcModuleType = loadGrpcModule();

/**
 * Event emitter for receiving gRPC events from native code.
 *
 * Events:
 *   - 'grpc-call': Emitted for all gRPC call lifecycle events
 *     Payload varies based on event type:
 *     - type: 'response' - Contains base64 response data
 *     - type: 'headers' - Contains response headers
 *     - type: 'trailers' - Contains response trailers
 *     - type: 'error' - Contains error message, code, and trailers
 *
 * Usage:
 *   const subscription = GrpcEmitter.addListener('grpc-call', (event) => {
 *     console.log(event.id, event.type, event.payload);
 *   });
 *
 *   // Cleanup
 *   subscription.remove();
 */
export const GrpcEmitter = new NativeEventEmitter(
  // NativeEventEmitter needs the module reference for proper cleanup
  // Works with both TurboModules and legacy NativeModules
  isTurboModuleEnabled() ? (GrpcNative as any) : NativeModules.Grpc
);

/**
 * Re-export architecture detection for consumers who need it.
 */
export { isTurboModuleEnabled } from './turboModuleUtils';
