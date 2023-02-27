package com.cavdardevelopment.exchangr.util.network.deserializer

import com.cavdardevelopment.exchangr.model.response.FluctuationResponse
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

class GetFluctuationDeserializer : JsonDeserializer<FluctuationResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): FluctuationResponse {
        if (json != null) {
            val jsonObject: JsonObject = json.asJsonObject
            val success: Boolean = jsonObject["success"].asBoolean
            val rate: JsonObject = jsonObject["rates"].asJsonObject.entrySet().first().value.asJsonObject
            val endDate: String = jsonObject["end_date"].asString
            val changeRate: Double = rate["change"].asDouble

            return FluctuationResponse(success, endDate, changeRate)
        }
        else {
            return FluctuationResponse(false, "end-date", 0.00)
        }

    }
}