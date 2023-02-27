package com.cavdardevelopment.exchangr.util.listener

import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs


class AppBarStateChangeListener(private val onStateChangedListener: (appBarLayout: AppBarLayout, state: AppBarState, verticalOffset: Int) -> Unit)
    : AppBarLayout.OnOffsetChangedListener {

    private var currentState = AppBarState.IDLE

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        when {
            verticalOffset == 0 -> {
                if (currentState != AppBarState.EXPANDED) {
                    currentState = AppBarState.EXPANDED
                }
                onStateChangedListener(appBarLayout, currentState, verticalOffset)
            }
            abs(verticalOffset) >= appBarLayout.totalScrollRange -> {
                if (currentState != AppBarState.COLLAPSED) {
                    currentState = AppBarState.COLLAPSED
                }
                onStateChangedListener(appBarLayout, currentState, verticalOffset)
            }
            else -> {
                if (currentState != AppBarState.IDLE) {
                    currentState = AppBarState.IDLE
                }
                onStateChangedListener(appBarLayout, currentState, verticalOffset)
            }
        }
    }
}

enum class AppBarState {
    EXPANDED,
    COLLAPSED,
    IDLE
}