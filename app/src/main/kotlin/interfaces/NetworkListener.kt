package interfaces

import Json.InputConvertedData

interface NetworkListener {
    fun successConnect()
    fun receiveData(data: InputConvertedData)
}