package interfaces

import EventType.DataType
import data.MouseData

interface CallSendData {
    fun callSendData(dataType: DataType, data: MouseData?)
}