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
        "$mainUrl/" to "Home",
        "$mainUrl/videos/latest" to "Latest videos",
        "$mainUrl/trending" to "Trending",
        "$mainUrl/popular" to "Top videos",
        "$mainUrl/categories/ass-masterpiece" to "Ass Masterpiece",
        "$mainUrl/categories/ass-parade" to "Ass Parade",
        "$mainUrl/categories/ass-to-mouth" to "Ass To Mouth",
        "$mainUrl/categories/ass-to-pussy" to "Ass To Pussy",
        "$mainUrl/categories/ass-traffic" to "Ass Traffic",
        "$mainUrl/categories/asshole-fever" to "Asshole Fever",
        "$mainUrl/categories/athlete" to "Athlete",
        "$mainUrl/categories/athletic" to "Athletic",
        "$mainUrl/categories/atk-exotics" to "Atk Exotics",
        "$mainUrl/categories/atk-girlfriends" to "Atk Girlfriends",
        "$mainUrl/categories/atk-network" to "Atk Network",
        "$mainUrl/categories/atk-petites" to "Atk Petites",
        "$mainUrl/categories/audition" to "Audition",
        "$mainUrl/categories/aunt-nephew" to "Aunt & Nephew",
        "$mainUrl/categories/aunt-judys" to "Aunt Judy's",
        "$mainUrl/categories/aunt-judys-xxx" to "Aunt Judys Xxx",
        "$mainUrl/categories/aussie-ass" to "Aussie Ass",
        "$mainUrl/categories/australian" to "Australian",
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
