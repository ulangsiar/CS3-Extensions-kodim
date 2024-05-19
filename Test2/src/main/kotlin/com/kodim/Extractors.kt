package com.hexated

import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.apmap
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.base64Decode
import com.lagradost.cloudstream3.extractors.*
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.INFER_TYPE
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

open class Hownetwork : ExtractorApi() {
    override val name = "Hownetwork"
    override val mainUrl = "https://stream.hownetwork.xyz"
    override val requiresReferer = true

    override suspend fun getUrl(
            url: String,
            referer: String?,
            subtitleCallback: (SubtitleFile) -> Unit,
            callback: (ExtractorLink) -> Unit
    ) {
        val id = url.substringAfter("id=")
        val res = app.post(
                "$mainUrl/api.php?id=$id",
                data = mapOf(
                        "r" to "https://playeriframe.shop/",
                        "d" to "stream.hownetwork.xyz",
                ),
                referer = url,
                headers = mapOf(
                        "X-Requested-With" to "XMLHttpRequest"
                )
        ).parsedSafe<Sources>()

        res?.data?.map {
            callback.invoke(
                    ExtractorLink(
                            this.name,
                            this.name,
                            it.file,
                            url,
                            getQualityFromName(it.label),
                            INFER_TYPE
                    )
            )
        }

    }

    data class Sources(
            val data: ArrayList<Data>
    ) {
        data class Data(
                val file: String,
                val label: String?,
        )
    }
}

class Furher : Filesim() {
    override val name = "Furher"
    override var mainUrl = "https://furher.in"
}

open class Uplayer : ExtractorApi() {
    override val name = "Uplayer"
    override val mainUrl = "https://uplayer.xyz"
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val res = app.get(url,referer=referer).text
        val m3u8 = Regex("file:\\s*\"(.*?m3u8.*?)\"").find(res)?.groupValues?.getOrNull(1)
        M3u8Helper.generateM3u8(
            name,
            m3u8 ?: return,
            mainUrl
        ).forEach(callback)
    }

}

open class Kotakajaib : ExtractorApi() {
    override val name = "Kotakajaib"
    override val mainUrl = "https://kotakajaib.me"
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        app.get(url,referer=referer).document.select("ul#dropdown-server li a").apmap {
            loadExtractor(base64Decode(it.attr("data-frame")), "$mainUrl/", subtitleCallback, callback)
        }
    }

}

class FilemoonNl : Ridoo() {
    override val name = "Filemoon"
    override var mainUrl = "https://filemoon.sx"
    override val defaulQuality = Qualities.Unknown.value
}
class Doods : DoodLaExtractor() {
    override var name = "Doods"
    override var mainUrl = "https://doods.pro"
}

class Dutamovie21 : StreamSB() {
    override var name = "Dutamovie21"
    override var mainUrl = "https://dutamovie21.xyz"
}

class FilelionsTo : Filesim() {
    override val name = "Filelions"
    override var mainUrl = "https://filelions.to"
}

class FilelionsOn : Filesim() {
    override val name = "Filelions"
    override var mainUrl = "https://filelions.online"
}

class Lylxan : Filesim() {
    override val name = "Lylxan"
    override var mainUrl = "https://lylxan.com"
}

class Embedwish : Filesim() {
    override val name = "Embedwish"
    override var mainUrl = "https://embedwish.com"
}

class Likessb : StreamSB() {
    override var name = "Likessb"
    override var mainUrl = "https://likessb.com"
}

class DbGdriveplayer : Gdriveplayer() {
    override var mainUrl = "https://database.gdriveplayer.us"
}