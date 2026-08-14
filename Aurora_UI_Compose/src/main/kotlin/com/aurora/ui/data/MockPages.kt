package com.aurora.ui.data

import com.aurora.ui.R
import com.aurora.ui.types.*

object MockData {

    private val profiles = listOf(
        Profile("prof-1", "Akib Al Nafij", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&q=80"),
        Profile("prof-2", "Family Lounge", "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=100&q=80", isGuest = true, isSynced = false),
        Profile("prof-3", "Guest Mode", "", isGuest = true, isSynced = false)
    )

    fun getProfiles(): List<Profile> = profiles

    data class PopularSite(val name: String, val url: String, val icon: String, val color: Long)

    val popularSites = listOf(
        PopularSite("YouTube", "https://youtube.com", "Youtube", 0xFFFF0000),
        PopularSite("GitHub", "https://github.com", "Github", 0xFF181717),
        PopularSite("Wikipedia", "https://wikipedia.org", "BookOpen", 0xFFFFFFFF),
        PopularSite("Google News", "https://news.google.com", "Newspaper", 0xFF4285F4),
        PopularSite("Reddit", "https://reddit.com", "MessageSquare", 0xFFFF4500),
        PopularSite("Google Drive", "https://drive.google.com", "HardDrive", 0xFF34A853)
    )

    private val siteLogoRes = mapOf(
        "https://aha.video" to R.drawable.site_aha,
        "https://altbalaji.com" to R.drawable.site_altbalaji,
        "https://amcplus.com" to R.drawable.site_amc,
        "https://tv.apple.com" to R.drawable.site_apple_tv,
        "https://ardmediathek.de" to R.drawable.site_ard_mediathek,
        "https://arte.tv" to R.drawable.site_arte,
        "https://atresplayer.com" to R.drawable.site_atresplayer,
        "https://bbc.co.uk/iplayer" to R.drawable.site_bbc_iplayer,
        "https://bilibili.com" to R.drawable.site_bilibili,
        "https://binge.watch" to R.drawable.site_binge,
        "https://blim.com" to R.drawable.site_blim,
        "https://cmore.se" to R.drawable.site_c_more,
        "https://channel4.com" to R.drawable.site_channel_4,
        "https://chaupal.tv" to R.drawable.site_chaupal,
        "https://clarovideo.com" to R.drawable.site_claro_video,
        "https://crackle.com" to R.drawable.site_crackle,
        "https://crunchyroll.com" to R.drawable.site_crunchyroll,
        "https://dailymotion.com" to R.drawable.site_dailymotion,
        "https://directv.com" to R.drawable.site_directv,
        "https://www.disney.com" to R.drawable.site_disney,
        "https://hotstar.com" to R.drawable.site_hotstar,
        "https://dr.dk/drtv" to R.drawable.site_dr_tv,
        "https://dstv.com" to R.drawable.site_dstv,
        "https://erosnow.com" to R.drawable.site_eros_now,
        "https://france.tv" to R.drawable.site_france_tv,
        "https://fubo.tv" to R.drawable.site_fubo,
        "https://funimation.com" to R.drawable.site_funimation,
        "https://github.com" to R.drawable.site_github,
        "https://globoplay.globo.com" to R.drawable.site_globoplay,
        "https://drive.google.com" to R.drawable.site_google_drive,
        "https://news.google.com" to R.drawable.site_google_news,
        "https://hidive.com" to R.drawable.site_hidive,
        "https://hoichoi.tv" to R.drawable.site_hoichoi,
        "https://hulu.com" to R.drawable.site_hulu,
        "https://hum.tv" to R.drawable.site_hum_tv,
        "https://iflix.com" to R.drawable.site_iflix,
        "https://iq.com" to R.drawable.site_iqiyi,
        "https://itv.com" to R.drawable.site_itvx,
        "https://jiocinema.com" to R.drawable.site_jiocinema,
        "https://joyn.de" to R.drawable.site_joyn,
        "https://kanopy.com" to R.drawable.site_kanopy,
        "https://kocowa.com" to R.drawable.site_kocowa,
        "https://6play.fr" to R.drawable.site_m6,
        "https://mgtv.com" to R.drawable.site_mango_tv,
        "https://manoramamax.com" to R.drawable.site_manoramamax,
        "https://max.com" to R.drawable.site_max,
        "https://mediasetplay.mediaset.it" to R.drawable.site_mediaset_infinity,
        "https://mitele.es" to R.drawable.site_mitele,
        "https://mubi.com" to R.drawable.site_mubi,
        "https://mxplayer.in" to R.drawable.site_mx_player,
        "https://netflix.com" to R.drawable.site_netflix,
        "https://nowtv.com" to R.drawable.site_now,
        "https://npostart.nl" to R.drawable.site_npo_start,
        "https://tv.nrk.no" to R.drawable.site_nrk_tv,
        "https://ondemandkorea.com" to R.drawable.site_ondemandkorea,
        "https://osnplus.com" to R.drawable.site_osn,
        "https://panflix.com.br" to R.drawable.site_panflix,
        "https://paramountplus.com" to R.drawable.site_paramount,
        "https://peacocktv.com" to R.drawable.site_peacock,
        "https://philo.com" to R.drawable.site_philo,
        "https://plex.tv" to R.drawable.site_plex,
        "https://pluto.tv" to R.drawable.site_pluto_tv,
        "https://primevideo.com" to R.drawable.site_prime_video,
        "https://raiplay.it" to R.drawable.site_raiplay,
        "https://rakuten.tv" to R.drawable.site_rakuten_tv,
        "https://reddit.com" to R.drawable.site_reddit,
        "https://retrocrush.tv" to R.drawable.site_retrocrush,
        "https://therokuchannel.roku.com" to R.drawable.site_roku_channel,
        "https://auvio.rtbf.be" to R.drawable.site_rtbf_auvio,
        "https://rtlplus.de" to R.drawable.site_rtl,
        "https://rtp.pt/play" to R.drawable.site_rtp_play,
        "https://rtve.es/play" to R.drawable.site_rtve_play,
        "https://shahid.mbc.net" to R.drawable.site_shahid,
        "https://shemaroome.com" to R.drawable.site_shemaroome,
        "https://showmax.com" to R.drawable.site_showmax,
        "https://showtime.com" to R.drawable.site_showtime,
        "https://shudder.com" to R.drawable.site_shudder,
        "https://skygo.sky.com" to R.drawable.site_sky_go,
        "https://sling.com" to R.drawable.site_sling_tv,
        "https://sonyliv.com" to R.drawable.site_sonyliv,
        "https://starz.com" to R.drawable.site_starz,
        "https://starzplay.com" to R.drawable.site_starzplay,
        "https://sunnxt.com" to R.drawable.site_sun_nxt,
        "https://svtplay.se" to R.drawable.site_svt_play,
        "https://tamasha.com" to R.drawable.site_tamasha,
        "https://telecineplay.com.br" to R.drawable.site_telecine_play,
        "https://v.qq.com" to R.drawable.site_tencent_video,
        "https://tf1.fr" to R.drawable.site_tf1,
        "https://tubitv.com" to R.drawable.site_tubi,
        "https://tv4play.se" to R.drawable.site_tv4_play,
        "https://tving.com" to R.drawable.site_tving,
        "https://viaplay.com" to R.drawable.site_viaplay,
        "https://videoland.com" to R.drawable.site_videoland,
        "https://viki.com" to R.drawable.site_viki,
        "https://vimeo.com" to R.drawable.site_vimeo,
        "https://viu.com" to R.drawable.site_viu,
        "https://voot.com" to R.drawable.site_voot,
        "https://vrt.be/max" to R.drawable.site_vrt_max,
        "https://vudu.com" to R.drawable.site_vudu,
        "https://wavve.com" to R.drawable.site_wavve,
        "https://wetv.vip" to R.drawable.site_wetv,
        "https://wikipedia.org" to R.drawable.site_wikipedia,
        "https://youku.com" to R.drawable.site_youku,
        "https://youtube.com" to R.drawable.site_youtube,
        "https://zattoo.com" to R.drawable.site_zattoo,
        "https://zdf.de" to R.drawable.site_zdf_mediathek,
        "https://zee5.com" to R.drawable.site_zee5,
        "https://facebook.com" to R.drawable.site_facebook,
        "https://messenger.com" to R.drawable.site_messenger,
        "https://web.whatsapp.com" to R.drawable.site_whatsapp,
        "https://instagram.com" to R.drawable.site_instagram,
        "https://x.com" to R.drawable.site_x,
        "https://linkedin.com" to R.drawable.site_linkedin,
        "https://web.telegram.org" to R.drawable.site_telegram,
        "https://discord.com" to R.drawable.site_discord,
        "https://pinterest.com" to R.drawable.site_pinterest,
        "https://tumblr.com" to R.drawable.site_tumblr,
        "https://threads.net" to R.drawable.site_threads,
        "https://animepahe.pw" to R.drawable.site_animepahe,
        "https://chatgpt.com" to R.drawable.site_chatgpt,
        "https://claude.ai" to R.drawable.site_claude,
        "https://gemini.google.com" to R.drawable.site_gemini,
        "https://copilot.microsoft.com" to R.drawable.site_copilot,
        "https://perplexity.ai" to R.drawable.site_perplexity,
        "https://chat.deepseek.com" to R.drawable.site_deepseek,
        "https://grok.com" to R.drawable.site_grok,
        "https://poe.com" to R.drawable.site_poe,
        "https://meta.ai" to R.drawable.site_meta_ai,
        "https://chat.mistral.ai" to R.drawable.site_mistral,
        "https://you.com" to R.drawable.site_you,
        "https://huggingface.co" to R.drawable.site_huggingface
    )

    fun logoResFor(site: PopularSite): Int = siteLogoRes[site.url] ?: 0

    const val STREAMING_COLUMNS = 6

    fun streamingRowGroupName(row: Int): String = "streaming_r$row"
    fun streamingRowGroupNames(): List<String> =
        (0 until (streamingSites.size + STREAMING_COLUMNS - 1) / STREAMING_COLUMNS).map { streamingRowGroupName(it) }

    fun featuredRowGroupName(row: Int): String = "streaming_featured_r$row"
    fun featuredRowGroupNames(): List<String> =
        (0 until (featuredStreamingSites.size + STREAMING_COLUMNS - 1) / STREAMING_COLUMNS).map { featuredRowGroupName(it) }

    val featuredStreamingSites = listOf(
        PopularSite("Netflix", "https://netflix.com", "Movie", 0xFFE50914),
        PopularSite("Prime Video", "https://primevideo.com", "PlayArrow", 0xFF00A8E1),
        PopularSite("Disney", "https://www.disney.com", "Movie", 0xFF113CCF),
        PopularSite("Max", "https://max.com", "Movie", 0xFF991EEB),
        PopularSite("YouTube", "https://youtube.com", "Youtube", 0xFFFF0000),
        PopularSite("Hotstar", "https://hotstar.com", "PlayArrow", 0xFF1F80E0),
        PopularSite("Crunchyroll", "https://crunchyroll.com", "PlayArrow", 0xFFF47521),
        PopularSite("Hulu", "https://hulu.com", "Tv", 0xFF1CE783),
        PopularSite("Apple TV+", "https://tv.apple.com", "Tv", 0xFFF5F5F7),
        PopularSite("Tubi", "https://tubitv.com", "Tv", 0xFF3AA757),
        PopularSite("Peacock", "https://peacocktv.com", "Tv", 0xFFFDCB11),
        PopularSite("Paramount+", "https://paramountplus.com", "Star", 0xFF0064FF)
    )

    val streamingSites = listOf(
        PopularSite("Netflix", "https://netflix.com", "Movie", 0xFFE50914),
        PopularSite("Prime Video", "https://primevideo.com", "PlayArrow", 0xFF00A8E1),
        PopularSite("Disney", "https://www.disney.com", "Movie", 0xFF113CCF),
        PopularSite("Max", "https://max.com", "Movie", 0xFF991EEB),
        PopularSite("Hulu", "https://hulu.com", "Tv", 0xFF1CE783),
        PopularSite("Apple TV+", "https://tv.apple.com", "Tv", 0xFFF5F5F7),
        PopularSite("Paramount+", "https://paramountplus.com", "Star", 0xFF0064FF),
        PopularSite("Peacock", "https://peacocktv.com", "Tv", 0xFFFDCB11),
        PopularSite("Tubi", "https://tubitv.com", "Tv", 0xFF3AA757),
        PopularSite("Pluto TV", "https://pluto.tv", "Tv", 0xFF0F5EFF),
        PopularSite("YouTube", "https://youtube.com", "Youtube", 0xFFFF0000),
        PopularSite("Vimeo", "https://vimeo.com", "PlayArrow", 0xFF1AB7EA),
        PopularSite("DailyMotion", "https://dailymotion.com", "PlayArrow", 0xFF00AADD),
        PopularSite("Roku Channel", "https://therokuchannel.roku.com", "Tv", 0xFF662D91),
        PopularSite("Plex", "https://plex.tv", "PlayArrow", 0xFFE5A00D),
        PopularSite("Kanopy", "https://kanopy.com", "PlayArrow", 0xFF4B4B4B),
        PopularSite("Crackle", "https://crackle.com", "PlayArrow", 0xFF0045FF),
        PopularSite("Vudu", "https://vudu.com", "PlayArrow", 0xFF0073E6),
        PopularSite("Mubi", "https://mubi.com", "Star", 0xFFFFC300),
        PopularSite("Shudder", "https://shudder.com", "Movie", 0xFFB20000),
        PopularSite("AMC+", "https://amcplus.com", "Star", 0xFFE3A829),
        PopularSite("Starz", "https://starz.com", "Star", 0xFFFFB600),
        PopularSite("Showtime", "https://showtime.com", "Star", 0xFFC42432),
        PopularSite("Fubo", "https://fubo.tv", "Tv", 0xFF00A6D6),
        PopularSite("Sling TV", "https://sling.com", "Tv", 0xFF0F70C1),
        PopularSite("Philo", "https://philo.com", "Tv", 0xFF9B51E0),
        PopularSite("DIRECTV", "https://directv.com", "Tv", 0xFF00A9E0),
        PopularSite("BBC iPlayer", "https://bbc.co.uk/iplayer", "Tv", 0xFFFF4C4C),
        PopularSite("ITVX", "https://itv.com", "Tv", 0xFF2B3A67),
        PopularSite("Channel 4", "https://channel4.com", "Tv", 0xFF4F28B4),
        PopularSite("Sky Go", "https://skygo.sky.com", "Tv", 0xFF00A5DF),
        PopularSite("NOW", "https://nowtv.com", "Tv", 0xFF8D0A92),
        PopularSite("Rakuten TV", "https://rakuten.tv", "PlayArrow", 0xFFBF0000),
        PopularSite("Zattoo", "https://zattoo.com", "Tv", 0xFF0097E0),
        PopularSite("ARD Mediathek", "https://ardmediathek.de", "Tv", 0xFF00386B),
        PopularSite("ZDF Mediathek", "https://zdf.de", "Tv", 0xFFFF0037),
        PopularSite("ARTE", "https://arte.tv", "Movie", 0xFFE1000F),
        PopularSite("Joyn", "https://joyn.de", "Tv", 0xFF7E2BE0),
        PopularSite("RTL+", "https://rtlplus.de", "Tv", 0xFFFFC600),
        PopularSite("TF1+", "https://tf1.fr", "Tv", 0xFF0076BF),
        PopularSite("France TV", "https://france.tv", "Tv", 0xFFE2001A),
        PopularSite("M6+", "https://6play.fr", "Tv", 0xFFFF5A00),
        PopularSite("Mediaset Infinity", "https://mediasetplay.mediaset.it", "Tv", 0xFF0069AA),
        PopularSite("RaiPlay", "https://raiplay.it", "Tv", 0xFF0051A8),
        PopularSite("RTVE Play", "https://rtve.es/play", "Tv", 0xFFE2001A),
        PopularSite("ATRESplayer", "https://atresplayer.com", "Tv", 0xFF0067A8),
        PopularSite("Mitele", "https://mitele.es", "Tv", 0xFFFF6A00),
        PopularSite("RTP Play", "https://rtp.pt/play", "Tv", 0xFF001E5C),
        PopularSite("SVT Play", "https://svtplay.se", "Tv", 0xFF00589C),
        PopularSite("TV4 Play", "https://tv4play.se", "Tv", 0xFF00A9E0),
        PopularSite("Viaplay", "https://viaplay.com", "PlayArrow", 0xFF2A6BFF),
        PopularSite("C More", "https://cmore.se", "Star", 0xFFFFCC00),
        PopularSite("NRK TV", "https://tv.nrk.no", "Tv", 0xFF00B9F2),
        PopularSite("DR TV", "https://dr.dk/drtv", "Tv", 0xFFFF6B00),
        PopularSite("VRT MAX", "https://vrt.be/max", "Tv", 0xFF00A03E),
        PopularSite("RTBF Auvio", "https://auvio.rtbf.be", "Tv", 0xFFE2001A),
        PopularSite("NPO Start", "https://npostart.nl", "Tv", 0xFF0A3B8C),
        PopularSite("Videoland", "https://videoland.com", "Tv", 0xFF003D8F),
        PopularSite("Disney+ Hotstar", "https://hotstar.com", "PlayArrow", 0xFF1F80E0),
        PopularSite("JioCinema", "https://jiocinema.com", "PlayArrow", 0xFF0053B0),
        PopularSite("SonyLIV", "https://sonyliv.com", "PlayArrow", 0xFF42145F),
        PopularSite("ZEE5", "https://zee5.com", "PlayArrow", 0xFF801C94),
        PopularSite("MX Player", "https://mxplayer.in", "PlayArrow", 0xFF00C3FF),
        PopularSite("Voot", "https://voot.com", "PlayArrow", 0xFFFF0054),
        PopularSite("ALTBalaji", "https://altbalaji.com", "PlayArrow", 0xFF8A2BE2),
        PopularSite("aha", "https://aha.video", "PlayArrow", 0xFFFF3300),
        PopularSite("Sun NXT", "https://sunnxt.com", "PlayArrow", 0xFFFF3A00),
        PopularSite("Hoichoi", "https://hoichoi.tv", "PlayArrow", 0xFFFFC52E),
        PopularSite("ManoramaMax", "https://manoramamax.com", "PlayArrow", 0xFF0E2E7E),
        PopularSite("Eros Now", "https://erosnow.com", "PlayArrow", 0xFFC00E1E),
        PopularSite("ShemarooMe", "https://shemaroome.com", "PlayArrow", 0xFFFF3D3D),
        PopularSite("Chaupal", "https://chaupal.tv", "PlayArrow", 0xFF4B0082),
        PopularSite("Hum TV", "https://hum.tv", "Tv", 0xFF0F7D37),
        PopularSite("iFlix", "https://iflix.com", "PlayArrow", 0xFF00B5AD),
        PopularSite("Viu", "https://viu.com", "PlayArrow", 0xFFFF8C00),
        PopularSite("Tamasha", "https://tamasha.com", "PlayArrow", 0xFFFF3E6C),
        PopularSite("Binge", "https://binge.watch", "PlayArrow", 0xFFFFD100),
        PopularSite("iQIYI", "https://iq.com", "PlayArrow", 0xFF00BE06),
        PopularSite("WeTV", "https://wetv.vip", "PlayArrow", 0xFF0A7AFB),
        PopularSite("Youku", "https://youku.com", "PlayArrow", 0xFF00A1E9),
        PopularSite("Tencent Video", "https://v.qq.com", "PlayArrow", 0xFF00A5EB),
        PopularSite("Bilibili", "https://bilibili.com", "PlayArrow", 0xFF00AEEC),
        PopularSite("Mango TV", "https://mgtv.com", "PlayArrow", 0xFFFF6A00),
        PopularSite("TVING", "https://tving.com", "PlayArrow", 0xFFFF00AA),
        PopularSite("Wavve", "https://wavve.com", "PlayArrow", 0xFF0057A8),
        PopularSite("KOCOWA", "https://kocowa.com", "PlayArrow", 0xFF8A4DFF),
        PopularSite("Viki", "https://viki.com", "PlayArrow", 0xFF5A31F4),
        PopularSite("OnDemandKorea", "https://ondemandkorea.com", "PlayArrow", 0xFFE4002B),
        PopularSite("Crunchyroll", "https://crunchyroll.com", "PlayArrow", 0xFFF47521),
        PopularSite("Funimation", "https://funimation.com", "PlayArrow", 0xFF6C1FAB),
        PopularSite("HiDive", "https://hidive.com", "PlayArrow", 0xFF00A8E8),
        PopularSite("RetroCrush", "https://retrocrush.tv", "PlayArrow", 0xFFFF3D3D),
        PopularSite("AnimePahe", "https://animepahe.pw", "PlayArrow", 0xFFE91E63),
        PopularSite("Claro Video", "https://clarovideo.com", "PlayArrow", 0xFF0FAD2D),
        PopularSite("Blim", "https://blim.com", "PlayArrow", 0xFF00AEEF),
        PopularSite("GloboPlay", "https://globoplay.globo.com", "PlayArrow", 0xFF0B0B0B),
        PopularSite("Panflix", "https://panflix.com.br", "PlayArrow", 0xFFFFB81C),
        PopularSite("Telecine Play", "https://telecineplay.com.br", "PlayArrow", 0xFFF5F5F5),
        PopularSite("Shahid", "https://shahid.mbc.net", "PlayArrow", 0xFFD9A200),
        PopularSite("OSN+", "https://osnplus.com", "PlayArrow", 0xFF00A1E0),
        PopularSite("StarzPlay", "https://starzplay.com", "PlayArrow", 0xFF6E1F7B),
        PopularSite("Showmax", "https://showmax.com", "PlayArrow", 0xFF12C2A3),
        PopularSite("DStv", "https://dstv.com", "Tv", 0xFF6EBA2C)
    )

    fun socialRowGroupName(row: Int): String = "social_r$row"
    fun socialRowGroupNames(): List<String> =
        (0 until (socialSites.size + STREAMING_COLUMNS - 1) / STREAMING_COLUMNS).map { socialRowGroupName(it) }

    val socialSites = listOf(
        PopularSite("Facebook", "https://facebook.com", "Social", 0xFF1877F2),
        PopularSite("Messenger", "https://messenger.com", "Social", 0xFF00B2FF),
        PopularSite("WhatsApp", "https://web.whatsapp.com", "Social", 0xFF25D366),
        PopularSite("Instagram", "https://instagram.com", "Social", 0xFFE4405F),
        PopularSite("X (Twitter)", "https://x.com", "Social", 0xFF111111),
        PopularSite("Reddit", "https://reddit.com", "Social", 0xFFFF4500),
        PopularSite("LinkedIn", "https://linkedin.com", "Social", 0xFF0A66C2),
        PopularSite("Telegram", "https://web.telegram.org", "Social", 0xFF26A5E4),
        PopularSite("Discord", "https://discord.com", "Social", 0xFF5865F2),
        PopularSite("Pinterest", "https://pinterest.com", "Social", 0xFFE60023),
        PopularSite("Tumblr", "https://tumblr.com", "Social", 0xFF36465D),
        PopularSite("Threads", "https://threads.net", "Social", 0xFF7C6FF0)
    )

    fun aiRowGroupName(row: Int): String = "ai_r$row"
    fun aiRowGroupNames(): List<String> =
        (0 until (aiSites.size + STREAMING_COLUMNS - 1) / STREAMING_COLUMNS).map { aiRowGroupName(it) }

    val aiSites = listOf(
        PopularSite("ChatGPT", "https://chatgpt.com", "AI", 0xFF10A37F),
        PopularSite("Claude", "https://claude.ai", "AI", 0xFFD97757),
        PopularSite("Gemini", "https://gemini.google.com", "AI", 0xFF8E75B2),
        PopularSite("Copilot", "https://copilot.microsoft.com", "AI", 0xFF6C5CE7),
        PopularSite("Perplexity", "https://perplexity.ai", "AI", 0xFF1FB8CD),
        PopularSite("DeepSeek", "https://chat.deepseek.com", "AI", 0xFF5786FE),
        PopularSite("Grok", "https://grok.com", "AI", 0xFF111111),
        PopularSite("Poe", "https://poe.com", "AI", 0xFF5D5CDE),
        PopularSite("Meta AI", "https://meta.ai", "AI", 0xFF0467DF),
        PopularSite("Mistral", "https://chat.mistral.ai", "AI", 0xFFFF7000),
        PopularSite("You.com", "https://you.com", "AI", 0xFF6F4EFF),
        PopularSite("Hugging Face", "https://huggingface.co", "AI", 0xFFFFD21E)
    )

    val mockVideos = listOf(
        MockVideo(
            "vid-1", "Aurora OS — Living Glass Concept Trailer", "Aurora Labs",
            "1.2M views", "2 days ago",
            "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400&q=80",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            "09:56",
            "Introducing the next generation design system for large screens. Soft translucent layers, ambient glows, and elastic spring cursors combined into a cohesive TV experience."
        ),
        MockVideo(
            "vid-2", "The Beauty of Atmospheric Noise & OLED Depth", "Cinematic Coding",
            "340K views", "1 week ago",
            "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=400&q=80",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            "14:22",
            "An exploration of how modern web browsers optimize raster layers, utilizing sub-3% noise masks and deep OLED blacks for cinematic display comfort."
        ),
        MockVideo(
            "vid-3", "Sintel — Open Movie Animation Test", "Durian Project",
            "4.8M views", "3 months ago",
            "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?w=400&q=80",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            "04:15",
            "Official test file for Sintel project, showcasing advanced CGI rendering, composition, and high dynamic range details perfect for TV benchmarking."
        )
    )

    val mockArticles = mapOf(
        "living-glass" to MockArticle(
            "Living Glass: Designing Translucent Interfaces for TV",
            "Elena Rostova",
            "July 15, 2026",
            "6 min read",
            """
                ## The Philosophy of Living Glass
                The television screen is different from any other display in the home. At 2 to 4 meters away, it behaves not as a tactile pad or a compact workspace, but as an active window in the room. This dictates a completely different design language.
                
                Traditional design systems rely on sharp rectangular card cutouts, drop shadows, and heavy opaque headers. These models are adapted from mobile and desktop, where space is sparse and readability demands boundaries. On TV, these boxes look heavy and mechanical.
                
                We present **Living Glass**—a translucent, reactive surface that floats above a deep OLED background.
                
                ### Core Principles
                
                1. **Subtle matte translucency:** We avoid the glossy, highly reflective glassmorphism patterns of early web toys. Living Glass uses a soft, heavy backdrop blur (min 20px) and dark surface tint (e.g., `rgba(23, 24, 28, 0.75)`).
                2. **Dynamic ambient glows:** When an item gains focus, it doesn't just display a bright outline. It casts a soft, low-opacity radial light behind itself, blending with the favicon color or content theme.
                3. **Spatial layering:** The UI is composed of floating panels with generous whitespace. This creates an immersive, comfortable, and cinematic experience that respects the physical room boundary.
                
                ### The TV Browser Constraint
                A television browser has to balance reading long paragraphs of documentation with scanning grid layouts. By scaling base typography (minimum 16sp for body text) and maintaining a consistent 8px grid alignment, we can ensure that text is readable from the couch without causing eye strain.
            """.trimIndent()
        ),
        "performance-architecture" to MockArticle(
            "Atmospheric Rendering: Optimizing Web Performance on Low-Memory TV Dongles",
            "Akib Al Nafij",
            "July 18, 2026",
            "8 min read",
            """
                ## The TV Performance Dilemma
                Many popular streaming sticks (Chromecast with Google TV, Fire TV dongles) operate on limited hardware. They typically pack only 1.5GB to 2GB of RAM, and older quad-core processors that throttle quickly under heavy CPU loads.
                
                Yet, the expectations of television users are extremely high. They want buttery 60fps transitions and instant response times, similar to native streaming apps.
                
                ### Tab State Compression
                To counter the constraint of limited memory, we design an intelligent tab state machine:
                
                - **Active:** Full resources, rendered directly onto the main composition layer.
                - **Background:** JavaScript timers throttled to 1Hz, rendering updates suspended.
                - **Sleeping:** Tab memory is compressed using custom V8 state snapshots. Open connections are held but JS is frozen.
                - **Discarded:** Memory is fully evicted. The tab retains its scroll position and metadata (URL, page title) in SQLite, allowing transparent restore on re-focus.
                
                ### Animation Governor
                Our animation engine dynamically scales complexity based on current process metrics. When the GPU is busy decoding high-bitrate video, the governor selectively disables costly CSS filters (like back-drop blurs) first. It preserves basic scales and glows, which are cheap for the compositor to compute.
                
                Through these architectural choices, we achieve a baseline memory footprint of under 250MB for active multi-tab browsing.
            """.trimIndent()
        )
    )

    val mockFiles = listOf(
        MockFile(
            "file-1", "Aurora_Design_System_Spec_v2.pdf", "4.2 MB", "application/pdf",
            "https://aurora.design/spec/Aurora_v2_Spec.pdf",
            PdfData("Aurora Design System v2.0", "Premium TV Browser Specification Handbook",
                listOf(
                    "Page 1: Introduction to Living Glass Architecture & OLED-first palettes",
                    "Page 2: Typography tokens - Inter + Space Grotesk paired with JetBrains Mono",
                    "Page 3: Spacing grid - 8px base grid with 64px overscan safe margins",
                    "Page 4: Spring-animated TV Cursor with snapping rings and glow offsets",
                    "Page 5: Component specifications - Floating Toolbar, Address bar, Tab workspace",
                    "Page 6: Focus System 2.0 - Scale, elevation, glow, and border cues combined"
                )
            )
        ),
        MockFile(
            "file-2", "aurora_sunset_4k.png", "2.8 MB", "image/png",
            "https://images.unsplash.com/photo-1472214222541-d510753a4707?w=1200&q=80",
            "A beautiful high-resolution capture of an atmospheric sunset showing deep orange glows fading into cosmic twilight blue, matching the Aurora Midnight color theme."
        ),
        MockFile(
            "file-3", "aurora_lofi_relax.mp3", "8.4 MB", "audio/mp3",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            AudioMeta("Aurora Ambient Lounge", "Lofi Tech", "Living Glass Sessions", "06:12",
                "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=400&q=80")
        ),
        MockFile(
            "file-4", "release_notes.txt", "12 KB", "text/plain",
            "https://aurora.design/downloads/release_notes.txt",
            """
                ========================================
                AURORA TV BROWSER v2.0.0 RELEASE NOTES
                ========================================
                
                Welcome to the official build of Aurora, the ultimate web browser crafted specifically for Google TV and Android TV screens.
                
                WHAT IS NEW IN V2.0:
                -------------------
                1. LIVING GLASS DESIGN:
                   Experience unparalleled visual comfort with translucent backgrounds, radial glowing highlights, and high-contrast focus rings designed for a distance of 3 meters.
                
                2. SPRING-ANIMATED CURSOR:
                   An incredibly organic virtual cursor with spring velocity, stretch mapping, and snap rings that stick to links and buttons dynamically.
                
                3. TAB WORKSPACE:
                   Say goodbye to tiny tab strips. Our Workspace groups open sessions with high-resolution screenshot cards, unread states, and recently closed recovery rows.
                
                4. BUILT-IN MEDIA HUB:
                   Listen to lofi beats in the background while researching, view PDF manuals, play 4K streams with PiP, and inspect markdown files with built-in text views.
                
                5. PROCESS MONITOR & TIMELINE:
                   Monitor system health directly in our Diagnostics Center. View active processes and track timeline logs chronologically.
                
                For feedback and questions, visit: https://github.com/aurora-browser/aurora
            """.trimIndent()
        )
    )
}

data class PdfData(val title: String, val subtitle: String, val pages: List<String>)
data class AudioMeta(val title: String, val artist: String, val album: String, val duration: String, val cover: String)
