package data

import EventType.DataType
import Json.InputConvertedData
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

class JsonConverter {
    private val mapper = ObjectMapper()

    fun dataConverter(dataType: DataType?, data: Any?): String? {
        try {
            val dataNode = mapper.valueToTree<JsonNode>(data)
            val sendData = InputConvertedData(dataType, dataNode)
            return mapper.writeValueAsString(sendData)
        } catch (e: JsonProcessingException) {
            return null
        }
    }
}