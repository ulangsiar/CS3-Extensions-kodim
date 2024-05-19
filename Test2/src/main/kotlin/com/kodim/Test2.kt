package com.kodim

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import org.jsoup.nodes.Element

class Test2 : MainAPI() {

    override var mainUrl = base64Decode("aHR0cHM6Ly90di5maWxtYXBpay5uZ28=")
    override var name = "Test2Provider"
    override val hasMainPage = true    
    override var lang = "id"
    override val hasQuickSearch = false
    override val hasDownloadSupport = true    
    override val supportedTypes = setOf( TvType.Movie, TvType.TvSeries )

    override val mainPage = mainPageOf(
            "$mainUrl/latest/page/" to "Film Terbaru",        
            "$mainUrl/tvshows/page/" to "Drama Terbaru",
    )

    override suspend fun getMainPage( 
        page: Int, 
        request: MainPageRequest 
    ): HomePageResponse {
        val document = if (page == 1) {
            app.get(request.data.removeSuffix("page/")).document
        } else {
            app.get(request.data + page).document
        }
        val home = document.select("article.movies,article.tvshows").mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h3 > a")?.text()?.trim() ?: return null
        val href = this.selectFirst("a")!!.attr("href")
        val posterUrl = this.selectFirst("img")?.attr("src")
        val type = if (this.select(".movies").isNotEmpty()) TvType.Movie else TvType.TvSeries
        return if (type == TvType.TvSeries) {
            val episode = Regex("Ep.(\\d+)").find(this.select("span.quality").text().trim().toIntOrNull())
            newAnimeSearchResponse(title, href, TvType.TvSeries) {
                this.posterUrl = posterUrl
                addSub(episode)
            }
        } else {
            val quality = this.select("span.quality").text().trim().toString()
            newMovieSearchResponse(title, href, TvType.Movie) {
                this.posterUrl = posterUrl
                addQuality(quality)
            }
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/?s=$query").document
        return document.select("div.result-item").mapNotNull {
            val title = it.selectFirst("div.title > a").text().cleanText()
            val href = it.selectFirst("a").attr("href")
            val posterUrl = it.selectFirst("img.")?.attr("src")
            newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                this.posterUrl = posterUrl
            }
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title = document.selectFirst("div.data h1").text().cleanText().trim()
        val poster = document.select("div.poster img").attr("src")
        val tags = document.select("span.sgeneros a").map { it.text() }
        val year = document.select("div.info-more:nth-child(3) span:nth-child(3) a").text().trim().toIntOrNull()

        val description = document.select("div[itemprop=description] p").text().cleanText()
        val rating = document.selectFirst("span.valor strong")?.text()?.toRatingInt()
        val actors = document.select("div.info-more:nth-child(2) span:nth-child(3) a").map { it.text() }

        val recommendations = document.select("div.srelacionados article").map {
            val recName = it.selectFirst("h3")?.text()?.trim().toString()
            val recHref = it.selectFirst("a")!!.attr("href")
            val recPosterUrl =
                    fixUrl(it.selectFirst("img")?.attr("src").toString())
            newTvSeriesSearchResponse(recName, recHref, TvType.TvSeries) {
                this.posterUrl = recPosterUrl
            }
        }

        val tvType = if(url.contains("/tvshows/")) TvType.TvSeries else TvType.Movie

        //To Be Continue
        return if (tvType == TvType.TvSeries) {
            val episodes = document.select("div.episodiotitle a:matches(\\d+)").map {
                val href = fixUrl(it.attr("href"))
                val episode = it.text().replace("EP", "").toIntOrNull()
                val season =
                        it.attr("href").substringAfter("season-").substringBefore("-").toIntOrNull()
                Episode(
                        href,
                        "Episode $episode",
                        season,
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
                this.recommendations = recommendations
            }
        } else {
            newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = poster
                this.year = year
                this.plot = description
                this.tags = tags
                this.rating = rating
                addActors(actors)
                this.recommendations = recommendations
            }
        }
    }

    override suspend fun loadLinks(
            data: String,
            isCasting: Boolean,
            subtitleCallback: (SubtitleFile) -> Unit,
            callback: (ExtractorLink) -> Unit
    ): Boolean {

        val document = app.get(data+"/play").document
        document.select("ul[ id=playeroptionsul]").map { it.select("li").attr("data-url") }
            .apmap { source ->
                    loadExtractor(source, "$mainUrl", subtitleCallback, callback)
        }
        return true
    }

    private fun String.cleanText(): String {
        return this.replace(Regex("(Nonton)|(Film)|(Sub\\sIndo\\sFilmapik)|(Subtitle\\sIndonesia\\sFilmapik)|(ALUR\\sCERITA\\s:\\s.\\s)"), "").trim()
    }

}
