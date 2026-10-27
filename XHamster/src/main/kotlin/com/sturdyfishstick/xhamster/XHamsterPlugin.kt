package com.sturdyfishstick.xhamster

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@Suppress("ClassName")
@CloudstreamPlugin
class xHamsterPlugin: BasePlugin() {
    override fun load() {
        registerMainAPI(xHamster())
    }
}
