package com.osei.aimusicstudio.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.osei.aimusicstudio.auth.AuthViewModel
import com.osei.aimusicstudio.data.model.Song
import com.osei.aimusicstudio.data.local.database.StudioDatabase
import com.osei.aimusicstudio.data.local.preferences.StudioPreferences
import com.osei.aimusicstudio.data.local.repository.RoomSongRepository
import com.osei.aimusicstudio.data.local.repository.RoomUserRepository
import com.osei.aimusicstudio.data.repository.FirestoreSongRepository
import com.osei.aimusicstudio.data.repository.FirestoreUserRepository
import com.osei.aimusicstudio.data.repository.OfflineFirstSongRepository
import com.osei.aimusicstudio.data.repository.OfflineFirstUserRepository
import com.osei.aimusicstudio.data.sync.StudioSyncCoordinator
import com.osei.aimusicstudio.player.Media3AudioPlayer
import com.osei.aimusicstudio.ui.screens.auth.CreateAccountScreen
import com.osei.aimusicstudio.ui.screens.auth.SignInScreen
import com.osei.aimusicstudio.ui.screens.welcome.WelcomeScreen
import com.osei.aimusicstudio.ui.screens.credits.CreditsPricingScreen
import com.osei.aimusicstudio.ui.screens.details.SongDetailsScreen
import com.osei.aimusicstudio.ui.screens.generator.GenreMoodSelectionScreen
import com.osei.aimusicstudio.ui.screens.generator.GenerationProgressScreen
import com.osei.aimusicstudio.ui.screens.generator.LyricsEditorScreen
import com.osei.aimusicstudio.ui.screens.generator.SongGeneratorScreen
import com.osei.aimusicstudio.ui.screens.generator.SongGeneratorViewModel
import com.osei.aimusicstudio.ui.screens.home.HomeScreen
import com.osei.aimusicstudio.ui.screens.library.LibraryViewModel
import com.osei.aimusicstudio.ui.screens.library.MySongsScreen
import com.osei.aimusicstudio.ui.screens.player.MusicPlayerScreen
import com.osei.aimusicstudio.ui.screens.profile.ProfileSettingsScreen
import com.osei.aimusicstudio.ui.screens.profile.ProfileViewModel
import com.osei.aimusicstudio.ui.screens.splash.SplashScreen
import com.osei.aimusicstudio.data.repository.RoomVideoProjectRepository
import com.osei.aimusicstudio.ui.screens.video.VideoStudioScreen
import com.osei.aimusicstudio.ui.screens.video.VideoStudioViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.osei.aimusicstudio.R
import com.osei.aimusicstudio.ui.screens.legal.AboutScreen
import com.osei.aimusicstudio.ui.screens.legal.ContactSupportScreen
import com.osei.aimusicstudio.ui.screens.legal.CopyrightScreen
import com.osei.aimusicstudio.ui.screens.legal.PrivacyPolicyScreen
import com.osei.aimusicstudio.ui.screens.legal.TermsConditionsScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()

    // Initialize Media3 Audio Player
    val audioPlayer = remember { Media3AudioPlayer(context) }
    DisposableEffect(audioPlayer) {
        onDispose { audioPlayer.release() }
    }

    // Initialize Room Database for robust local storage
    val studioDatabase = remember { StudioDatabase.getInstance(context) }
    val localSongStorage = remember(studioDatabase) { RoomSongRepository(studioDatabase.songDao()) }
    val localUserStorage = remember(studioDatabase) { RoomUserRepository(studioDatabase.userProfileDao()) }
    val localVideoStorage = remember(studioDatabase) { RoomVideoProjectRepository(studioDatabase.videoProjectDao()) }

    // Cloud Firestore backends
    val databaseId = context.getString(R.string.firestore_database_id)
    val db = remember(databaseId) { FirebaseFirestore.getInstance(databaseId) }
    val remoteSongRepo = remember(db) { FirestoreSongRepository(db) }
    val remoteUserRepo = remember(db) { FirestoreUserRepository(db) }

    val studioPreferences = remember { StudioPreferences(context) }

    // Sync coordinator for bidirectional Room <-> Firestore sync
    val syncCoordinator = remember(localSongStorage, localUserStorage, remoteSongRepo, remoteUserRepo, studioPreferences) {
        StudioSyncCoordinator(
            localSongStorage = localSongStorage,
            localUserStorage = localUserStorage,
            remoteSongRepository = remoteSongRepo,
            remoteUserRepository = remoteUserRepo,
            studioPreferences = studioPreferences
        )
    }

    // Offline-first repositories combining instant Room reactive reads/writes with Firestore sync
    val songRepository = remember(localSongStorage, syncCoordinator) {
        OfflineFirstSongRepository(
            localSongStorage = localSongStorage,
            syncCoordinator = syncCoordinator
        )
    }
    val userRepository = remember(localUserStorage, syncCoordinator) {
        OfflineFirstUserRepository(
            localUserStorage = localUserStorage,
            syncCoordinator = syncCoordinator
        )
    }

    val activeUserId = currentUser?.uid ?: ""

    // ViewModels scoped to active session
    val generatorViewModel = remember(activeUserId) {
        SongGeneratorViewModel(songRepository, activeUserId)
    }
    val videoViewModel = remember(activeUserId) {
        VideoStudioViewModel(localVideoStorage, songRepository, activeUserId)
    }
    val libraryViewModel = remember(activeUserId) {
        LibraryViewModel(songRepository, activeUserId)
    }
    val profileViewModel = remember(activeUserId) {
        ProfileViewModel(userRepository, activeUserId)
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        // 1. Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    if (currentUser != null) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // 2. Welcome Screen
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToSignIn = { navController.navigate(Screen.SignIn.route) },
                onNavigateToCreateAccount = { navController.navigate(Screen.CreateAccount.route) }
            )
        }

        // 3. Sign-In Screen
        composable(Screen.SignIn.route) {
            SignInScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreateAccount = {
                    navController.navigate(Screen.CreateAccount.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                }
            )
        }

        // 4. Create Account Screen
        composable(Screen.CreateAccount.route) {
            CreateAccountScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSignIn = {
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(Screen.CreateAccount.route) { inclusive = true }
                    }
                }
            )
        }

        // 5. Home Dashboard
        composable(Screen.Home.route) {
            HomeScreen(
                userName = currentUser?.displayName ?: "Creator",
                libraryViewModel = libraryViewModel,
                onNavigateToGenerator = { genre ->
                    if (genre != null) {
                        generatorViewModel.updateGenre(genre)
                    }
                    navController.navigate(Screen.SongGenerator.route)
                },
                onNavigateToVideoStudio = {
                    navController.navigate(Screen.VideoStudio.route)
                },
                onNavigateToLibrary = { navController.navigate(Screen.MySongs.route) },
                onNavigateToPricing = { navController.navigate(Screen.CreditsPricing.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToSongDetails = { songId ->
                    navController.navigate(Screen.SongDetails.createRoute(songId))
                },
                onPlaySong = { song ->
                    if (song.status == "completed" && song.audioUrl.isNotBlank()) {
                        audioPlayer.playSong(song, song.audioUrl)
                        navController.navigate(Screen.MusicPlayer.createRoute(song.id))
                    } else {
                        navController.navigate(Screen.SongDetails.createRoute(song.id))
                    }
                }
            )
        }

        // 6. AI Song Generator / Audio Studio
        composable(Screen.SongGenerator.route) {
            SongGeneratorScreen(
                viewModel = generatorViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToGenreMood = { navController.navigate(Screen.GenreMood.route) },
                onNavigateToLyricsEditor = { navController.navigate(Screen.LyricsEditor.route) },
                onNavigateToProgress = { navController.navigate(Screen.GenerationProgress.route) }
            )
        }

        // 6b. AI Video Studio
        composable(Screen.VideoStudio.route) {
            VideoStudioScreen(
                viewModel = videoViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 7. Genre and Mood Selection
        composable(Screen.GenreMood.route) {
            GenreMoodSelectionScreen(
                viewModel = generatorViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 8. Custom Lyrics Editor
        composable(Screen.LyricsEditor.route) {
            LyricsEditorScreen(
                viewModel = generatorViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 9. Generation Progress
        composable(Screen.GenerationProgress.route) {
            GenerationProgressScreen(
                viewModel = generatorViewModel,
                onNavigateToPlayer = { song ->
                    if (song.status == "completed" && song.audioUrl.isNotBlank()) {
                        audioPlayer.playSong(song, song.audioUrl)
                        navController.navigate(Screen.MusicPlayer.createRoute(song.id)) {
                            popUpTo(Screen.SongGenerator.route) { inclusive = true }
                        }
                    }
                },
                onNavigateToLibrary = {
                    navController.navigate(Screen.MySongs.route) {
                        popUpTo(Screen.SongGenerator.route) { inclusive = true }
                    }
                }
            )
        }

        // 10. Music Player Screen
        composable(
            route = Screen.MusicPlayer.route,
            arguments = listOf(
                navArgument("songId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            MusicPlayerScreen(
                song = null,
                player = audioPlayer,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 11. My Studio Projects (Audio Tracks & Music Videos)
        composable(Screen.MySongs.route) {
            MySongsScreen(
                libraryViewModel = libraryViewModel,
                videoViewModel = videoViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToGenerator = {
                    generatorViewModel.resetForm()
                    navController.navigate(Screen.SongGenerator.route)
                },
                onNavigateToVideoStudio = {
                    videoViewModel.resetForm()
                    navController.navigate(Screen.VideoStudio.route)
                },
                onNavigateToSongDetails = { songId ->
                    navController.navigate(Screen.SongDetails.createRoute(songId))
                },
                onPlaySong = { song ->
                    if (song.status == "completed" && song.audioUrl.isNotBlank()) {
                        audioPlayer.playSong(song, song.audioUrl)
                        navController.navigate(Screen.MusicPlayer.createRoute(song.id))
                    } else {
                        navController.navigate(Screen.SongDetails.createRoute(song.id))
                    }
                }
            )
        }

        // 12. Song Details Screen
        composable(
            route = Screen.SongDetails.route,
            arguments = listOf(
                navArgument("songId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val songId = backStackEntry.arguments?.getString("songId") ?: ""
            SongDetailsScreen(
                songId = songId,
                userId = activeUserId,
                songRepository = songRepository,
                onNavigateBack = { navController.popBackStack() },
                onPlaySong = { song ->
                    if (song.status == "completed" && song.audioUrl.isNotBlank()) {
                        audioPlayer.playSong(song, song.audioUrl)
                        navController.navigate(Screen.MusicPlayer.createRoute(song.id))
                    }
                }
            )
        }

        // 13. Profile & Settings Screen
        composable(Screen.Profile.route) {
            ProfileSettingsScreen(
                user = currentUser,
                profileViewModel = profileViewModel,
                authViewModel = authViewModel,
                studioPreferences = studioPreferences,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPricing = { navController.navigate(Screen.CreditsPricing.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) },
                onNavigateToPrivacy = { navController.navigate(Screen.PrivacyPolicy.route) },
                onNavigateToTerms = { navController.navigate(Screen.TermsConditions.route) },
                onNavigateToCopyright = { navController.navigate(Screen.Copyright.route) },
                onNavigateToContact = { navController.navigate(Screen.ContactSupport.route) },
                onSignOutComplete = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 14. Credits & Pricing Screen
        composable(Screen.CreditsPricing.route) {
            CreditsPricingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 15. About Screen
        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPrivacy = { navController.navigate(Screen.PrivacyPolicy.route) },
                onNavigateToTerms = { navController.navigate(Screen.TermsConditions.route) },
                onNavigateToCopyright = { navController.navigate(Screen.Copyright.route) },
                onNavigateToContact = { navController.navigate(Screen.ContactSupport.route) }
            )
        }

        // 16. Privacy Policy Screen
        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 17. Terms & Conditions Screen
        composable(Screen.TermsConditions.route) {
            TermsConditionsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 18. Copyright Screen
        composable(Screen.Copyright.route) {
            CopyrightScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 19. Contact & Support Screen
        composable(Screen.ContactSupport.route) {
            ContactSupportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
