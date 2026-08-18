package Json

import EventType.DataType
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

class InputConvertedData(var dataType: DataType? = null, data: JsonNode? = null) {
    var data: JsonNode?

    init {
        this.data = mapper.valueToTree<JsonNode?>(data)
    }

    companion object {
        private val mapper = ObjectMapper()
    }
}