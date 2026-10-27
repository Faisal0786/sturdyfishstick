package com.sturdyfishstick.uncutmaza

import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class UncutMaza : MainAPI() {
    override var mainUrl              = "https://uncutmaza.cc"
    override var name                 = "UncutMaza"
    override val hasMainPage          = true
    override var lang                 = "hi"
    override val hasQuickSearch       = false
    override val hasDownloadSupport   = true
    override val hasChromecastSupport = true
    override val supportedTypes       = setOf(TvType.NSFW)
    override val vpnStatus            = VPNStatus.MightBeNeeded

    override val mainPage = mainPageOf(
            "${mainUrl}/page/" to "Home",
            "${mainUrl}/category/kooku-originals-web-series/page/" to "Kooku",
            "${mainUrl}/category/ullu-originals-web-series/page/" to "Ullu",
            "${mainUrl}/category/flizmovies-originals-web-series/page/" to "Fliz movies",
            "${mainUrl}/category/uncutadda-web-series/page/" to "Uncutadda Webseries",
            "${mainUrl}/category/hotshots-web-series/page/" to "Hotshots",
            "${mainUrl}/category/hindi-sex-web-series/page/" to "Hindi Sex Web Series",
            "${mainUrl}/category/hindi-uncut-web-series-films/page/" to "Hindi Uncut Web Series & Films",
            "${mainUrl}/category/niks-indian-porn/page/" to "Niks Indian",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(request.data + page).document
        val home     = document.select("div.videos-list > article.post").mapNotNull { it.toSearchResult() }

        return newHomePageResponse(
            list    = HomePageList(
                name               = request.name,
                list               = home,
                isHorizontalImages = true
            ),
            hasNext = true
        )
    }

    private fun Element.toSearchResult(): SearchResponse {
        val title = fixTitle(this.select("a").attr("title"))
        val href = fixUrl(this.select("a").attr("href"))
        
        val img = this.selectFirst("img")
        var posterUrl = img?.attr("data-src")
            ?: img?.attr("data-lazy-src")
            ?: img?.attr("data-original")
            ?: img?.attr("src")

        if (posterUrl.isNullOrEmpty()) {
            val thumbDiv = this.selectFirst("div.post-thumbnail, div.post-thumbnail-container, div.thumb")
            val style = thumbDiv?.attr("style") ?: img?.attr("style") ?: ""
            if (style.contains("url(")) {
                posterUrl = style.substringAfter("url(").substringBefore(")").trim('\'', '"')
            }
        }

        return newMovieSearchResponse(title, href, TvType.NSFW) {
            this.posterUrl = fixUrlNull(posterUrl)
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val searchResponse = mutableListOf<SearchResponse>()

        for (i in 1..5) {
            val document = app.get("${mainUrl}/page/$i?s=$query").document

            val results = document.select("article.post").mapNotNull { it.toSearchResult() }

            if (!searchResponse.containsAll(results)) {
                searchResponse.addAll(results)
            } else {
                break
            }

            if (results.isEmpty()) break
        }

        return searchResponse
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document

        val title       = document.selectFirst("meta[property=og:title]")?.attr("content")?.trim().toString()
        val poster      = fixUrlNull(document.selectFirst("meta[property=og:image]")?.attr("content").toString())
        val description = document.selectFirst("meta[property=og:description]")?.attr("content")?.trim()

        return newMovieLoadResponse(title, url, TvType.NSFW, url) {
            this.posterUrl = poster
            this.plot      = description
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val document = app.get(data).document

        document.select("video, source").forEach { source ->
            val videoUrl = source.attr("src") ?: source.attr("data-src")
            if (!videoUrl.isNullOrEmpty()) {
                callback.invoke(
                    newExtractorLink(
                        source = this.name,
                        name = this.name,
                        url = fixUrl(videoUrl)
                    ) {
                        this.referer = mainUrl
                        this.quality = Qualities.Unknown.value
                    }
                )
            }
        }

        document.select("div.video-player, div.player-container, div.entry-content").forEach { element ->
            val contentUrl = element.selectFirst("meta[itemprop=contentURL]")?.attr("content")
                ?: element.selectFirst("iframe")?.attr("src")

            if (!contentUrl.isNullOrEmpty()) {
                val fixedUrl = fixUrl(contentUrl)
                if (fixedUrl.contains("http")) {
                    loadExtractor(fixedUrl, data, subtitleCallback, callback)
                }
            }
        }

        document.select("iframe").forEach { iframe ->
            val src = iframe.attr("src")
            if (src.isNotEmpty() && !src.contains("ads")) {
                loadExtractor(fixUrl(src), data, subtitleCallback, callback)
            }
        }

        return true
    }
}
