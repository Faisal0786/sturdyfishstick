package com.sturdyfishstick.xmoviesforyou

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.readValue
import com.lagradost.api.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer

class XMoviesForYou : MainAPI() {
    override var mainUrl = "https://xmoviesforyou.com"
    override var name = "XMoviesForYou"
    override val hasMainPage = true
    override var lang = "en"
    override val hasQuickSearch = false
    override val supportedTypes = setOf(TvType.NSFW)

    override val mainPage = mainPageOf(
        // --- Studios (Step-by-Step Added) ---
        "${mainUrl}/studio/assparade" to "Studio: AssParade",
        "${mainUrl}/studio/atkgirlfriends" to "Studio: ATKGirlfriends",
        "${mainUrl}/studio/backroomcastingcouch" to "Studio: BackroomCastingCouch",
        "${mainUrl}/studio/bangbus" to "Studio: BangBus",
        "${mainUrl}/studio/bjraw" to "Studio: BJRaw",
        "${mainUrl}/studio/blacked" to "Studio: Blacked",
        "${mainUrl}/studio/blackedraw" to "Studio: BlackedRaw",
        "${mainUrl}/studio/blacksonblondes" to "Studio: BlacksOnBlondes",
        "${mainUrl}/studio/brazzersexxtra" to "Studio: BrazzersExxtra",
        "${mainUrl}/studio/cherrypimps" to "Studio: CherryPimps",
        "${mainUrl}/studio/dadcrush" to "Studio: DadCrush",
        "${mainUrl}/studio/deeplush" to "Studio: DeepLush",
        "${mainUrl}/studio/deeper" to "Studio: Deeper",
        "${mainUrl}/studio/devilsfilm" to "Studio: DevilsFilm",
        "${mainUrl}/studio/dickdrainers" to "Studio: DickDrainers",
        "${mainUrl}/studio/dirtyauditions" to "Studio: DirtyAuditions",
        "${mainUrl}/studio/dorcelclub" to "Studio: DorcelClub",
        "${mainUrl}/studio/elegantangel" to "Studio: ElegantAngel",
        "${mainUrl}/studio/eviltilexxx" to "Studio: EvilTileXXX",
        "${mainUrl}/studio/evilangel" to "Studio: EvilAngel",
        "${mainUrl}/studio/exxtrasmall" to "Studio: ExxtraSmall",
        "${mainUrl}/studio/familystrokes" to "Studio: FamilyStrokes",
        "${mainUrl}/studio/familytherapyxx" to "Studio: FamilyTherapyXX",
        "${mainUrl}/studio/gangbangcreampie" to "Studio: GangbangCreampie",
        "${mainUrl}/studio/girlsway" to "Studio: GirlsWay",
        "${mainUrl}/studio/hookuphots" to "Studio: HookupHots",
        "${mainUrl}/studio/hotwifexxx" to "Studio: HotwifeXXX",
        "${mainUrl}/studio/hussiepass" to "Studio: HussiePass",
        "${mainUrl}/studio/hustler" to "Studio: Hustler",
        "${mainUrl}/studio/japanhdv" to "Studio: JapanHDV",
        "${mainUrl}/studio/jayspov" to "Studio: JaysPOV",
        "${mainUrl}/studio/julesjordan" to "Studio: JulesJordan",
        "${mainUrl}/studio/loveherfeet" to "Studio: LoveHerFeet",
        "${mainUrl}/studio/mylifeinmiami" to "Studio: MyLifeInMiami",
        "${mainUrl}/studio/mypervyfamily" to "Studio: MyPervyFamily",
        "${mainUrl}/studio/mysistershotfriend" to "Studio: MySistersHotFriend",
        "${mainUrl}/studio/newsensations" to "Studio: NewSensations",
        "${mainUrl}/studio/nickmarxs" to "Studio: NickMarxs",
        "${mainUrl}/studio/penthousegold" to "Studio: PenthouseGold",
        "${mainUrl}/studio/pervmom" to "Studio: PervMom",
        "${mainUrl}/studio/porndudecasting" to "Studio: PornDudeCasting",
        "${mainUrl}/studio/pornworld" to "Studio: PornWorld",
        "${mainUrl}/studio/private" to "Studio: Private",
        "${mainUrl}/studio/puretaboo" to "Studio: PureTaboo",
        "${mainUrl}/studio/roccosiffredi" to "Studio: RoccoSiffredi",
        "${mainUrl}/studio/seehimfuck" to "Studio: SeeHimFuck",
        "${mainUrl}/studio/sexmex" to "Studio: SexMex",
        "${mainUrl}/studio/shoplyfter" to "Studio: Shoplyfter",
        "${mainUrl}/studio/sislovesme" to "Studio: SisLovesMe",
        "${mainUrl}/studio/spankmonster" to "Studio: SpankMonster",
        "${mainUrl}/studio/sweetsinner" to "Studio: SweetSinner",
        "${mainUrl}/studio/tabooheat" to "Studio: TabooHeat",
        "${mainUrl}/studio/teamskeetselects" to "Studio: TeamSkeetSelects",
        "${mainUrl}/studio/teenfidelity" to "Studio: TeenFidelity",
        "${mainUrl}/studio/throated" to "Studio: Throated",
        "${mainUrl}/studio/touchmywife" to "Studio: TouchMyWife",
        "${mainUrl}/studio/tushy" to "Studio: Tushy",
        "${mainUrl}/studio/tushyraw" to "Studio: TushyRaw",
        "${mainUrl}/studio/vixen" to "Studio: Vixen",
        "${mainUrl}/studio/wicked" to "Studio: Wicked"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val link = if (page <= 1) {
            request.data
        } else {
            "${request.data}?page=$page"
        }

        val sayfa = app.get(link).document
        val icerik = sayfa.select("a.group.flex.flex-col").mapNotNull { it.toMainPageResult() }

        return newHomePageResponse(
            list = HomePageList(
                name = request.name,
                list = icerik,
                isHorizontalImages = true
            ),
            hasNext = true
        )
    }

    private fun Element.toMainPageResult(): SearchResponse? {
        val baslikelementi = this.selectFirst("h3")
        val asilbaslik = baslikelementi?.text()?.trim() ?: return null
        val baslik = asilbaslik.replace(Regex("""\[.*?\]"""), "").trim()
        val adres = fixUrlNull(this.attr("href")) ?: return null
        val afis = fixUrlNull(this.selectFirst("img")?.attr("src"))

        return newMovieSearchResponse(baslik, adres, TvType.NSFW) {
            this.posterUrl = afis
        }
    }

    override suspend fun search(query: String, page: Int): SearchResponseList {
        val link = if (page == 1) {
            "$mainUrl/search?q=$query"
        } else {
            "$mainUrl/search?q=$query&page=$page"
        }

        val sayfa = app.get(link).document
        val sonuclar = sayfa.select("a.group.flex.flex-col").mapNotNull { it.toMainPageResult() }

        return newSearchResponseList(sonuclar, hasNext = true)
    }

    override suspend fun quickSearch(query: String): List<SearchResponse>? = search(query)

    override suspend fun load(url: String): LoadResponse? {
        val cevap = app.get(url)
        val sayfa = cevap.document

        val baslik = sayfa.selectFirst("h1")?.text()?.trim() ?: return null

        val afis = fixUrlNull(
            sayfa.selectFirst("#player img")?.attr("src")
                ?: sayfa.selectFirst("meta[property='og:image']")?.attr("content")
        )

        val ozet = sayfa.selectFirst("div.prose p")?.text()?.trim()

        val yilElementi = sayfa.selectFirst("span.material-symbols-outlined:contains(calendar_month)")?.parent()?.text()
        val yilBulucu = Regex("""\b\d{4}\b""")
        val yil = yilBulucu.find(yilElementi ?: baslik)?.value?.toIntOrNull()

        // Categories & Studios Dono Ke Etiketler Ko Load Karne Ke Liye Update Kiya Gaya Hai
        val etiketler = sayfa.select("a[href*='/category/'], a[href*='/studio/']").map {
            it.text().replace("category", "", ignoreCase = true).replace("studio", "", ignoreCase = true).trim()
        }

        val oyuncular = sayfa.select("a[href*='/pornstar/']").map {
            Actor(it.text().trim(), null)
        }

        val postId = cevap.text.substringAfter("const postId = \"").substringBefore("\"")
        val oneriler = if (postId.isNotEmpty() && postId.length > 3) {
            try {
                val apiCevap = app.get("$mainUrl/api/related/$postId", referer = url).text
                val veri = mapper.readValue<RelatedData>(apiCevap)
                veri.data?.mapNotNull { item ->
                    newMovieSearchResponse(
                        item.title ?: return@mapNotNull null,
                        fixUrl(item.slug ?: return@mapNotNull null),
                        TvType.NSFW
                    ) {
                        this.posterUrl = item.thumbnail_url
                    }
                }
            } catch (e: Exception) {
                Log.d("RelatedError", e.message ?: "")
                null
            }
        } else null

        return newMovieLoadResponse(baslik, url, TvType.NSFW, url) {
            this.posterUrl = afis
            this.plot = ozet
            this.year = yil
            this.tags = etiketler
            this.recommendations = oneriler
            addActors(oyuncular)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val sayfa = app.get(data).document

        sayfa.select("a[href*='streamtape.com'], a[href*='mixdrop'], a[href*='dood'], a[href*='bigwarp']")
            .forEach { baglanti ->
                val link = baglanti.attr("href")
                if (link.isNotEmpty()) {
                    loadExtractor(link, data, subtitleCallback, callback)
                }
            }
        return true
    }

    data class RelatedData(
        val data: List<RelatedItem>? = null
    )

    data class RelatedItem(
        val title: String? = null,
        val slug: String? = null,
        val thumbnail_url: String? = null
    )
}
