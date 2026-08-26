import EventType.DataType
import EventType.SearchButtonType
import EventType.SearchButtonType.*
import Json.InputConvertedData
import androidx.lifecycle.ViewModel
import data.JsonConverter
import data.MouseData
import interfaces.CallSendData
import interfaces.NetworkListener
import interfaces.UIListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/*

*/



class MouseViewModel() : ViewModel(), UIListener, NetworkListener, CallSendData {
    private val connectionRepository = ConnectionRepository(this)
    private val _accessibilityEnabled = MutableStateFlow(false)
    private val mouseCommandProcessor = MouseCommandProcessor(this)

    private val _connectStatus = MutableStateFlow(SearchButtonType.IDLING)
    val connectStatus: StateFlow<SearchButtonType> = _connectStatus.asStateFlow()

    private val networkThread = NetworkThread()
    init{
        networkThread.start()
    }

    fun refreshAccessibilityState(enabled: Boolean) {
        _accessibilityEnabled.value = enabled
    }



    override fun ClickedSearchiButton() {
        when (_connectStatus.value) {
            IDLING -> {
                println("connect")
                networkThread.addTask { connectionRepository.connect() }
                changeConnectStatus(SEARCHING)
            }

            SEARCHING -> {
                connectionRepository.closeSearch()
                changeConnectStatus(STOP)
            }

            STOP -> {
                networkThread.addTask { connectionRepository.connect() }
                changeConnectStatus(SEARCHING)
            }

            SUCCESS -> {
                networkThread.addTask {
                    callSendData(DataType.SYSTEMEXIT,null)
                    connectionRepository.closeSearch()}
                changeConnectStatus(IDLING)
            }
        }
    }

    private fun changeConnectStatus(connectStatus: SearchButtonType) {
        this._connectStatus.value = connectStatus
    }

    override fun successConnect() {
        changeConnectStatus(SUCCESS)
        mouseCommandProcessor.serviceConnect()
    }

    override fun receiveData(data: InputConvertedData) {
        mouseCommandProcessor.receiveData(data)
    }

    override fun callSendData(dataType: DataType, data: MouseData?) {
        val jsonConverter = JsonConverter()
        connectionRepository.sendData(jsonConverter.dataConverter(dataType,data))
    }

    override fun errorOccurred(){
        mouseCommandProcessor.errorOccurred()
    }
}
