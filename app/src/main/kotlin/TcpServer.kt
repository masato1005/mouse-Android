import Json.InputConvertedData
import com.fasterxml.jackson.databind.ObjectMapper
import interfaces.NetworkListener
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class TcpServer(
    private val portNumber: Int,
    private val listener: NetworkListener
) {
    private var inData: BufferedReader? = null
    private var out: PrintWriter? = null
    private val mapper = ObjectMapper()
    private var socket: Socket? = null
    private var server: ServerSocket? = null

    fun makeServer() {
        try {
            val newServer = ServerSocket(portNumber)
            server = newServer
            println("接続待機中...")

            val newSocket = newServer.accept()
            socket = newSocket
            println("接続されました")

            inData = BufferedReader(
                InputStreamReader(newSocket.getInputStream())
            )
            out = PrintWriter(newSocket.getOutputStream(), true)
        } catch (e: IOException) {
            close()
        }
    }

    fun loop() {
        receive()
    }

    private fun receive() {
        try {
            val json = inData?.readLine()
            if (json != null) {
                val data: InputConvertedData = convertJsonToData(json)
                listener.receiveData(data)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun convertJsonToData(json: String): InputConvertedData {
        return mapper.readValue(json, InputConvertedData::class.java)
    }

    fun send(msg: String) {
        out?.println(msg)
    }

    fun close() {
        inData?.close()
        out?.close()
        socket?.close()
        server?.close()

        inData = null
        out = null
        socket = null
        server = null
    }
}
