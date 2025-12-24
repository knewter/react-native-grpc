package com.reactnativegrpc

import android.util.Base64
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import io.grpc.CallOptions
import io.grpc.ClientCall
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.Status
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class GrpcModuleImpl(private val context: ReactApplicationContext) {
    private val callsMap = HashMap<Int, ClientCall<*, *>>()
    private val connections = HashMap<Int, GrpcConnection>()
    private val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    companion object {
        const val NAME = "Grpc"
    }

    fun setGrpcSettings(id: Double, settings: ReadableMap) {
        val intId = id.toInt()
        destroyClient(intId.toDouble())

        val channel = createManagedChannel(intId, settings)
        connections[intId] = GrpcConnection(channel, settings)
    }

    fun unaryCall(
        callId: Double,
        clientId: Double,
        path: String,
        obj: ReadableMap,
        headers: ReadableMap,
        promise: Promise
    ) {
        val intCallId = callId.toInt()
        val intClientId = clientId.toInt()

        val call: ClientCall<ByteArray, ByteArray>
        try {
            call = startGrpcCall(intCallId, intClientId, path, MethodDescriptor.MethodType.UNARY, headers)
        } catch (e: Exception) {
            promise.reject(e)
            return
        }

        val data = Base64.decode(obj.getString("data"), Base64.NO_WRAP)
        call.sendMessage(data)
        call.request(1)
        call.halfClose()

        callsMap[intCallId] = call
        promise.resolve(null)
    }

    fun serverStreamingCall(
        callId: Double,
        clientId: Double,
        path: String,
        obj: ReadableMap,
        headers: ReadableMap,
        promise: Promise
    ) {
        val intCallId = callId.toInt()
        val intClientId = clientId.toInt()

        val call: ClientCall<ByteArray, ByteArray>
        try {
            call = startGrpcCall(intCallId, intClientId, path, MethodDescriptor.MethodType.SERVER_STREAMING, headers)
        } catch (e: Exception) {
            promise.reject(e)
            return
        }

        val data = Base64.decode(obj.getString("data"), Base64.NO_WRAP)
        call.sendMessage(data)
        call.request(1)
        call.halfClose()

        callsMap[intCallId] = call
        promise.resolve(null)
    }

    fun clientStreamingCall(
        callId: Double,
        clientId: Double,
        path: String,
        obj: ReadableMap,
        headers: ReadableMap,
        promise: Promise
    ) {
        val intCallId = callId.toInt()
        val intClientId = clientId.toInt()

        @Suppress("UNCHECKED_CAST")
        var call = callsMap[intCallId] as? ClientCall<ByteArray, ByteArray>

        if (call == null) {
            try {
                call = startGrpcCall(intCallId, intClientId, path, MethodDescriptor.MethodType.CLIENT_STREAMING, headers)
            } catch (e: Exception) {
                promise.reject(e)
                return
            }
            callsMap[intCallId] = call
        }

        val data = Base64.decode(obj.getString("data"), Base64.NO_WRAP)
        call.sendMessage(data)
        call.request(1)

        promise.resolve(null)
    }

    fun finishClientStreaming(callId: Double, promise: Promise) {
        val intCallId = callId.toInt()
        val call = callsMap[intCallId]
        if (call != null) {
            call.halfClose()
            promise.resolve(true)
        } else {
            promise.resolve(false)
        }
    }

    fun cancelGrpcCall(callId: Double, promise: Promise) {
        val intCallId = callId.toInt()
        val call = callsMap[intCallId]
        if (call != null) {
            call.cancel("Cancelled", Exception("Cancelled by app"))
            promise.resolve(true)
        } else {
            promise.resolve(false)
        }
    }

    fun destroyClient(clientId: Double) {
        val intId = clientId.toInt()
        connections[intId]?.let { connection ->
            connection.channel.shutdown()
            connections.remove(intId)
        }
    }

    private fun startGrpcCall(
        callId: Int,
        clientId: Int,
        path: String,
        methodType: MethodDescriptor.MethodType,
        headers: ReadableMap
    ): ClientCall<ByteArray, ByteArray> {
        val connection = connections[clientId]
            ?: throw Exception("Channel not created")

        val settings = connection.settings
        val channel = connection.channel

        var normalizedPath = path
        if (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1)
        }

        val headersMetadata = Metadata()
        for ((key, value) in headers.toHashMap()) {
            headersMetadata.put(
                Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER),
                value.toString()
            )
        }

        val marshaller = GrpcMarshaller()

        val descriptor = MethodDescriptor.newBuilder<ByteArray, ByteArray>()
            .setFullMethodName(normalizedPath)
            .setType(methodType)
            .setRequestMarshaller(marshaller)
            .setResponseMarshaller(marshaller)
            .build()

        var callOptions = CallOptions.DEFAULT

        if (settings.hasKey("requestTimeout")) {
            val callTimeout = settings.getInt("requestTimeout")
            callOptions = callOptions.withDeadlineAfter(callTimeout.toLong(), TimeUnit.MILLISECONDS)
        }

        if (settings.hasKey("compressionName")) {
            callOptions = callOptions.withCompression(settings.getString("compressionName"))
        }

        val call = channel.newCall(descriptor, callOptions)

        call.start(object : ClientCall.Listener<ByteArray>() {
            override fun onHeaders(headers: Metadata) {
                super.onHeaders(headers)

                val event = Arguments.createMap()
                val payload = Arguments.createMap()

                for (key in headers.keys()) {
                    if (key.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                        val data = headers.get(Metadata.Key.of(key, Metadata.BINARY_BYTE_MARSHALLER))
                        if (data != null) {
                            payload.putString(key, String(Base64.encode(data, Base64.NO_WRAP)))
                        }
                    } else if (!key.startsWith(":")) {
                        val data = headers.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER))
                        if (data != null) {
                            payload.putString(key, data)
                        }
                    }
                }

                event.putInt("id", callId)
                event.putString("type", "headers")
                event.putMap("payload", payload)

                emitEvent("grpc-call", event)
            }

            override fun onMessage(message: ByteArray) {
                super.onMessage(message)

                val event = Arguments.createMap()
                event.putInt("id", callId)
                event.putString("type", "response")
                event.putString("payload", Base64.encodeToString(message, Base64.NO_WRAP))

                emitEvent("grpc-call", event)

                if (methodType == MethodDescriptor.MethodType.SERVER_STREAMING) {
                    call.request(1)
                }
            }

            override fun onClose(status: Status, trailers: Metadata) {
                super.onClose(status, trailers)

                callsMap.remove(callId)

                val trailersEvent = Arguments.createMap()
                val trailersMap = Arguments.createMap()

                trailersEvent.putInt("id", callId)
                trailersEvent.putString("type", "trailers")

                for (key in trailers.keys()) {
                    if (key.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                        val data = trailers.get(Metadata.Key.of(key, Metadata.BINARY_BYTE_MARSHALLER))
                        if (data != null) {
                            trailersMap.putString(key, String(Base64.encode(data, Base64.NO_WRAP)))
                        }
                    } else if (!key.startsWith(":")) {
                        val data = trailers.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER))
                        if (data != null) {
                            trailersMap.putString(key, data)
                        }
                    }
                }

                if (!status.isOk) {
                    val errorEvent = Arguments.createMap()
                    errorEvent.putInt("id", callId)
                    errorEvent.putString("type", "error")
                    errorEvent.putString("error", status.asException(trailers).localizedMessage ?: "Unknown error")
                    errorEvent.putInt("code", status.code.value())
                    errorEvent.putMap("trailers", trailersMap.copy())

                    emitEvent("grpc-call", errorEvent)
                }

                trailersEvent.putMap("payload", trailersMap)
                emitEvent("grpc-call", trailersEvent)
            }
        }, headersMetadata)

        if (settings.hasKey("compression") && settings.getBoolean("compression")) {
            call.setMessageCompression(true)
        }

        return call
    }

    private fun emitEvent(eventName: String, params: WritableMap) {
        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }

    private fun createManagedChannel(id: Int, options: ReadableMap): ManagedChannel {
        if (!options.hasKey("host")) {
            throw IllegalArgumentException("host is required")
        }

        val host = options.getString("host")!!

        var channelBuilder = ManagedChannelBuilder.forTarget(host).executor(executor)

        if (options.hasKey("insecure") && options.getBoolean("insecure")) {
            channelBuilder = channelBuilder.usePlaintext()
        }

        if (options.hasKey("responseSizeLimit")) {
            val responseSizeLimit = options.getInt("responseSizeLimit")
            channelBuilder = channelBuilder.maxInboundMessageSize(responseSizeLimit)
        }

        var keepalive = true
        if (options.hasKey("keepalive")) {
            keepalive = options.getBoolean("keepalive")
        }

        if (keepalive) {
            var keepAliveTimeout = 20
            var keepaliveInterval = Long.MAX_VALUE

            if (options.hasKey("keepaliveInterval")) {
                keepaliveInterval = options.getInt("keepaliveInterval").toLong()
            }

            if (options.hasKey("keepaliveTimeout")) {
                keepAliveTimeout = options.getInt("keepaliveTimeout")
            }

            channelBuilder = channelBuilder
                .keepAliveWithoutCalls(true)
                .keepAliveTime(keepaliveInterval, TimeUnit.SECONDS)
                .keepAliveTimeout(keepAliveTimeout.toLong(), TimeUnit.SECONDS)
        }

        return channelBuilder.build()
    }
}
