package com.osei.aimusicstudio.data.model

object StudioConstants {
    val GENRES = listOf(
        "Ghanaian Highlife",
        "Afrobeats",
        "Hiplife",
        "Gospel",
        "Reggae",
        "Hip-Hop",
        "R&B",
        "Afro-Fusion",
        "Pop",
        "Instrumental"
    )

    val MOODS = listOf(
        "Uplifting",
        "Energetic",
        "Chill",
        "Soulful",
        "Romantic",
        "Melancholic",
        "Celebratory",
        "Spiritual"
    )

    val VOCAL_PREFERENCES = listOf(
        "Male",
        "Female",
        "Duet",
        "Choired",
        "Instrumental Only"
    )

    val LANGUAGES = listOf(
        "English",
        "Twi",
        "Ga",
        "Ewe",
        "Pidgin",
        "Multilingual"
    )

    val TEMPOS = listOf(
        "Slow (75 BPM)",
        "Moderate (110 BPM)",
        "Upbeat (128 BPM)",
        "Highlife Fast (140 BPM)",
        "Fast (160 BPM)"
    )

    val GENRE_DESCRIPTIONS = mapOf(
        "Ghanaian Highlife" to "Classic melodic horns, upbeat guitar grooves, and deep roots in Ghanaian musical culture.",
        "Afrobeats" to "Contemporary West African syncopated drums, infectious percussion, and modern basslines.",
        "Hiplife" to "Dynamic fusion of highlife guitar harmonies with hard-hitting hip-hop cadence and local wordplay.",
        "Gospel" to "Soul-stirring choral harmonies, heartfelt vocal runs, and spiritually uplifting themes.",
        "Reggae" to "Relaxed off-beat guitar skanks, heavy dub basslines, and reflective conscious storytelling.",
        "Hip-Hop" to "Crisp 808 beats, sharp lyricism, and modern studio rhythm arrangements.",
        "R&B" to "Velvet smooth vocal delivery, emotive chords, and lush rhythmic grooves.",
        "Afro-Fusion" to "Boundary-pushing hybrid blending jazz, highlife, dancehall, and electronic elements.",
        "Pop" to "Catchy synth hooks, driving contemporary tempo, and radio-ready song structure.",
        "Instrumental" to "Pure acoustic and digital instrumentation without vocals, ideal for background scores."
    )

    const val MAX_TITLE_LENGTH = 80
    const val MAX_DESCRIPTION_LENGTH = 500
    const val MAX_LYRICS_LENGTH = 3000
    const val MIN_TITLE_LENGTH = 3
}

data class PricingPlan(
    val id: String,
    val name: String,
    val price: String,
    val credits: Int,
    val description: String,
    val features: List<String>,
    val isPopular: Boolean = false
)

val PRICING_PLANS = listOf(
    PricingPlan(
        id = "free",
        name = "Free Starter",
        price = "$0 / mo",
        credits = 50,
        description = "Explore AI music creation and test your musical ideas.",
        features = listOf(
            "50 studio generation credits",
            "Access to Highlife & Afrobeats genres",
            "Standard audio quality playback",
            "Save up to 10 songs in library",
            "Personal non-commercial license"
        )
    ),
    PricingPlan(
        id = "creator",
        name = "Creator Studio",
        price = "$9.99 / mo",
        credits = 300,
        description = "For passionate songsmiths, beatmakers and creators.",
        features = listOf(
            "300 studio generation credits per month",
            "All Ghanaian & international genres unlocked",
            "Custom lyrics editor with rhyme assists",
            "Unlimited library saves & offline caching",
            "Commercial release rights"
        ),
        isPopular = true
    ),
    PricingPlan(
        id = "studio_pro",
        name = "Studio Pro",
        price = "$24.99 / mo",
        credits = 1000,
        description = "Professional power for independent producers and labels.",
        features = listOf(
            "1,000 monthly credits + priority generation queue",
            "Lossless audio export stems (instrumentals + vocals)",
            "Multi-language Twi & English dialect tuning",
            "Dedicated sound engineering presets",
            "Full commercial master ownership"
        )
    )
)
