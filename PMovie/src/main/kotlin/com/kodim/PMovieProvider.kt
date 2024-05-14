package com.kodim

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.lagradost.cloudstream3.mvvm.safeApiCall
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import org.jsoup.nodes.Element

class PMovieProvider : MainAPI() {

    override var mainUrl = base64Decode("aHR0cHM6Ly93dzc5LnBlbmN1cmltb3ZpZS5hdXRvcy8=")
    override var name = "PenCURI"
    override val hasMainPage = true
    override var lang = "ms"
    override val supportedTypes = setOf(
        TvType.Movie,
        TvType.TvSeries
    )

    override val mainPage = mainPageOf(
        "$mainUrl/movies/page/" to "Latest Movies",
        "$mainUrl/series/page/" to "Latest TV Series",
        "$mainUrl/episode/page/" to "Latest Episode",        
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
        val home = document.select("div.ml-item").mapNotNull {
            it.toSearchResult()
        }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h2")?.text()?.trim() ?: return null
        val href = fixUrl(this.selectFirst("a")?.attr("href").toString())
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("data-original"))
        val type = if (this.selectFirst("span.mli-eps") == null) TvType.Movie else TvType.TvSeries
        return if (type == TvType.TvSeries) {
            val episode = this.selectFirst("span.mli-eps")?.text()?.filter { it.isDigit() } ?.toIntOrNull()
            newAnimeSearchResponse(title, href, TvType.TvSeries) {
                this.posterUrl = posterUrl
                addSub(episode)
            }
        } else {
            // output = huruf besar, replace -
            val quality = getQualityFromString(this.selectFirst("span.mli-quality").text().replace("-", "").toString())
            //val quality = getQualityFromString(this.selectFirst("span.mli-quality").text().toString())
            newMovieSearchResponse(title, href, TvType.Movie) {
                this.posterUrl = posterUrl
                addQuality(quality)
            }           
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/?s=$query").document

        return document.select("div.ml-item").mapNotNull {
            it.toSearchResult()
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title = document.selectFirst("div.mvic-desc h3")?.text()?.trim() ?: return null
        val poster = fixUrlNull(document.selectFirst("div.thumb.mvic-thumb img")?.attr("src"))
        val tags = document.select("div.mvici-left p:nth-child(1) a").map { it.text() }
        val year = document.select("div.mvici-right p:nth-child(3) a").text().trim()
            .toIntOrNull()

        val tvType = if (document.selectFirst("div.les-content")
                ?.select("a")?.size!! > 1 || document.selectFirst("ul.idTabs li strong")?.text()
                ?.contains(Regex("(?i)(EP\\s?[0-9]+)|(episode\\s?[0-9]+)")) == true
        ) TvType.TvSeries else TvType.Movie

        val description = document.selectFirst("p.f-desc")?.text()?.trim()
        val trailer = fixUrlNull(document.select("iframe#iframe-trailer").attr("src"))
        val rating = document.select("div.mvici-right > div.imdb_r span").text().toRatingInt()
        val actors = document.select("div.mvici-left p:nth-child(3) a").map { it.text() }
        val recommendations = document.select("div.ml-item").mapNotNull {
            it.toSearchResult()
        }

        return if (tvType == TvType.TvSeries) {
            val episodes = if (document.selectFirst("div.les-title strong")?.text().toString()
                    .contains(Regex("(?i)EP\\s?[0-9]+|Episode\\s?[0-9]+"))
            ) {
                document.select("ul.idTabs li").map {
                    val id = it.select("a").attr("href")
                    Episode(
                        data = fixUrl(document.select("div$id iframe").attr("src")),
                        name = it.select("strong").text().replace("Server Ep", "Episode")
                    )
                }

            } else {
                document.select("div.les-content a").map {
                    Episode(
                        data = it.attr("href"),
                        name = it.text().replace("Server Ep", "Episode").trim(),
                    )
                }
            }

            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl = poster
                this.year = year
                this.plot = description
                this.tags = tags
                this.rating = rating
                addActors(actors)
                this.recommendations = recommendations
                addTrailer(trailer)
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
                addTrailer(trailer)
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {

        if (data.startsWith(mainUrl)) {
            app.get(data).document.select("div.movieplay iframe").map { fixUrl(it.attr("data-src")) }
                .apmap { source ->
                    safeApiCall {
                        when {
                            source.startsWith("https://membed.net") -> app.get(
                                source,
                                referer = "$mainUrl/"
                            ).document.select("ul.list-server-items li")
                                .apmap {
                                    loadExtractor(
                                        it.attr("data-video").substringBefore("=https://msubload"),
                                        "$mainUrl/",
                                        subtitleCallback,
                                        callback
                                    )
                                }
                            else -> loadExtractor(source, "$mainUrl/", subtitleCallback, callback)
                        }
                    }
                }
        } else {

            loadExtractor(data, "$mainUrl/", subtitleCallback, callback)
        }
        return true
    }
}