package com.sturdyfishstick.xvideos

import android.content.Context
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class xVideosPlugin: Plugin() {
    override fun load() {
        registerMainAPI(xVideos())
        registerExtractorAPI(xVideosExtractor())
    }
}
