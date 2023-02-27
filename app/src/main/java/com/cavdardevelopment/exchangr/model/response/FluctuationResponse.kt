package com.cavdardevelopment.exchangr.model.response

import com.google.gson.annotations.SerializedName

data class FluctuationResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("endDate")
    val endDate: String,
    @SerializedName("changeRate")
    val changeRate: Double
    )


//{
//    "motd": {
//        "msg": "If you or your company use this project or like what we doing, please consider backing us so we can continue maintaining and evolving this project.",
//        "url": "https://exchangerate.host/#/donate"
//    },
//    "success": true,
//    "fluctuation": true,
//    "start_date": "2023-01-10",
//    "end_date": "2023-02-13",
//    "rates": {
//        "TRY": {
//            "start_rate": 4.3,
//            "end_rate": 4.22,
//            "change": 0.08,
//            "change_pct": 0.02
//        }
//    }
//}
