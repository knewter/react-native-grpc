package com.reactnativegrpc

import com.facebook.react.bridge.ReadableMap
import io.grpc.ManagedChannel

data class GrpcConnection(
    val channel: ManagedChannel,
    val settings: ReadableMap
)
