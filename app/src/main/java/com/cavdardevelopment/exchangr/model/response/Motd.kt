package com.cavdardevelopment.exchangr.model.response


import com.google.gson.annotations.SerializedName

data class Motd(
    @SerializedName("msg")
    val msg: String,
    @SerializedName("url")
    val url: String
)