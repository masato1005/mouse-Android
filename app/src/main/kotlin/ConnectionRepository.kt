import interfaces.NetworkListener

class ConnectionRepository(private val listener: NetworkListener) {
    private val portNumber: Int = 5000
    private var udp: UdpServer? = null
    private var tcp: TcpServer? = null

    fun connect() {
        closeSearch()

        val udpServer = UdpServer(portNumber)
        udp = udpServer
        udpServer.makeServer()

        if (udp != null) {
            val tcpServer = TcpServer(portNumber, listener)
            tcp = tcpServer
            tcpServer.makeServer()
            listener.successConnect()
        }
    }

    fun loop() {
        tcp?.loop()
    }


    fun closeSearch() {
        udp?.close()
        tcp?.close()
        udp = null
        tcp = null
    }
}
