import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException

class UdpServer(
    private val portNumber: Int
) {
    private var socket: DatagramSocket? = null

    fun makeServer() {
        val newSocket = DatagramSocket(portNumber)
        socket = newSocket

        val buffer = ByteArray(1024)
        val packet = DatagramPacket(buffer, buffer.size)
        waitMessage(newSocket, packet)
    }

    private fun waitMessage(
        socket: DatagramSocket,
        packet: DatagramPacket
    ) {
        println("待機中...")

        while (!socket.isClosed) {
            try {
                socket.receive(packet)
            } catch (e: SocketException) {
                if (socket.isClosed) break
                throw e
            }

            val message = String(packet.data, 0, packet.length)
            println("受信: $message")

            when (message) {
                "DISCOVER_SERVER" -> {
                    sendMessage(socket, packet)
                    break
                }

                "STOP" -> break
            }
        }

        close()
    }

    private fun sendMessage(
        socket: DatagramSocket,
        receivedPacket: DatagramPacket
    ) {
        val response = "SERVER_HERE".toByteArray()
        val reply = DatagramPacket(
            response,
            response.size,
            receivedPacket.address,
            receivedPacket.port
        )

        socket.send(reply)
        println("返信しました")
    }

    fun sendStopMessage() {
        val currentSocket = socket ?: return
        if (currentSocket.isClosed) return

        val message = "STOP".toByteArray()
        val address = InetAddress.getByName("localhost")
        val stopMessage = DatagramPacket(
            message,
            message.size,
            address,
            portNumber
        )

        try {
            currentSocket.send(stopMessage)
        } catch (e: IOException) {
            if (!currentSocket.isClosed) throw e
        }
    }

    fun close() {
        socket?.close()
        socket = null
    }
}
