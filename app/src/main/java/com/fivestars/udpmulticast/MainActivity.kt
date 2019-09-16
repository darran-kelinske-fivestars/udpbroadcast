package com.fivestars.udpmulticast

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), CoroutineScope {

    private val buf: ByteArray = ByteArray(256)
    private val socket = DatagramSocket(4445)

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val job: Job = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        broadcast_button.setOnClickListener {
            broadcast("boop", InetAddress.getByName("255.255.255.255"))
        }
        run()

    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    @Throws(IOException::class)
    fun broadcast(
        broadcastMessage: String, address: InetAddress
    ) = launch(Dispatchers.IO) {
        socket.broadcast = true

        val data = broadcastMessage.toByteArray()

        val packet = DatagramPacket(data, data.size, address, 4445)
        socket.send(packet)
    }

    private fun run() = GlobalScope.launch {

        while (true) {
            var packet = DatagramPacket(buf, buf.size)
            socket.receive(packet)

            val address = packet.address
            val port = packet.port
            packet = DatagramPacket(buf, buf.size, address, port)
            val received = String(packet.data, 0, packet.length).trim()
            Log.e("Darran", received)

            if (received.startsWith("boop")) {
                broadcast("received message", InetAddress.getByName("255.255.255.255"))
            }
        }s
    }
}
