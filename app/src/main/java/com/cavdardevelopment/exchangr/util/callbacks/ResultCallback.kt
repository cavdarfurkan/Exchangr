package com.cavdardevelopment.exchangr.util.callbacks

interface ResultCallback {
    fun onResult(success: Boolean, result: String? = null)

    fun onFailed(e: Exception?) {
        e?.printStackTrace()
    }
}