package com.sturdyfishstick.xxxtime

import android.content.Context
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class XXXTimePlugin : Plugin() {
    override fun load(context: Context) {
        registerMainAPI(XXXTime())
    }
}
