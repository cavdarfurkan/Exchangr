package com.cavdardevelopment.exchangr.util.network.deserializer

import com.cavdardevelopment.exchangr.model.response.SupportedSymbolsResponse
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

class GetSupportedSymbolsDeserializer: JsonDeserializer<SupportedSymbolsResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): SupportedSymbolsResponse {
        if (json != null) {
            val descriptions: ArrayList<String> = ArrayList()
            val codes: ArrayList<String> = ArrayList()

            val jsonObject: JsonObject = json.asJsonObject
            val success: Boolean = jsonObject["success"].asBoolean
            val symbols: JsonObject = jsonObject["symbols"].asJsonObject

            for (i in 0 until symbols.size()) {
                descriptions.add(symbols.entrySet().elementAt(i).value.asJsonObject.entrySet().elementAt(0).value.asString)
                codes.add(symbols.entrySet().elementAt(i).value.asJsonObject.entrySet().elementAt(1).value.asString)
            }

            return SupportedSymbolsResponse(success, descriptions, codes)
        }
        else {
            return SupportedSymbolsResponse(false, arrayListOf(), arrayListOf())
        }
    }
}