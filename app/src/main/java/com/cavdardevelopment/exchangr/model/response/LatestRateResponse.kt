package com.cavdardevelopment.exchangr.model.response


import com.google.gson.annotations.SerializedName

data class LatestRateResponse(
    @SerializedName("base")
    val base: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("currencyNames")
    val currencyNames: ArrayList<String>,
    @SerializedName("currencyRates")
    val currencyRates: ArrayList<Double>
    )


//{
//    "motd": {
//    "msg": "If you or your company use this project or like what we doing, please consider backing us so we can continue maintaining and evolving this project.",
//    "url": "https://exchangerate.host/#/donate"
//},
//    "success": true,
//    "base": "USD",
//    "date": "2023-02-17",
//    "rates": {
//    "TRY": 18.93
//}
//}