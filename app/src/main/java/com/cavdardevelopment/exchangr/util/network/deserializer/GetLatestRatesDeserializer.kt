package com.cavdardevelopment.exchangr.util.network.deserializer

import com.cavdardevelopment.exchangr.model.response.LatestRateResponse
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

class GetLatestRatesDeserializer: JsonDeserializer<LatestRateResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LatestRateResponse {
        if (json != null){
            val currencyNames: ArrayList<String> = ArrayList()
            val currencyRates: ArrayList<Double> = ArrayList()

            val jsonObject: JsonObject = json.asJsonObject
            val base: String = jsonObject["base"].asString
            val date: String = jsonObject["date"].asString
            val success: Boolean = jsonObject["success"].asBoolean
            val rates: JsonObject = jsonObject["rates"].asJsonObject

            for (i in 0 until rates.size()){
                currencyNames.add(rates.entrySet().elementAt(i).key)
                currencyRates.add(rates.entrySet().elementAt(i).value.asDouble)
            }

            return LatestRateResponse(base, date, success, currencyNames, currencyRates)
        }
        else {
            return LatestRateResponse("error", "error", false, arrayListOf(), arrayListOf())
        }

    }
}