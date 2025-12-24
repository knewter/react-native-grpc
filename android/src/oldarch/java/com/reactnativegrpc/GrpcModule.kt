package com.reactnativegrpc

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = GrpcModuleImpl.NAME)
class GrpcModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private val impl = GrpcModuleImpl(reactContext)

    override fun getName(): String = GrpcModuleImpl.NAME

    @ReactMethod
    fun setGrpcSettings(clientId: Double, settings: ReadableMap) {
        impl.setGrpcSettings(clientId, settings)
    }

    @ReactMethod
    fun destroyClient(clientId: Double) {
        impl.destroyClient(clientId)
    }

    @ReactMethod
    fun unaryCall(
        callId: Double,
        clientId: Double,
        path: String,
        obj: ReadableMap,
        headers: ReadableMap,
        promise: Promise
    ) {
        impl.unaryCall(callId, clientId, path, obj, headers, promise)
    }

    @ReactMethod
    fun serverStreamingCall(
        callId: Double,
        clientId: Double,
        path: String,
        obj: ReadableMap,
        headers: ReadableMap,
        promise: Promise
    ) {
        impl.serverStreamingCall(callId, clientId, path, obj, headers, promise)
    }

    @ReactMethod
    fun clientStreamingCall(
        callId: Double,
        clientId: Double,
        path: String,
        obj: ReadableMap,
        headers: ReadableMap,
        promise: Promise
    ) {
        impl.clientStreamingCall(callId, clientId, path, obj, headers, promise)
    }

    @ReactMethod
    fun finishClientStreaming(callId: Double, promise: Promise) {
        impl.finishClientStreaming(callId, promise)
    }

    @ReactMethod
    fun cancelGrpcCall(callId: Double, promise: Promise) {
        impl.cancelGrpcCall(callId, promise)
    }

    @ReactMethod
    fun addListener(eventName: String) {
        // Required for event emitter
    }

    @ReactMethod
    fun removeListeners(count: Double) {
        // Required for event emitter
    }
}
