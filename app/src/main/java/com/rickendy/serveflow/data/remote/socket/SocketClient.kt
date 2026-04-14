package com.rickendy.serveflow.data.remote.socket

import com.rickendy.serveflow.data.remote.api.NetworkClient
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URI

object SocketClient {

    private var socket: Socket? = null

    fun connect() {
        if (socket?.connected() == true) return

        val options = IO.Options.builder()
            .setTransports(arrayOf("polling", "websocket"))
            .build()

        socket = IO.socket(URI.create(NetworkClient.BASE_URL.trimEnd('/')), options)
        socket?.connect()
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
    }

    fun subscribeKitchen() {
        socket?.emit("subscribe:kitchen")
    }

    fun subscribeCashier() {
        socket?.emit("subscribe:cashier")
    }

    fun subscribeTable(tableId: Int) {
        socket?.emit("subscribe:table", tableId)
    }

    fun onOrderNew(callback: (JSONObject) -> Unit) {
        socket?.on("order:new") { args ->
            val data = args[0] as? JSONObject ?: return@on
            callback(data)
        }
    }

    fun onOrderUpdated(callback: (JSONObject) -> Unit) {
        socket?.on("order:updated") { args ->
            val data = args[0] as? JSONObject ?: return@on
            callback(data)
        }
    }

    fun onOrderItemAdded(callback: (JSONObject) -> Unit) {
        socket?.on("order:item_added") { args ->
            val data = args[0] as? JSONObject ?: return@on
            callback(data)
        }
    }

    fun onPaymentCompleted(callback: (JSONObject) -> Unit) {
        socket?.on("payment:completed") { args ->
            val data = args[0] as? JSONObject ?: return@on
            callback(data)
        }
    }

    fun off(event: String) {
        socket?.off(event)
    }

    fun isConnected(): Boolean = socket?.connected() == true
}