package com.osei.aimusicstudio.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Welcome : Screen("welcome")
    data object SignIn : Screen("sign_in")
    data object CreateAccount : Screen("create_account")
    data object Home : Screen("home")
    data object AudioStudio : Screen("audio_studio")
    data object VideoStudio : Screen("video_studio")
    data object SongGenerator : Screen("audio_studio") // Kept as alias for AudioStudio for backwards compatibility
    data object GenreMood : Screen("genre_mood")
    data object LyricsEditor : Screen("lyrics_editor")
    data object GenerationProgress : Screen("generation_progress")
    data object MusicPlayer : Screen("music_player?songId={songId}") {
        fun createRoute(songId: String = ""): String =
            if (songId.isNotBlank()) "music_player?songId=$songId" else "music_player"
    }
    data object MySongs : Screen("my_projects")
    data object MyProjects : Screen("my_projects")
    data object SongDetails : Screen("song_details/{songId}") {
        fun createRoute(songId: String): String = "song_details/$songId"
    }
    data object Profile : Screen("profile")
    data object CreditsPricing : Screen("credits_pricing")
    data object About : Screen("about")
    data object PrivacyPolicy : Screen("privacy_policy")
    data object TermsConditions : Screen("terms_conditions")
    data object Copyright : Screen("copyright")
    data object ContactSupport : Screen("contact_support")
}
