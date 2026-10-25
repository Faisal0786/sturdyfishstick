package com.sturdyfishstick.xmoviesforyou

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class WatchPornPlugin : Plugin() {
    override fun load(context: Context) {
        registerMainAPI(XMoviesForYou())
    }
}
