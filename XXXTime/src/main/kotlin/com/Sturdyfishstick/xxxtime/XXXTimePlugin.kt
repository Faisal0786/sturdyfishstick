package com.sturdyfishstick.xxxtime

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class LuluStreamExtractor : Plugin() {
    override fun load() {
        registerMainAPI(XXXTime())
        registerExtractorAPI(LuluStream())
        registerExtractorAPI(LuluVid())
        registerExtractorAPI(LuluVdo())
        registerExtractorAPI(LuluPvp())
    }
}
