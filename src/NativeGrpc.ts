import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

/**
 * Settings for configuring a gRPC client connection.
 * Maps to GrpcClientSettings in types.ts
 */
export type GrpcClientSettings = {
  host: string;
  insecure?: boolean;
  compression?: boolean;
  compressionName?: string;
  compressionLimit?: number;
  responseLimit?: number;
  keepalive?: boolean;
  keepaliveInterval?: number;
  keepaliveTimeout?: number;
  requestTimeout?: number;
};

/**
 * Request object containing base64-encoded data.
 * TurboModules handle Uint8Array poorly, so we use base64 strings.
 */
export type GrpcRequestObject = {
  data: string;
};

/**
 * Metadata key-value pairs for gRPC headers/trailers.
 * Codegen requires explicit Object type definition.
 */
export type GrpcMetadata = { [key: string]: string };

/**
 * TurboModule Spec for the Grpc native module.
 *
 * IMPORTANT: The interface MUST be named "Spec" for Codegen to recognize it.
 * The file MUST follow "Native<ModuleName>.ts" naming pattern.
 */
export interface Spec extends TurboModule {
  /**
   * Configure or update settings for a gRPC client.
   * Creates a new connection if one doesn't exist for the given clientId.
   *
   * @param clientId - Unique identifier for the client instance
   * @param settings - Connection configuration options
   */
  setGrpcSettings(clientId: number, settings: GrpcClientSettings): void;

  /**
   * Destroy a gRPC client and close its connection.
   *
   * @param clientId - The client instance to destroy
   */
  destroyClient(clientId: number): void;

  /**
   * Initiate a unary (single request, single response) gRPC call.
   *
   * @param callId - Unique identifier for this call
   * @param clientId - The client instance to use
   * @param path - The gRPC method path (e.g., "/package.Service/Method")
   * @param obj - Request object with base64-encoded data
   * @param headers - Request metadata headers
   * @returns Promise that resolves when the call is initiated
   */
  unaryCall(
    callId: number,
    clientId: number,
    path: string,
    obj: GrpcRequestObject,
    headers: GrpcMetadata
  ): Promise<void>;

  /**
   * Initiate a server streaming gRPC call.
   *
   * @param callId - Unique identifier for this call
   * @param clientId - The client instance to use
   * @param path - The gRPC method path
   * @param obj - Request object with base64-encoded data
   * @param headers - Request metadata headers
   * @returns Promise that resolves when the call is initiated
   */
  serverStreamingCall(
    callId: number,
    clientId: number,
    path: string,
    obj: GrpcRequestObject,
    headers: GrpcMetadata
  ): Promise<void>;

  /**
   * Send a message on a client streaming call.
   * Creates the call if it doesn't exist, or sends additional messages.
   *
   * @param callId - Unique identifier for this call
   * @param clientId - The client instance to use
   * @param path - The gRPC method path
   * @param obj - Request object with base64-encoded data
   * @param headers - Request metadata headers
   * @returns Promise that resolves when the message is sent
   */
  clientStreamingCall(
    callId: number,
    clientId: number,
    path: string,
    obj: GrpcRequestObject,
    headers: GrpcMetadata
  ): Promise<void>;

  /**
   * Complete a client streaming call (half-close the stream).
   *
   * @param callId - The call to finish
   * @returns Promise that resolves when the stream is closed
   */
  finishClientStreaming(callId: number): Promise<void>;

  /**
   * Cancel an in-progress gRPC call.
   *
   * @param callId - The call to cancel
   * @returns Promise resolving to true if cancelled, false if call not found
   */
  cancelGrpcCall(callId: number): Promise<boolean>;

  /**
   * Required for NativeEventEmitter compatibility.
   * Called when JS side adds an event listener.
   *
   * @param eventName - Name of the event being listened to
   */
  addListener(eventName: string): void;

  /**
   * Required for NativeEventEmitter compatibility.
   * Called when JS side removes event listeners.
   *
   * @param count - Number of listeners removed
   */
  removeListeners(count: number): void;
}

/**
 * Export the TurboModule using get() which returns null if unavailable.
 * For backward compatibility, use the module loader in Grpc.ts instead.
 */
export default TurboModuleRegistry.get<Spec>('Grpc');
