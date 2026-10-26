package com.sturdyfishstick.k8porner

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class K8PornerPlugin : Plugin() {
    override fun load(context: Context) {
        registerMainAPI(K8Porner())
    }
}
