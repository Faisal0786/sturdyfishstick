package com.sturdyfishstick.k8porner

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class K8Porner : MainAPI() {
    override var mainUrl = "https://8kporner.com"
    override var name = "8kporner"
    override val hasMainPage = true
    override var lang = "en"
    override val hasDownloadSupport = true
    override val hasChromecastSupport = true
    override val supportedTypes = setOf(TvType.NSFW)
    override val vpnStatus = VPNStatus.MightBeNeeded

    override val mainPage = mainPageOf(
        "studio/1pondo/" to "1Pondo",
        "studio/21naturals/" to "21Naturals",
        "studio/21sextury/" to "21Sextury",
        "studio/50plusmilfs/" to "50PlusMilfs",
        "studio/5kporn/" to "5KPorn",
        "studio/abbywinters/" to "AbbyWinters",
        "studio/abc/" to "ABC",
        "studio/ad4x/" to "AD4X",
        "studio/adriana-chechik/" to "Adriana Chechik",
        "studio/adultprime/" to "AdultPrime",
        "studio/adulttime/" to "AdultTime",
        "studio/alexis-tae/" to "Alexis Tae",
        "studio/alexlegend/" to "AlexLegend",
        "studio/allblackx/" to "AllBlackX",
        "studio/alt/" to "Alt",
        "studio/alterotic/" to "Alterotic",
        "studio/analized/" to "Analized",
        "studio/analonly/" to "AnalOnly",
        "studio/analvids/" to "AnalVids",
        "studio/anatomikmedia/" to "AnatomikMedia",
        "studio/angel-wicky/" to "Angel Wicky",
        "studio/angela-white/" to "Angela White",
        "studio/anilos/" to "Anilos",
        "studio/anna-claire-clouds/" to "Anna Claire Clouds",
        "studio/ara/" to "ARA",
        "studio/archangel/" to "ArchAngel",
        "studio/ark/" to "ARK",
        "studio/arousins/" to "Arousins",
        "studio/asa-akira/" to "Asa Akira",
        "studio/asia/" to "Asia",
    )

    private fun toResult(post: Element): SearchResponse? {
        val anchor = post.selectFirst("a") ?: return null
        val url = anchor.attr("href").takeIf { it.isNotBlank() } ?: return null
        val title = anchor.attr("title").takeIf { it.isNotBlank() } ?: anchor.text()
        if (title.isBlank()) return null
        
        val img = post.selectFirst("img")
        val imageUrl = img?.attr("data-src")?.takeIf { it.isNotBlank() }
            ?: img?.attr("src")?.takeIf { it.isNotBlank() && !it.startsWith("data:") }

        return newMovieSearchResponse(fixTitle(title), url, TvType.NSFW) {
            this.posterUrl = fixUrlNull(imageUrl)
        }
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = when {
            request.data.isEmpty() && page == 1 -> "$mainUrl/"
            request.data.isEmpty() -> "$mainUrl/page/$page/"
            page == 1 -> "$mainUrl/${request.data}"
            else -> "$mainUrl/${request.data}page/$page/"
        }
        val document = app.get(url).document
        // Common WordPress/Video theme selectors, adjust if layout differs
        val home = document.select("article, .post, .item, .video-item").mapNotNull { toResult(it) }
        val hasNext = document.select("link[rel=next], .pagination .next").isNotEmpty() && home.isNotEmpty()
        return newHomePageResponse(HomePageList(request.name, home, true), hasNext)
    }

    override suspend fun search(query: String, page: Int): SearchResponseList? {
        val document = app.get("$mainUrl/page/$page/?s=$query").document
        val searchResult: List<SearchResponse> =
            document.select("article, .post, .item, .video-item").mapNotNull { toResult(it) }
        return searchResult.toNewSearchResponseList()
    }

    override suspend fun load(url: String): LoadResponse? {
        val doc = app.get(url).document
        val title = doc.selectFirst("meta[property=og:title]")?.attr("content")?.trim().toString()
        val poster = fixUrlNull(doc.selectFirst("meta[property=og:image]")?.attr("content"))
        val description = doc.selectFirst("meta[property=og:description]")?.attr("content")?.trim()
        
        // Extracting iframe or video source URL
        val streamLink = doc.selectFirst("iframe[data-litespeed-src]")?.attr("data-litespeed-src")
            ?: doc.selectFirst("iframe[src]")?.attr("src") 
            ?: doc.selectFirst("video source")?.attr("src") 
            ?: ""

        return newMovieLoadResponse(title, url, TvType.NSFW, streamLink.ifEmpty { url }) {
            this.posterUrl = poster
            this.plot = description
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        if (data.isEmpty()) return false

        // Agar direct video stream link hai ya embed link, toh loadExtractor use karein
        return loadExtractor(data, referer = data, subtitleCallback, callback)
    }
}
