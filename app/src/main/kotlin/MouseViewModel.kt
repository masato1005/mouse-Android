import EventType.SearchButtonType
import EventType.SearchButtonType.*
import Json.InputConvertedData
import androidx.lifecycle.ViewModel
import interfaces.NetworkListener
import interfaces.UIListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MouseViewModel() : ViewModel(), UIListener, NetworkListener {
    private val connectionRepository = ConnectionRepository(this)
    private val _accessibilityEnabled = MutableStateFlow(false)
    private val mouseCommandProcessor = MouseCommandProcessor()

    private val _connectStatus = MutableStateFlow(SearchButtonType.IDLING)
    val connectStatus: StateFlow<SearchButtonType> = _connectStatus.asStateFlow()
    fun refreshAccessibilityState(enabled: Boolean) {
        _accessibilityEnabled.value = enabled
    }

    override fun ClickedSearchiButton() {
        when (_connectStatus.value) {
            IDLING -> {
                Thread {
                    connectionRepository.connect()
                }.start()
                changeConnectStatus(SEARCHING)
            }

            SEARCHING -> {
                connectionRepository.closeSearch()
                changeConnectStatus(STOP)
            }

            STOP -> {
                Thread {
                    connectionRepository.connect()
                }.start()
                changeConnectStatus(SEARCHING)
            }

            SUCCESS -> {

            }
        }
    }

    private fun loop() {
        while (true) {
            connectionRepository.loop()
        }
    }

    private fun changeConnectStatus(connectStatus: SearchButtonType) {
        this._connectStatus.value = connectStatus
    }

    override fun successConnect() {
        changeConnectStatus(SUCCESS)
        mouseCommandProcessor.serviceConnect()
        loop()
    }

    override fun receiveData(data: InputConvertedData) {
        mouseCommandProcessor.receiveData(data)
    }
}
