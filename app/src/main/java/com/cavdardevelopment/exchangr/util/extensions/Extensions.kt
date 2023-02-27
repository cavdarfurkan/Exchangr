package com.cavdardevelopment.exchangr.util.extensions

import com.cavdardevelopment.exchangr.util.listener.AppBarStateChangeListener
import com.cavdardevelopment.exchangr.util.listener.AppBarState
import com.google.android.material.appbar.AppBarLayout
import java.text.SimpleDateFormat
import java.util.*

fun AppBarLayout.addOnStateChangedListener(onStateChangedListener: (appBarLayout: AppBarLayout, state: AppBarState, verticalOffset: Int) -> Unit)  {
    val listener = AppBarStateChangeListener(onStateChangedListener)
    addOnOffsetChangedListener(listener)
}

fun Int.normalize(min: Int, max: Int, newMin: Int, newMax: Int): Int {
    return (((this - min) * (newMax - newMin)) / (max - min)) + newMin
}

fun Double.normalize(min: Double, max: Double, newMin: Double, newMax: Double): Double {
    return (((this - min) * (newMax - newMin)) / (max - min)) + newMin
}

fun Float.normalize(min: Float, max: Float, newMin: Float, newMax: Float): Float {
    return (((this - min) * (newMax - newMin)) / (max - min)) + newMin
}

object Extensions {
    const val pattern = "yyyy-MM-dd"
    val timeZoneGMT: TimeZone = TimeZone.getTimeZone("GMT")
}

fun Calendar.format(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(this.time)
}

fun String.toCalendar(): Calendar {
    val formatter = SimpleDateFormat(Extensions.pattern, Locale.getDefault()).apply { timeZone = Extensions.timeZoneGMT }
    val formattedTime = formatter.parse(this)
    return Calendar.getInstance().apply {
        if (formattedTime != null) {
            time = formattedTime
        }
    }
}

fun Calendar.dayBefore(): Calendar {
    return this.also {
        it.add(Calendar.DAY_OF_YEAR, -1)
    }
}
