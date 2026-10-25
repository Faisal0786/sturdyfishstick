plugins {
    id("com.lagradost.cloudstream3.gradle")
}

// Version ko root level par rakhein (Integer hona chahiye, jaise 1)
version = 10

cloudstream {
    // Description aur language ko yahan quotes ke andar sahi se define karein
    description = "XmoviesForYou - Watch Porn Movies with original high speed servers"
    language = "en"
    
    authors = listOf("sturdyfishstick")
    status = 3 // 3 matlab Stable, ya 1 (Beta) rakhein
    tvTypes = listOf("Movie")
    iconUrl = "https://t0.gstatic.com/faviconV2?client=SOCIAL&type=FAVICON&fallback_ts=128&url=https://xmoviesforyou.com&size=128"
    iconUrl = "https://xmoviescdn.online/2018/08/logo.png"
}

android {
    defaultConfig {
        minSdk = 21
    }
}

    }
}
