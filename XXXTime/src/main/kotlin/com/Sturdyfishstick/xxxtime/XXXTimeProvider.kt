package com.sturdyfishstick.xxxtime

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
        // Channels & Categories
        "${mainUrl}/channels.php?ch=Teamskeet" to "Teamskeet",
        "${mainUrl}/channels.php?ch=Cum4k" to "Cum4k",
        "${mainUrl}/channels.php?ch=Brazzers" to "Brazzers",
        "${mainUrl}/channels.php?ch=Mature4k" to "Mature4k",
        "${mainUrl}/channels.php?ch=Mom4k" to "Mom4k",
        "${mainUrl}/categories.php?c=Big+Ass" to "Big Ass",
        "${mainUrl}/categories.php?c=Redhead" to "Redhead",

        // Models (A to Z)
        "${mainUrl}/model.php?p=Abigail-Morris" to "Abigail Morris",
        "${mainUrl}/model.php?p=Alanna-pow" to "Alanna Pow",
        "${mainUrl}/model.php?p=Aleksa-Mink" to "Aleksa Mink",
        "${mainUrl}/model.php?p=Alexa-Chains" to "Alexa Chains",
        "${mainUrl}/model.php?p=Ana-Khalifa" to "Ana Khalifa",
        "${mainUrl}/model.php?p=Angel-Youngs" to "Angel Youngs",
        "${mainUrl}/model.php?p=Angela-White" to "Angela White",
        "${mainUrl}/model.php?p=aspen-reign" to "aspen reign",
        "${mainUrl}/model.php?p=Comatozze" to "Comatozze",
        "${mainUrl}/model.php?p=Carolina-Guerrero" to "Carolina Guerrero",
        "${mainUrl}/model.php?p=Coco-Lovelock" to "Coco Lovelock",
        "${mainUrl}/model.php?p=Dee-Williams" to "Dee Williams",
        "${mainUrl}/model.php?p=ellie-nova" to "ellie nova",
        "${mainUrl}/model.php?p=Elly-Clutch" to "Elly Clutch",
        "${mainUrl}/model.php?p=Eve-Sweet" to "Eve Sweet",
        "${mainUrl}/model.php?p=Hailey-Rose" to "Hailey Rose",
        "${mainUrl}/model.php?p=Isa-Bella" to "Isa Bella",
        "${mainUrl}/model.php?p=jasmine-sherni" to "jasmine sherni",
        "${mainUrl}/model.php?p=Juniper-Ren" to "Juniper Ren",
        "${mainUrl}/model.php?p=Koda-Monroe" to "Koda Monroe",
        "${mainUrl}/model.php?p=Kali-Roses" to "Kali Roses",
        "${mainUrl}/model.php?p=Kayley-Gunner" to "Kayley Gunner",
        "${mainUrl}/model.php?p=Lauren-Phillips" to "Lauren Phillips",
        "${mainUrl}/model.php?p=Lena-The-Plug" to "Lena The Plug",
        "${mainUrl}/model.php?p=LuSirena69" to "LuSirena69",
        "${mainUrl}/model.php?p=Lucy-Mochi" to "Lucy Mochi",
        "${mainUrl}/model.php?p=Lula-Stocch" to "Lula Stocch",
        "${mainUrl}/model.php?p=Luna-Doll" to "Luna Doll",
        "${mainUrl}/model.php?p=Melissa-Stratton" to "Melissa Stratton",
        "${mainUrl}/model.php?p=Mia-Khalifa" to "Mia Khalifa",
        "${mainUrl}/model.php?p=Nia-Bleu" to "Nia Bleu",
        "${mainUrl}/model.php?p=Octokuro" to "Octokuro",
        "${mainUrl}/model.php?p=Payton-Preslee" to "Payton Preslee",
        "${mainUrl}/model.php?p=Raissa-Bellini" to "Raissa Bellini",
        "${mainUrl}/model.php?p=Rossa-Vaxx" to "Rossa Vaxx",
        "${mainUrl}/model.php?p=Roxie-Sinner" to "Roxie Sinner",
        "${mainUrl}/model.php?p=Savannah-Bond" to "Savannah Bond",
        "${mainUrl}/model.php?p=Vivienne-Vo" to "Vivienne Vo",
        "${mainUrl}/model.php?p=Willow-Ryder" to "Willow Ryder",
        "${mainUrl}/model.php?p=Yasmina-Khan" to "Yasmina Khan",
        "${mainUrl}/model.php?p=Zoey-Uso" to "Zoey Uso"
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
