package com.reactnativegrpc

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = GrpcModuleImpl.NAME)
class GrpcModule(reactContext: ReactApplicationContext) : NativeGrpcSpec(reactContext) {
    private val impl = GrpcModuleImpl(reactContext)

    override fun getName(): String = GrpcModuleImpl.NAME

    override fun setGrpcSettings(clientId: Double, settings: ReadableMap) {
        impl.setGrpcSettings(clientId, settings)
    }

    override fun destroyClient(clientId: Double) {
        impl.destroyClient(clientId)
    }

    override fun unaryCall(
        callId: Double,
        clientId: Double,
        path: String,
        obj: ReadableMap,
        headers: ReadableMap,
        promise: Promise
    ) {
        impl.unaryCall(callId, clientId, path, obj, headers, promise)
    }

    override fun serverStreamingCall(
        callId: Double,
        clientId: Double,
        path: String,
        obj: ReadableMap,
        headers: ReadableMap,
        promise: Promise
    ) {
        impl.serverStreamingCall(callId, clientId, path, obj, headers, promise)
    }

    override fun clientStreamingCall(
        callId: Double,
        clientId: Double,
        path: String,
        obj: ReadableMap,
        headers: ReadableMap,
        promise: Promise
    ) {
        impl.clientStreamingCall(callId, clientId, path, obj, headers, promise)
    }

    override fun finishClientStreaming(callId: Double, promise: Promise) {
        impl.finishClientStreaming(callId, promise)
    }

    override fun cancelGrpcCall(callId: Double, promise: Promise) {
        impl.cancelGrpcCall(callId, promise)
    }

    override fun addListener(eventName: String) {
        // Required for event emitter
    }

    override fun removeListeners(count: Double) {
        // Required for event emitter
    }
}
