package com.sturdyfishstick.brazzpw

import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import java.net.URLEncoder

class BrazzPW : MainAPI() {
    override var mainUrl             = "https://brazzpw.xyz"
    override var name                = "BrazzPW"
    override val hasMainPage         = true
    override var lang                = "en"
    override val hasQuickSearch      = false
    override val supportedTypes      = setOf(TvType.NSFW)
    override val vpnStatus           = VPNStatus.MightBeNeeded
    override val instantLinkLoading  = true

    override val mainPage = mainPageOf(
        "$mainUrl/a-z/"                        to "A to Z",
        "$mainUrl/baby-got-boobs/"             to "Baby got boobs",
        "$mainUrl/videos/sortby/beingwatched/" to "Being Watched",
        "$mainUrl/big-butts-like-it-big/"      to "Big butts like it big",
        "$mainUrl/big-tits-at-school/"         to "Big tits at school",
        "$mainUrl/big-tits-at-work/"           to "Big tits at work",
        "$mainUrl/big-tits-in-sports/"         to "Big tits in sports",
        "$mainUrl/big-tits-in-uniform/"        to "Big tits in uniform",
        "$mainUrl/big-wet-butts/"              to "Big wet butts",
        "$mainUrl/brazzers-exxtra/"            to "Brazzers Exxtra",
        "$mainUrl/cfnm/"                       to "Cfnm",
        "$mainUrl/day-with-pornstar/"          to "Day with pornstar",
        "$mainUrl/dirty-masseur/"              to "Dirty masseur",
        "$mainUrl/doctor-adventures/"          to "Doctor Adventures",
        "$mainUrl/hot-and-mean/"               to "Hot and mean",
        "$mainUrl/milfs-like-it-big/"          to "Milfs like it big",
        "$mainUrl/mom-in-control/"             to "Mom in control",
        "$mainUrl/mommy-got-boobs/"            to "Mommy got boobs",
        "$mainUrl/videos/sortby/views/"        to "Most Viewed",
        "$mainUrl/videos/"                     to "Newest Videos",
        "$mainUrl/pornstar-like-it-big/"       to "Pornstar like it big",
        "$mainUrl/real-wife-stories/"          to "Real wife Stories",
        "$mainUrl/teen-like-it-big/"           to "Teen like it big",
        "$mainUrl/videos/sortby/rating/"       to "Top Rated",
        "$mainUrl/zz-series/"                  to "Zz Series"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val pageUrl  = if (page <= 1) request.data else "${request.data}page/$page/"
        val document = app.get(pageUrl).document
        val home     = document.select("article.loop-video").mapNotNull { it.toCardResult() }

        return newHomePageResponse(
            HomePageList(request.name, home,
                isHorizontalImages = true
            ),
            hasNext = home.isNotEmpty()
        )
    }

    private fun Element.toCardResult(): SearchResponse? {
        val link      = selectFirst("a") ?: return null
        val href      = fixUrlNull(link.attr("href").ifEmpty { return null }) ?: return null
        val header    = selectFirst("header.entry-header span")?.text()?.substringAfter(":")?.trim()
        val title     = header?.ifEmpty { null } ?: link.attr("title").ifEmpty { null } ?: selectFirst("img")?.attr("alt")?.ifEmpty { null } ?: return null
        val img       = selectFirst("img")
        val posterUrl = fixUrlNull(img?.attr("data-src")?.ifEmpty { null } ?: img?.attr("src")?.ifEmpty { null })

        return newMovieSearchResponse(title, href, TvType.NSFW) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String, page: Int): SearchResponseList {
        val encoded  = URLEncoder.encode(query, "UTF-8")
        val url      = if (page <= 1) "$mainUrl/search/?s=$encoded" else "$mainUrl/search/page/$page/?s=$encoded"
        val document = app.get(url).document
        val results  = document.select("article.loop-video").mapNotNull { it.toCardResult() }

        return newSearchResponseList(results, hasNext = results.isNotEmpty())
    }

    override suspend fun quickSearch(query: String): List<SearchResponse>? = search(query, 1).items

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document

        val title         = document.selectFirst("h1.entry-title")?.text()?.trim()?.ifEmpty { null } ?: return null
        val poster        = fixUrlNull(document.selectFirst("meta[itemprop=thumbnailUrl]")?.attr("content")?.ifEmpty { null })
        val description     = document.selectFirst("div.video-description p")?.text()?.trim()?.ifEmpty { null }
        val year          = document.selectFirst("meta[itemprop=uploadDate]")?.attr("content")?.let { Regex("""\d+""").find(it)?.value?.toIntOrNull() }
        val tags          = document.select("div.tags-list a").map { it.text().trim() }.filter { it.isNotBlank() }.distinct()
        val scoreVal        = document.selectFirst("div.percentage")?.text()?.trim()?.removeSuffix("%")?.toDoubleOrNull()
        val duration        = document.selectFirst("meta[itemprop=duration]")?.attr("content")?.let { Regex("""(\d+)M""").find(it)?.groupValues?.get(1)?.toIntOrNull() }
        val recommendations = document.select("div.under-video-block article.loop-video").mapNotNull { it.toCardResult() }
        val actors          = document.select("div#video-actors a").map { it.text().trim() }.filter { it.isNotBlank() }.distinct()
        val iframe          = fixUrlNull(document.selectFirst("iframe[name=player]")?.attr("src")?.ifEmpty { null })

        return newMovieLoadResponse(title, url, TvType.NSFW, iframe ?: url) {
            this.posterUrl       = poster
            this.plot            = description
            this.year            = year
            this.tags            = tags
            this.score           = Score.from100(scoreVal?.toInt())
            this.duration        = duration
            this.recommendations = recommendations
            addActors(actors)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val playerUrl = if (data.contains("/player/")) data else fixUrlNull(
            app.get(data).document.selectFirst("iframe[name=player]")?.attr("src")?.ifEmpty { null }
        ) ?: return false
        val pageText  = app.get(playerUrl, referer = "$mainUrl/").text
        val m3u8Path  = Regex("""src:\s*"([^"]+)"""").find(pageText)?.groupValues?.get(1)?.ifEmpty { null } ?: return false
        val m3u8Url   = when {
            m3u8Path.startsWith("http") -> m3u8Path
            m3u8Path.startsWith("/")    -> "$mainUrl$m3u8Path"
            else                        -> "${playerUrl.substringBeforeLast("/")}/$m3u8Path"
        }

        callback.invoke(
            newExtractorLink(
                source = name,
                name   = name,
                url    = m3u8Url,
                type   = ExtractorLinkType.M3U8
            ) {
                this.referer = playerUrl
            }
        )
        return true
    }
}
