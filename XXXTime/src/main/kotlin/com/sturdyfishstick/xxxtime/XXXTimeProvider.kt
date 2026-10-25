package com.sturdyfishstick.XXXTime

import com.lagradost.api.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer

    class XXXTime : MainAPI() {
    override var mainUrl = "https://xxxtime.video"
    override var name = "XXXTime"
    override val hasMainPage = true
    override var lang = "en"
    override val hasQuickSearch = false
    override val supportedTypes = setOf(TvType.NSFW)
    override val vpnStatus = VPNStatus.MightBeNeeded

    private val tag = "gizlikeyif_${name}"

    override val mainPage = mainPageOf(
        "${mainUrl}/channels.php?ch=Teamskeet" to "Teamskeet",
        "${mainUrl}/channels.php?ch=Cum4k" to "Cum4k",
        "${mainUrl}/channels.php?ch=Brazzers" to "Brazzers",
        "${mainUrl}/channels.php?ch=Mature4k" to "Mature4k",
        "${mainUrl}/channels.php?ch=Mom4k" to "Mom4k",
        "${mainUrl}/categories.php?c=Big+Ass" to "Big Ass",
        "${mainUrl}/categories.php?c=Redhead" to "Redhead",
        // Models List Added
        "${mainUrl}/models.php?m=Eve Sweet" to "Eve Sweet",
        "${mainUrl}/models.php?m=Yasmina Khan" to "Yasmina Khan",
        "${mainUrl}/models.php?m=Zory Miller" to "Zory Miller",
        "${mainUrl}/models.php?m=Raisa Bellini" to "Raisa Bellini",
        "${mainUrl}/models.php?m=Angela White" to "Angela White",
        "${mainUrl}/models.php?m=Kada Monroe" to "Kada Monroe",
        "${mainUrl}/models.php?m=Abigail Morris" to "Abigail Morris",
        "${mainUrl}/models.php?m=Luna Vital" to "Luna Vital",
        "${mainUrl}/models.php?m=Alanna Rae" to "Alanna Rae",
        "${mainUrl}/models.php?m=Lena The Plug" to "Lena The Plug",
        "${mainUrl}/models.php?m=Angel Youngs" to "Angel Youngs",
        "${mainUrl}/models.php?m=CamAzza" to "CamAzza",
        "${mainUrl}/models.php?m=Willow Ryder" to "Willow Ryder",
        "${mainUrl}/models.php?m=Mia Khalifa" to "Mia Khalifa",
        "${mainUrl}/models.php?m=Kayley Gunner" to "Kayley Gunner",
        "${mainUrl}/models.php?m=Hailey Rose" to "Hailey Rose",
        "${mainUrl}/models.php?m=Dee Williams" to "Dee Williams",
        "${mainUrl}/models.php?m=Lauren Phillips" to "Lauren Phillips",
        "${mainUrl}/models.php?m=ellie nova" to "ellie nova",
        "${mainUrl}/models.php?m=Aleksa Slus" to "Aleksa Slus",
        "${mainUrl}/models.php?m=Elly Clutch" to "Elly Clutch",
        "${mainUrl}/models.php?m=LuSinna69" to "LuSinna69",
        "${mainUrl}/models.php?m=Isa Bella" to "Isa Bella",
        "${mainUrl}/models.php?m=Coco Lovelock" to "Coco Lovelock",
        "${mainUrl}/models.php?m=Lula Stocch" to "Lula Stocch",
        "${mainUrl}/models.php?m=Carolina Guerrero" to "Carolina Guerrero",
        "${mainUrl}/models.php?m=Lucy Mochi" to "Lucy Mochi",
        "${mainUrl}/models.php?m=Alexa Chains" to "Alexa Chains",
        "${mainUrl}/models.php?m=Nia Bleu" to "Nia Bleu",
        "${mainUrl}/models.php?m=Roxie Sinner" to "Roxie Sinner",
        "${mainUrl}/models.php?m=Payton Preslee" to "Payton Preslee",
        "${mainUrl}/models.php?m=Octokuro" to "Octokuro",
        "${mainUrl}/models.php?m=jasmine sherni" to "jasmine sherni",
        "${mainUrl}/models.php?m=Kali Roses" to "Kali Roses",
        "${mainUrl}/models.php?m=Rossa Vaxx" to "Rossa Vaxx",
        "${mainUrl}/models.php?m=Ana Khalifa" to "Ana Khalifa",
        "${mainUrl}/models.php?m=aspen reign" to "aspen reign",
        "${mainUrl}/models.php?m=Vivienne Vo" to "Vivienne Vo",
        "${mainUrl}/models.php?m=Savannah Bond" to "Savannah Bond",
        "${mainUrl}/models.php?m=Melissa Stratton" to "Melissa Stratton",
        "${mainUrl}/models.php?m=Juniper Ren" to "Juniper Ren"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("${request.data}&page=$page").document
        val home = document.select("li.pure-u-1-3").mapNotNull { it.toMainPageResult() }

        return newHomePageResponse(list = HomePageList(request.name, home, true))
    }

    private fun Element.toMainPageResult(): SearchResponse? {
        val title = this.selectFirst("h3")?.text() ?: return null
        val href = fixUrlNull(this.selectFirst("a.video-thumb")?.attr("href")) ?: return null
        val posterUrl = this.selectFirst("a.video-thumb img")?.attr("data-src")

        val poster = if (posterUrl.isNullOrBlank()) {
            this.selectFirst("a.video-thumb img")?.attr("src")
        } else {
            posterUrl
        }

        return newMovieSearchResponse(title, href, TvType.NSFW) { this.posterUrl = poster }
    }

    override suspend fun search(query: String, page: Int): SearchResponseList {
        val document = app.get("${mainUrl}/search.php?s=$query&page=$page").document
        val searchAnswer = document.select("li.pure-u-1-3").mapNotNull { it.toMainPageResult() }

        return newSearchResponseList(searchAnswer, hasNext = true)
    }


    override suspend fun quickSearch(query: String): List<SearchResponse>? = search(query)


    override suspend fun load(url: String): LoadResponse? {
        Log.d(name, "Load aşaması: $url")
        val document = app.get(url).document

        val title = document.selectFirst("h1")?.text()?.trim() ?: return null
        val poster = fixUrlNull(document.selectFirst("meta[property=og:image]")?.attr("content"))
        val description =
            document.selectFirst("meta[property=og:description]")?.attr("content")?.trim()
        val year = document.selectFirst("div.extra span.C a")?.text()?.trim()?.toIntOrNull()
        val tags = document.select("div.pure-u-1-5 p a[href*=categories]").map { it.text() }
        val scoreText = document.selectFirst("span.dt_rating_vgs")?.text()?.trim()
        val duration =
            document.selectFirst("div.pure-u-1-5 p:contains(duration)")?.text()?.split(" ")?.first()
                ?.substringAfter("Duration:")?.trim()?.toIntOrNull()
        val recommendations = document.select("li.pure-u-1-3").mapNotNull { it.toMainPageResult() }
        val actors = document.select("div.pure-u-1-5 p a[href*=acctress]").map { Actor(it.text()) }
        val trailer = Regex("""embed\/(.*)\?rel""").find(document.html())?.groupValues?.get(1)
            ?.let { "https://www.youtube.com/embed/$it" }

        return newMovieLoadResponse(title, url, TvType.NSFW, url) {
            this.posterUrl = poster
            this.plot = description
            this.year = year
            this.tags = tags
            this.score = Score.from10(scoreText)
            this.duration = duration
            this.recommendations = recommendations
            addActors(actors)
            addTrailer(trailer)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        Log.d("Plugin", "data = $data")
        val document = app.get(data).document

        val allVideos = document.select("div.playerplace iframe")

        allVideos.forEach { video ->
            val link = video.attr("src")
            loadExtractor(link, "$mainUrl/", subtitleCallback, callback)
        }

        return true
    }
}
