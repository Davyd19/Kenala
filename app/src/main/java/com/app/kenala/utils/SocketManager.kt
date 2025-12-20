package com.app.kenala.utils

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SocketManager {
    private const val TAG = "SocketManager"

    // GANTI IP INI SESUAI ENV ANDA:
    // Emulator: "http://10.0.2.2:5000"
    // Device Fisik: "http://192.168.x.x:5000" (Cek IP Laptop Anda)
    private const val SERVER_URL = "http://10.0.2.2:5000"

    private var mSocket: Socket? = null

    // StateFlow untuk UI agar bisa observe data real-time
    private val _trackingUpdates = MutableStateFlow<JSONObject?>(null)
    val trackingUpdates: StateFlow<JSONObject?> = _trackingUpdates

    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus: StateFlow<Boolean> = _connectionStatus

    @Synchronized
    fun init() {
        if (mSocket == null) {
            try {
                val options = IO.Options().apply {
                    reconnection = true
                    reconnectionAttempts = Int.MAX_VALUE
                    reconnectionDelay = 1000
                    timeout = 20000
                }
                mSocket = IO.socket(SERVER_URL, options)
                setupListeners()
            } catch (e: URISyntaxException) {
                Log.e(TAG, "URI Error: ${e.message}")
            }
        }
    }

    private fun setupListeners() {
        mSocket?.on(Socket.EVENT_CONNECT) {
            Log.d(TAG, "Connected to server")
            _connectionStatus.value = true
        }?.on(Socket.EVENT_DISCONNECT) {
            Log.d(TAG, "Disconnected from server")
            _connectionStatus.value = false
        }?.on(Socket.EVENT_CONNECT_ERROR) { args ->
            Log.e(TAG, "Connection Error: ${args.getOrElse(0) { "Unknown" }}")
            _connectionStatus.value = false
        }?.on("tracking_update") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                Log.d(TAG, "Received Update: $data")
                _trackingUpdates.value = data
            }
        }
    }

    fun connect() {
        if (mSocket?.connected() == false) {
            mSocket?.connect()
        }
    }

    fun disconnect() {
        mSocket?.disconnect()
    }

    fun joinMission(userId: Int, missionId: Int) {
        val data = JSONObject()
        data.put("userId", userId)
        data.put("missionId", missionId)
        mSocket?.emit("join_mission_tracking", data)
    }

    fun sendLocation(lat: Double, lng: Double, speed: Float = 0f) {
        if (mSocket?.connected() == true) {
            val data = JSONObject()
            data.put("latitude", lat)
            data.put("longitude", lng)
            data.put("speed", speed)
            mSocket?.emit("update_location", data)
        }
    }

    fun stopTracking() {
        mSocket?.emit("stop_tracking")
    }
}