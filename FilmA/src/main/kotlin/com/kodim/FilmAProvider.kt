package com.kodim

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.extractors.Filesim
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class FilmAProvider : MainAPI() {

    override var mainUrl = base64Decode("aHR0cHM6Ly9maWxtYXBpay5maXQv")
    override var name = "FApik"
    override val hasMainPage = true
    override var lang = "en"
    override val supportedTypes = setOf(
            TvType.Movie,
            TvType.TvSeries,
            TvType.AsianDrama
    )

    override val mainPage = mainPageOf(
            "$mainUrl/latest/page/" to "Film Terbaru",
            "$mainUrl/tvshowss/page/" to "Drama Terbaru",
            "$mainUrl/category/box-office/page/" to "Box Office",
    )

    override suspend fun getMainPage(
            page: Int,
            request: MainPageRequest
    ): HomePageResponse {
        val document =  if (page == 1) {
            app.get(request.data.removeSuffix("page/")).document
        } else {
            app.get(request.data + page).document
        }
        val home = document.select("article.item movies, article.item tvshows").mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h3 > a")?.text()?.trim()?.toString()
        val href = fixUrl(this.selectFirst("a")!!.attr("href"))
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("src"))
        val type =
                if (this.selectFirst(".item") == null) TvType.Movie else TvType.TvSeries
        return if (type == TvType.TvSeries) {
            val episode = this.selectFirst("span > quality")?.text()?.filter { it.isDigit() }
                    ?.toIntOrNull()
            newTVSeriesSearchResponse(title, href, TvType.TvSeries) {
                this.posterUrl = posterUrl
                addSub(episode)
            }
        } else {
            val quality = this.select("span > quality").text().trim()
            newMovieSearchResponse(title, href, TvType.Movie) {
                this.posterUrl = posterUrl
                addQuality(quality)
            }
        }
    }

    private fun Element.toRecommendResult(): SearchResponse? {
        val title = this.selectFirst("h3")?.text()?.replace("Nonton Film", "")?.replace("Subtitle Indonesia", "")?.
                    replace("Nonton", "")?.replace("Sub Indo", "")?.trim()
        val href = this.selectFirst("a").attr("href")
        val posterUrl = this.selectFirst("img > src")
        return newMovieSearchResponse(title, href, TvType.Movie) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/?s=$query").document
        return document.select("div.result-item").mapNotNull {
            val title = it.selectFirst(".title > a")?.text()?.trim().toString()
            val href = fixUrl(it.selectFirst("a > href") ?: return@mapNotNull null)
            val posterUrl = fixUrlNull(it.selectFirst("img > src"))
            newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                this.posterUrl = posterUrl
            }
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val tvType = if (url.contains("/tvshows/")) TvType.TvSeries else TvType.Movie
        val title = 
            if (type == TvType.TvSeries) {
                document.selectFirst("h1:nth-child(1)")?.text()?.replace("Nonton Film", "")?.
                replace("Subtitle Indonesia Filmapik", "")?.trim()
            } else {
                document.selectFirst("h1")?.text()?.replace("Nonton", "")?.
                replace("Sub Indo Filmapik", "")?.trim()                
            }

        val poster = document.select("img[itemprop]")
        val tags = document.select("span.sgeneros > a").map { it.text() }
        val year = document.selectFirst("span")?.attr("release").text().trim()

        val description = document.selectFirst(".wp-content > p").text().trim()
        val rating = document.selectFirst("span.valor > b > strong")?.text()?.toRatingInt()
        val actors = document.select("span.tagline > a").map { it.text() }
        val recommendations = document.select("article").mapNotNull {
            it.toRecommendResult()

        return if (tvType == TvType.TvSeries) {
            val episodes = document.select("div.episodiotitle").map {
                val href = fixUrl(it.attr("href"))
                val episode = it.text().toIntOrNull()
                Episode(
                        href,
                        "Episode $episode",
                        episode,
                )
            }.reversed()
            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl = poster
                this.year = year
                this.plot = description
                this.tags = tags
                this.rating = rating
                addActors(actors)
            }
        } else {
            newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = poster
                this.year = year
                this.plot = description
                this.tags = tags
                this.rating = rating
                addActors(actors)
            }
        }
    }

    override suspend fun loadLinks(
            data: String,
            isCasting: Boolean,
            subtitleCallback: (SubtitleFile) -> Unit,
            callback: (ExtractorLink) -> Unit
    ): Boolean {

        val document = app.get(data).document
        document.select("ul#loadProviders > li").map {
            fixUrl(it.select("a").attr("href"))
        }.apmap {
            loadExtractor(it.getIframe(), "https://nganunganu.sbs", subtitleCallback, callback)
        }

        return true
    }

    private suspend fun String.getIframe() : String {
        return app.get(this, referer = "$seriesUrl/").document.select("div.embed iframe").attr("src")
    }

}
