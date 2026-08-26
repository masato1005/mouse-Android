import interfaces.NetworkListener

class ConnectionRepository(private val listener: NetworkListener) {
    private val portNumber: Int = 5000
    private var udp: UdpServer? = null
    private var tcp: TcpServer? = null

    fun connect() {
        println("connect2")
        closeSearch()

        val udpServer = UdpServer(portNumber)
        udp = udpServer
        udpServer.makeServer()

        if (udp != null) {
            val tcpServer = TcpServer(portNumber, listener)
            tcp = tcpServer
            tcp?.makeServer()
            tcp?.connecting = true
            tcp?.startReceiveThread()
            listener.successConnect()
        }
    }

    fun sendData(sendData: String?){
        tcp?.send(sendData)
    }



    fun closeSearch() {
        udp?.close()
        tcp?.close()
        udp = null
        tcp = null
    }
}
