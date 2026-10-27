package com.sturdyfishstick.xhamster

import com.lagradost.api.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Document
import com.fasterxml.jackson.module.kotlin.readValue

@Suppress("ClassName")
class xHamster : MainAPI() {
    override var mainUrl          = "https://xhamster2.com"
    override var name             = "xHamster"
    override val hasMainPage      = true
    override var lang             = "en"
    override val hasQuickSearch   = false
    override val supportedTypes   = setOf(TvType.NSFW)
    override val vpnStatus        = VPNStatus.MightBeNeeded

    override val mainPage = mainPageOf(
        // Default Main Pages
        "${mainUrl}/newest/" to "Newest",
        "${mainUrl}/most-viewed/weekly/" to "Weekly Most Viewed",
        "${mainUrl}/most-viewed/monthly/" to "Monthly Most Viewed",
        "${mainUrl}/most-viewed/" to "All Time Most Viewed",
        "${mainUrl}/4k/" to "4K",
        "${mainUrl}/hd/2?quality=1080p" to "1080p",
        "${mainUrl}/channels/faphouse" to "FapHouse",
        "${mainUrl}/channels/oopsfamily" to "OopsFamily",
        "${mainUrl}/channels/adult-time" to "Adult Time",
        "${mainUrl}/channels/inkasex" to "InkaSex",
        "${mainUrl}/channels/brattysis" to "BrattySis",
        "${mainUrl}/channels/nublie-films" to "Nublie Films",
        "${mainUrl}/channels/brazzers" to "Brazzers",
        "${mainUrl}/channels/family-strokes" to "Family Strokes",
        "${mainUrl}/channels/pure-taboo" to "Pure Taboo",
        "${mainUrl}/channels/niks-indian" to "NIKS INDIAN",
        "${mainUrl}/channels/my-pervy-family" to "My pervy family",
        "${mainUrl}/channels/blacked-raw" to "Blacked Raw",
        "${mainUrl}/channels/vip4k" to "VIP4K",
        "${mainUrl}/channels/dogfart-network" to "Dogfart Network",
        "${mainUrl}/channels/sexmex" to "SEXMEX",
        "${mainUrl}/channels/freeuse" to "FreeUse",
        "${mainUrl}/channels/freeuse-milf" to "FreeUse Milf",
        "${mainUrl}/channels/mommys-boy" to "Mommys Boy",
        "${mainUrl}/channels/mmv-films" to "MMV Films",
        "${mainUrl}/channels/deeper" to "Deeper",
        "${mainUrl}/channels/qombol" to "Qombol",
        "${mainUrl}/channels/daddy4k" to "Daddy4K",
        "${mainUrl}/channels/shame4k" to "Shame4K",
        "${mainUrl}/channels/family-swap" to "Family Swap",
        "${mainUrl}/channels/filthy-kings" to "Filthy Kings",
        "${mainUrl}/channels/vixen-plus" to "Vixen Plus",
        "${mainUrl}/channels/digital-playground" to "Digital Playground",
        "${mainUrl}/channels/mommy4k" to "Mommy4K",
        "${mainUrl}/channels/milfy" to "Milfy",
        "${mainUrl}/channels/indian-milf-porn" to "Indian MilF Porn",
        "${mainUrl}/channels/indian-porn-babe" to "Indian Porn Babe",
        "${mainUrl}/channels/indian-real-porn" to "Indian Real Porn",
        "${mainUrl}/channels/naughty-america" to "Naughty America",
        "${mainUrl}/search/bhabhi" to "Bhabhi",
        "${mainUrl}/search/neha+bhabhi" to "Neha bhabhi",
        "${mainUrl}/search/indian+new+sex" to "Indian New Sex",
        "${mainUrl}/search/full+length" to "Full-Length",
        "${mainUrl}/search/indian" to "Indian",
        "${mainUrl}/search/mami" to "Mami",
        "${mainUrl}/search/chachi" to "Chachi",
        "${mainUrl}/search/desi+mummy" to "Desi Mummy",
        "${mainUrl}/search/indian+mummy" to "Indian Mummy",
        "${mainUrl}/search/indian+chachi" to "Indian Chachi"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val targetUrl = if (request.data.contains("/search/")) {
            "${request.data}?page=$page&x_platform_switch=desktop&geo=us"
        } else {
            "${request.data}/$page?geo=us"
        }

        val document = app.get(
            targetUrl,
            cookies = mapOf("video_titles_translation" to "0")
        ).document

        Log.d(name, "$page ${request.data}")

        val home = document.select("div.thumb-list div.thumb-list__item, div.thumb-list__item, div.video-thumb")
            .mapNotNull { it.toSearchResult() }

        Log.d(name, "${home.size}")

        return newHomePageResponse(
            list    = HomePageList(
                name               = request.name,
                list               = home,
                isHorizontalImages = true
            ),
            hasNext = home.isNotEmpty()
        )
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val titleElement = this.selectFirst("a.video-thumb-info__name") ?: this.selectFirst("a.video-thumb__name") ?: return null
        val title        = titleElement.attr("title").ifEmpty { titleElement.text().trim() }.ifEmpty { return null }
        val href         = fixUrl(titleElement.attr("href"))

        val imgElement   = this.selectFirst("img")
        val rawPoster    = imgElement?.attr("srcset")?.substringBefore(" ")?.ifEmpty { null }
            ?: imgElement?.attr("data-src")?.ifEmpty { null }
            ?: imgElement?.attr("src")?.ifEmpty { null }
        val posterUrl    = fixUrlNull(rawPoster)

        Log.d(name, "$title $href")

        return newMovieSearchResponse(title, href, TvType.NSFW) { this.posterUrl = posterUrl }
    }

    override suspend fun search(query: String, page: Int): SearchResponseList {
        val document =
            app.get(
                "${mainUrl}/search/${
                    query.replace(
                        " ",
                        "+"
                    )
                }/?page=$page&x_platform_switch=desktop&geo=us",
                cookies = mapOf("video_titles_translation" to "0")
            ).document

        val aramaCevap = document.select("div.thumb-list div.thumb-list__item, div.video-thumb")
            .mapNotNull { it.toSearchResult() }

        return newSearchResponseList(aramaCevap, hasNext = aramaCevap.isNotEmpty())
    }

    override suspend fun load(url: String): LoadResponse {
        val document =
            app.get("${url}?geo=us", cookies = mapOf("video_titles_translation" to "0")).document

        val title = document.selectFirst("div.with-player-container h1")?.text()?.trim().toString()
        val poster = fixUrlNull(
            document.selectFirst("div.xp-preload-image")?.attr("style")?.substringAfter("https:")
                ?.substringBefore("\');")
        )
        val description = document.selectFirst("div.controls-info div.ab-info p")?.text()?.trim()
            ?.replace("\\s+".toRegex(), " ")

        val actors = document.select("a.entity-author-container__name").map { aTag ->
            val name = aTag.selectFirst("span")?.text()?.trim() ?: ""
            val image =
                aTag.selectFirst("img")?.attr("src") ?: aTag.selectFirst("img")?.attr("data-src")
            Actor(name, image)
        }

        val tags =
            document.select("div[data-role='video-tags-list'] a[href*='/categories/'], div[data-role='video-tags-list'] a[href*='/tags/']")
                .map { it.text().trim() }

        val recommendations = document.select("div[data-role='related-item']").mapNotNull {
            val name = it.selectFirst("a.video-thumb-info__name")?.text() ?: return@mapNotNull null
            val link =
                it.selectFirst("a[data-role='thumb-link']")?.attr("href") ?: return@mapNotNull null
            val thumb =
                it.selectFirst("img")?.attr("src") ?: it.selectFirst("img")?.attr("data-src")

            newMovieSearchResponse(name, link, TvType.NSFW) {
                this.posterUrl = thumb
            }
        }

        return newMovieLoadResponse(title, url, TvType.NSFW, url) {
            this.posterUrl = poster
            this.plot = description
            this.tags = tags
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
        var foundLinks = false
        val sourceName = name

        val document: Document = try {
            app.get("${data}?geo=us", cookies = mapOf("video_titles_translation" to "0")).document
        } catch (e: Exception) {
            Log.e(sourceName, "${e.message}")
            return false
        }

        val preloadLinks = document.select("link[rel=preload][as=fetch]")
        preloadLinks.forEach { link ->
            val href = link.attr("href")
            if (href.isNotEmpty() && href.contains(".m3u8")) {
                val fixed = fixUrl(href)
                Log.d(sourceName, fixed)

                callback(
                    newExtractorLink(
                        source = sourceName,
                        name = sourceName,
                        url = fixed,
                        type = ExtractorLinkType.M3U8
                    ) {
                        this.referer = data
                        this.quality = Qualities.Unknown.value
                    }
                )
                foundLinks = true
            }
        }

        val initialData = getInitialsJson(document.html())
        initialData?.xplayerSettings?.subtitles?.tracks?.forEach { track ->
            track.urls?.vtt?.let { url ->
                val fixed = fixUrl(url)
                val cleanLabel =
                    track.label?.replace(Regex("\\s*\\(auto-generated\\)"), "") ?: track.lang
                    ?: "Unknown"

                Log.d(sourceName, "$cleanLabel $fixed")
                subtitleCallback(newSubtitleFile(lang = cleanLabel, url = fixed))
            }
        }

        return foundLinks
    }

    data class InitialsJson(
        val xplayerSettings: XPlayerSettings? = null
    )

    data class XPlayerSettings(
        val sources: VideoSources? = null,
        val subtitles: Subtitles? = null
    )

    data class VideoSources(
        val hls: HlsSources? = null,
        val standard: StandardSources? = null
    )

    data class HlsSources(val h264: HlsSource? = null)
    data class HlsSource(val url: String? = null)

    data class StandardSources(val h264: List<StandardSourceQuality>? = null)
    data class StandardSourceQuality(val quality: String? = null, val url: String? = null)

    data class Subtitles(val tracks: List<SubtitleTrack>? = null)
    data class SubtitleTrack(
        val label: String? = null,
        val lang: String? = null,
        val urls: SubtitleUrls? = null
    )

    data class SubtitleUrls(val vtt: String? = null)

    private fun getInitialsJson(html: String): InitialsJson? {
        return try {
            val regex = Regex("window\\.initials\\s*=\\s*(\\{.*?\\});", RegexOption.DOT_MATCHES_ALL)
            val match = regex.find(html) ?: return null
            val jsonString = match.groupValues[1]
            val parsedJson = mapper.readValue<InitialsJson>(jsonString)
            parsedJson
        } catch (e: Exception) {
            Log.e("xHamster", "${e.message}")
            null
        }
    }
}
