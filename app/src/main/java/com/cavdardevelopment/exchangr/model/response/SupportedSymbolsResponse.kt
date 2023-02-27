package com.cavdardevelopment.exchangr.model.response

import com.google.gson.annotations.SerializedName

data class SupportedSymbolsResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("descriptions")
    val descriptions: ArrayList<String>,
    @SerializedName("codes")
    val codes: ArrayList<String>
)
