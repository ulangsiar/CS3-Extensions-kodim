package com.ulangsiar

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class TestPlugin: Plugin() {
    override fun load(context: Context) {
        registerMainAPI(TestProvider())
    }
}