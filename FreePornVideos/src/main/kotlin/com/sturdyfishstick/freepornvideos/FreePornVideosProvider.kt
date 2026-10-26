package com.sturdyfishstick.freepornvideos

import com.lagradost.api.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors

class FreePornVideos : MainAPI() {
    override var mainUrl              = "https://www.freepornvideos.xxx"
    override var name                 = "Free Porn Videos"
    override val hasMainPage          = true
    override var lang                 = "en"
    override val hasQuickSearch       = false
    override val supportedTypes       = setOf(TvType.NSFW)
    

    override val mainPage = mainPageOf(
        "$mainUrl/latest-updates" to "All Videos",
        "$mainUrl/most-popular/week" to "Most Popular",
        "$mainUrl/categories/ass-to-mouth" to "Ass to Mouth",
        "$mainUrl/categories/bbw" to "BBW",
        "$mainUrl/categories/bdsm" to "BDSM",
        "$mainUrl/categories/big-ass" to "Big Ass",
        "$mainUrl/categories/big-cock" to "Big Cock",
        "$mainUrl/categories/big-natural-tits" to "Big Natural Tits",
        "$mainUrl/categories/big-tits" to "Big Tits",
        "$mainUrl/categories/blonde" to "Blonde",
        "$mainUrl/categories/blowjob" to "Blowjob",
        "$mainUrl/categories/brunette" to "Brunette",
        "$mainUrl/categories/creampie" to "Creampie",
        "$mainUrl/categories/cuckold" to "Cuckold",
        "$mainUrl/categories/cum-in-mouth" to "Cum in Mouth",
        "$mainUrl/categories/cumshot" to "Cumshot",
        "$mainUrl/categories/deepthroat" to "Deepthroat",
        "$mainUrl/categories/doggystyle" to "Doggystyle",
        "$mainUrl/categories/double-penetration" to "Double Penetration",
        "$mainUrl/categories/ebony" to "Ebony",
        "$mainUrl/categories/facial" to "Facial",
        "$mainUrl/categories/fisting" to "Fisting",
        "$mainUrl/categories/gangbang" to "Gangbang",
        "$mainUrl/categories/group-sex" to "Group Sex",
        "$mainUrl/categories/hairy" to "Hairy",
        "$mainUrl/categories/handjob" to "Handjob",
        "$mainUrl/categories/hardcore" to "Hardcore",
        "$mainUrl/categories/indian" to "Indian",
        "$mainUrl/categories/interracial" to "Interracial",
        "$mainUrl/categories/japanese" to "Japanese",
        "$mainUrl/categories/jav-uncensored" to "Jav Uncensored",
        "$mainUrl/categories/latina" to "Latina",
        "$mainUrl/categories/lesbian" to "Lesbian",
        "$mainUrl/categories/massage" to "Massage",
        "$mainUrl/categories/masturbation" to "Masturbation",
        "$mainUrl/categories/mature" to "Mature",
        "$mainUrl/categories/milf" to "MILF",
        "$mainUrl/categories/orgy" to "Orgy",
        "$mainUrl/categories/pawg" to "PAWG",
        "$mainUrl/categories/petite" to "Petite",
        "$mainUrl/categories/pissing" to "Pissing",
        "$mainUrl/categories/pov" to "POV",
        "$mainUrl/categories/pregnant" to "Pregnant",
        "$mainUrl/categories/public" to "Public",
        "$mainUrl/categories/redhead" to "Redhead",
        "$mainUrl/categories/rough-sex" to "Rough Sex",
        "$mainUrl/categories/schoolgirl" to "Schoolgirl",
        "$mainUrl/categories/shemale" to "Shemale",
        "$mainUrl/categories/squirting" to "Squirting",
        "$mainUrl/categories/step-fantasy" to "Step Fantasy",
        "$mainUrl/categories/teen" to "Teen",
        "$mainUrl/categories/threesome" to "Threesome",
        "$mainUrl/categories/toys" to "Toys",
        "$mainUrl/categories/vr-virtual-reality" to "VR (Virtual Reality)"
    )

   override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
      val document = app.get("${request.data}/$page/").document
    
    
    val home = document.select("div.item").mapNotNull {
        try {
            it.toSearchResult()
        } catch (e: Exception) {
            
            null
        }
    }

    return newHomePageResponse(
        list = HomePageList(
            name = request.name,
            list = home,
            isHorizontalImages = true
        ),
        hasNext = true
    )
}

    private fun Element.toSearchResult(): SearchResponse? {
        try {
            if (selectFirst("div.img iframe") != null) return null

            val linkElement = selectFirst("a.thumb_title") ?: return null
            val href = linkElement.attr("href").takeIf { it.isNotBlank() } ?: return null
            val title = linkElement.selectFirst("strong.title")?.text()?.takeIf { it.isNotBlank() } ?: return null

            var posterUrl: String? = null
            for (img in select("img.thumb")) {
                img.attr("data-src").takeIf { it.isNotBlank() }?.let {
                    posterUrl = it; break
                }
                img.attr("src").takeIf { it.isNotBlank() && !it.contains("data:image") }?.let {
                    posterUrl = it; break
                }
            }
            if (posterUrl == null) return null
            return newMovieSearchResponse(title, href, TvType.NSFW) {
                this.posterUrl = posterUrl
            }
        } catch (e: Exception) {
            Log.d("FPV", "Error in toSearchResult: ${e.message}")
            return null
        }
    }

    fun String?.createSlug(): String? {
        return this?.filter { it.isWhitespace() || it.isLetterOrDigit() }
            ?.trim()
            ?.replace("\\s+".toRegex(), "-")
            ?.lowercase()
    }

    override suspend fun search(query: String, page: Int): SearchResponseList {
        val searchquery= query.createSlug() ?:""
        val document = app.get("${mainUrl}/search/$searchquery/1/").document
        val aramaCevap = document.select("#custom_list_videos_videos_list_search_result_items > div.item").mapNotNull { it.toSearchResult() }

        return newSearchResponseList(aramaCevap, hasNext = true)
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document

        val full_title      = document.selectFirst("div.headline > h1")?.text()?.trim().toString()
        val last_index      = full_title.lastIndexOf(" - ")
        val raw_title       = if (last_index != -1) full_title.substring(0, last_index) else full_title
        val title           = raw_title.removePrefix("- ").trim().removeSuffix("-").trim()

        val poster          = fixUrlNull(document.selectFirst("[property='og:image']")?.attr("content"))
        val tags            = document.selectXpath("//div[contains(text(), 'Categories:')]/a").map { it.text() }
        val description     = document.selectXpath("//div[contains(text(), 'Description:')]/em").text().trim()
        val actors          = document.selectXpath("//div[contains(text(), 'Models:')]/a").map { it.text() }
        val recommendations = document.select("div#list_videos_related_videos_items div.item").mapNotNull { it.toSearchResult() }

        val year            = full_title.substring(full_title.length - 4).toIntOrNull()
        val rating          = document.selectFirst("div.rating span")?.text()?.substringBefore("%")?.trim()?.toFloatOrNull()?.div(10)?.toString()

        val raw_duration    = document.selectXpath("//span[contains(text(), 'Duration')]/em").text().trim()
        val duration_parts  = raw_duration.split(":")
        val duration        = when (duration_parts.size) {
            3 -> {
                val hours   = duration_parts[0].toIntOrNull() ?: 0
                val minutes = duration_parts[1].toIntOrNull() ?: 0

                hours * 60 + minutes
            }
            else -> {
                duration_parts[0].toIntOrNull() ?: 0
            }
        }

        return newMovieLoadResponse(title.removePrefix("- ").removeSuffix("-").trim(), url, TvType.NSFW, url) {
            this.posterUrl       = poster
            this.year            = year
            this.plot            = description
            this.tags            = tags
            this.recommendations = recommendations
            this.score           = Score.from10(rating)
            this.duration        = duration
            addActors(actors)
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
    val document = app.get(data).document
    
    document.select("video source").forEach { res ->
        val redirectUrl = res.attr("src")
        val quality = res.attr("label")
        
        try {
            
            val response = app.get(redirectUrl, allowRedirects = true)
            val finalUrl = response.url
            
            callback.invoke(
                newExtractorLink(
                    source = "FPV",
                    name = "FPV",
                    url = finalUrl,
                    
                    type = if (finalUrl.endsWith(".m3u8")) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO
                ){
                    this.referer = data
                    this.quality = getQualityFromName(quality)
                }
            )
        } catch (e: Exception) {
            Log.d("FPV", "Error getting final URL for $quality: ${e.message}")
        }
    }

    return true
}}
