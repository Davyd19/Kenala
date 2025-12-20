package com.app.kenala.utils

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

object SocketManager {
    private var socket: Socket? = null

    // Ganti URL ini dengan IP Address komputer Anda jika menggunakan Emulator (biasanya 10.0.2.2)
    // atau IP Address LAN jika menggunakan Device Fisik (misal 192.168.1.X:3000)
    private const val SOCKET_URL = "http://10.0.2.2:3000"

    fun connect() {
        if (socket == null) {
            try {
                val options = IO.Options()
                options.transports = arrayOf("websocket")
                options.reconnection = true

                socket = IO.socket(SOCKET_URL, options)

                socket?.on(Socket.EVENT_CONNECT) {
                    Log.d("SocketManager", "Connected to server: ${socket?.id()}")
                }

                socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                    Log.e("SocketManager", "Connection error: ${args.getOrElse(0) { "Unknown" }}")
                }

                socket?.connect()

            } catch (e: URISyntaxException) {
                Log.e("SocketManager", "URI Syntax Error: ${e.message}")
            }
        } else if (socket?.connected() == false) {
            socket?.connect()
        }
    }

    // Fungsi ini yang sebelumnya hilang dan menyebabkan error
    fun emit(event: String, data: Any) {
        if (socket == null) {
            connect()
        }

        if (socket?.connected() == true) {
            socket?.emit(event, data)
            Log.d("SocketManager", "Emitted event: $event with data: $data")
        } else {
            Log.w("SocketManager", "Socket not connected. Failed to emit: $event")
            // Coba reconnect
            socket?.connect()
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        Log.d("SocketManager", "Disconnected")
    }

    fun isConnected(): Boolean {
        return socket?.connected() == true
    }
}