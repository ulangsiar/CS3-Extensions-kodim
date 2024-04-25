package com.kodim

import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.extractors.Filesim
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.M3u8Helper
import com.lagradost.cloudstream3.utils.getQualityFromName

open class Emturbovid : ExtractorApi() {
    override val name = "Emturbovid"
    override val mainUrl = "https://emturbovid.com"
    override val requiresReferer = true

    override suspend fun getUrl(
            url: String,
            referer: String?,
            subtitleCallback: (SubtitleFile) -> Unit,
            callback: (ExtractorLink) -> Unit
    ) {
        val response = app.get(url, referer = referer)
        val m3u8 = Regex("[\"'](.*?master\\.m3u8.*?)[\"']").find(response.text)?.groupValues?.getOrNull(1)
        M3u8Helper.generateM3u8(
                name,
                m3u8 ?: return,
                mainUrl
        ).forEach(callback)
    }

}


class Furher : Filesim() {
    override val name = "Furher"
    override var mainUrl = "https://furher.in"
}