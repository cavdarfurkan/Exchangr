package com.cavdardevelopment.exchangr.model.response

import com.google.gson.annotations.SerializedName

data class ConvertRate(
    @SerializedName("date")
    val date: String,
    @SerializedName("historical")
    @Transient
    val historical: Boolean,
    @SerializedName("info")
    val info: Info,
    @SerializedName("query")
    val query: Query,
    @SerializedName("result")
    val result: Double,
    @SerializedName("success")
    val success: Boolean
)