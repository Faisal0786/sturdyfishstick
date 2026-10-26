package com.sturdyfishstick.freepornvideos

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.BasePlugin

@CloudstreamPlugin
class FreePornVideosProvider: BasePlugin() {
    override fun load() {
        registerMainAPI(FreePornVideos())
    }
}
