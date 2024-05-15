package com.kodim

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.lagradost.cloudstream3.extractors.Filesim
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class Filmapik : MainAPI() {

    override var mainUrl = "https://tv.filmapik.ngo/"

    override var name = "Filmapik"
    override val hasMainPage = true
    override var lang = "id"
    override val supportedTypes = setOf( TvType.Movie, TvType.TvSeries )

    override val mainPage = mainPageOf(
        https://tv.filmapik.ngo/latest
            "$mainUrl/category/box-office/page/" to "Box Office",
            "$mainUrl/tvshows/page/" to "Drama Terbaru",
            "$mainUrl/latest/page/" to "Film Terbaru"
    )

    override suspend fun getMainPage( page: Int, request: MainPageRequest ): HomePageResponse {
        val document =  = if (page == 1) {
            app.get(request.data.removeSuffix("page/")).document
        } else {
            app.get(request.data + page).document
        }
        val home = document.select("article.tvshows,article.movies").mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h3")?.ownText()?.trim() ?: return null
        val href = this.selectFirst("a")!!.attr("href")
        val posterUrl = this.selectFirst("img")?.attr("src")
        val type = if (this.select(".movies") == null) TvType.Movie else TvType.TvSeries
        return if (type == TvType.TvSeries) {
            val episode = Regex("Ep.(\\d+)").find(document.select("span.quality")
                .text().trim())?.groupValues?.get(1).toString().toIntOrNull()
            newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                this.posterUrl = posterUrl
                addSub(episode)
            }
        } else {
            val quality = this.select("div.quality").text().trim()
            newMovieSearchResponse(title, href, TvType.Movie) {
                this.posterUrl = posterUrl
                addQuality(quality)
            }
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/search.php?s=$query").document
        return document.select("div.search-item").mapNotNull {
            val title = it.selectFirst("div.title  a").text().cleanText() ?: ""
            val href = it.selectFirst("a")?.attr("href") ?: return@mapNotNull null)
            val posterUrl = it.selectFirst("img.")?.attr("src"))
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

        val year = Regex("\\d, (\\d+)").find(
                document.select("div.content > div:nth-child(7) > h3").text().trim()
        )?.groupValues?.get(1).toString().toIntOrNull()
        val tvType = if (document.select("div.serial-wrapper")
                        .isNotEmpty()
        ) TvType.TvSeries else TvType.Movie
        val description = document.select("div.content > blockquote").text().trim()
        val trailer = document.selectFirst("div.action-player li > a.fancybox")?.attr("href")
        val rating =
                document.selectFirst("div.content > div:nth-child(6) > h3")?.text()?.toRatingInt()
        val actors =
                document.select("div.col-xs-9.content > div:nth-child(3) > h3 > a").map { it.text() }

        val recommendations = document.select("div.row.item-media").map {
            val recName = it.selectFirst("h3")?.text()?.trim().toString()
            val recHref = it.selectFirst(".content-media > a")!!.attr("href")
            val recPosterUrl =
                    fixUrl(it.selectFirst(".poster-media > a > img")?.attr("src").toString())
            newTvSeriesSearchResponse(recName, recHref, TvType.TvSeries) {
                this.posterUrl = recPosterUrl
            }
        }

        return if (tvType == TvType.TvSeries) {
            val episodes = document.select("div.episode-list > a:matches(\\d+)").map {
                val href = fixUrl(it.attr("href"))
                val episode = it.text().toIntOrNull()
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

    private suspend fun String.cleanText(): String {
        return this.replace("Nonton", "")
            .replace("Nonton\\sFilm", "")
            .replace("Sub\\sIndo\\sFilmapik", "")
            .replace("Subtitle\\sIndonesia\\sFilmapik", "")
    }

}
