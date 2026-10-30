package com.osei.aimusicstudio.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.osei.aimusicstudio.data.local.dao.SongDao
import com.osei.aimusicstudio.data.local.dao.UserProfileDao
import com.osei.aimusicstudio.data.local.database.StudioDatabase
import com.osei.aimusicstudio.data.local.entity.LocalSongEntity
import com.osei.aimusicstudio.data.local.entity.LocalUserProfileEntity
import com.osei.aimusicstudio.data.local.repository.RoomSongRepository
import com.osei.aimusicstudio.data.local.repository.RoomUserRepository
import com.osei.aimusicstudio.data.model.Song
import com.osei.aimusicstudio.data.model.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomDatabaseTest {

    private lateinit var database: StudioDatabase
    private lateinit var songDao: SongDao
    private lateinit var userProfileDao: UserProfileDao
    private lateinit var songRepository: RoomSongRepository
    private lateinit var userRepository: RoomUserRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, StudioDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        songDao = database.songDao()
        userProfileDao = database.userProfileDao()
        songRepository = RoomSongRepository(songDao)
        userRepository = RoomUserRepository(userProfileDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveSong_usingDao() = runBlocking {
        val songEntity = LocalSongEntity(
            id = "test_song_1",
            userId = "user_123",
            title = "Golden Stool Highlife",
            description = "Celebration song in Kumasi",
            genre = "Ghanaian Highlife",
            mood = "Celebratory",
            vocalPreference = "Male",
            language = "Twi",
            lyrics = "Akwaaba o, yɛbɛsa!",
            isInstrumental = false,
            status = "completed",
            audioUrl = "https://example.com/audio1.mp3",
            durationSeconds = 185,
            isFavorite = true,
            phaseVersion = "phase_1_preview",
            createdAtEpoch = 1000L,
            updatedAtEpoch = 1000L,
            syncStatus = LocalSongEntity.SYNC_STATUS_PENDING_INSERT
        )

        songDao.insertSong(songEntity)

        val retrieved = songDao.getSongById("test_song_1")
        assertNotNull(retrieved)
        assertEquals("Golden Stool Highlife", retrieved?.title)
        assertEquals(LocalSongEntity.SYNC_STATUS_PENDING_INSERT, retrieved?.syncStatus)

        // Verify observeSongs flow
        val songList = songDao.observeSongs("user_123").first()
        assertEquals(1, songList.size)
        assertEquals("test_song_1", songList[0].id)
    }

    @Test
    fun pendingSyncQuery_returnsOnlyUnsyncedItems() = runBlocking {
        val synced = LocalSongEntity(
            id = "song_synced",
            userId = "user_123",
            title = "Synced Track",
            description = "Already on cloud",
            genre = "Afrobeats",
            mood = "Energetic",
            vocalPreference = "Female",
            language = "English",
            lyrics = "",
            isInstrumental = false,
            status = "completed",
            audioUrl = "",
            durationSeconds = 120,
            isFavorite = false,
            phaseVersion = "phase_1_preview",
            createdAtEpoch = 1000L,
            updatedAtEpoch = 1000L,
            syncStatus = LocalSongEntity.SYNC_STATUS_SYNCED
        )
        val pending = synced.copy(
            id = "song_pending",
            title = "Pending Track",
            syncStatus = LocalSongEntity.SYNC_STATUS_PENDING_UPDATE
        )

        songDao.insertSong(synced)
        songDao.insertSong(pending)

        val pendingList = songDao.getPendingSyncSongs("user_123")
        assertEquals(1, pendingList.size)
        assertEquals("song_pending", pendingList[0].id)

        // Mark as synced
        songDao.markAsSynced("song_pending")
        val pendingAfterMark = songDao.getPendingSyncSongs("user_123")
        assertTrue(pendingAfterMark.isEmpty())
    }

    @Test
    fun repository_createSong_persistsWithPendingInsertStatus() = runBlocking {
        val song = Song(
            id = "repo_song_1",
            userId = "user_456",
            title = "Hiplife Genesis",
            genre = "Hiplife",
            mood = "Confident",
            vocalPreference = "Male",
            language = "English & Twi"
        )

        val result = songRepository.createSong(song)
        assertTrue(result.isSuccess)

        val fetched = songRepository.getSong("user_456", "repo_song_1")
        assertNotNull(fetched)
        assertEquals("Hiplife Genesis", fetched?.title)

        val pending = songRepository.getPendingSyncSongs("user_456")
        assertEquals(1, pending.size)
        assertEquals(LocalSongEntity.SYNC_STATUS_PENDING_INSERT, pending[0].syncStatus)
    }

    @Test
    fun repository_deleteSong_marksPendingDeleteIfPreviouslySynced() = runBlocking {
        val song = Song(
            id = "synced_song_to_delete",
            userId = "user_456",
            title = "Old Song",
            genre = "Afro-fusion"
        )
        songRepository.saveRemoteSongs(listOf(song))

        // When deleting a previously synced song, it should transition to PENDING_DELETE for Firestore sync
        val deleteResult = songRepository.deleteSong("user_456", "synced_song_to_delete")
        assertTrue(deleteResult.isSuccess)

        // Should no longer be returned by observeSongs or getSong
        val fetched = songRepository.getSong("user_456", "synced_song_to_delete")
        assertNull(fetched)

        val observed = songRepository.observeSongs("user_456").first()
        assertTrue(observed.none { it.id == "synced_song_to_delete" })

        // But still in pending sync queue as PENDING_DELETE so Firestore sync can purge remote doc
        val pending = songRepository.getPendingSyncSongs("user_456")
        assertEquals(1, pending.size)
        assertEquals(LocalSongEntity.SYNC_STATUS_PENDING_DELETE, pending[0].syncStatus)
    }

    @Test
    fun userProfileDao_insertAndObserveProfile() = runBlocking {
        val profileEntity = LocalUserProfileEntity(
            userId = "user_789",
            displayName = "Kwame Mensah",
            email = "kwame@example.com",
            avatarUrl = null,
            credits = 75,
            subscriptionTier = "creator_pro",
            preferredLanguage = "Twi",
            preferredGenre = "Ghanaian Highlife",
            createdAtEpoch = 2000L,
            updatedAtEpoch = 2000L,
            syncStatus = LocalUserProfileEntity.SYNC_STATUS_SYNCED
        )

        userProfileDao.insertOrUpdate(profileEntity)

        val retrieved = userProfileDao.getUserProfile("user_789")
        assertNotNull(retrieved)
        assertEquals("Kwame Mensah", retrieved?.displayName)
        assertEquals(75L, retrieved?.credits)
        assertEquals("Twi", retrieved?.preferredLanguage)

        // Update preferences
        userProfileDao.updatePreferences("user_789", "English", "Afrobeats", System.currentTimeMillis())
        val updated = userProfileDao.getUserProfile("user_789")
        assertEquals("English", updated?.preferredLanguage)
        assertEquals("Afrobeats", updated?.preferredGenre)
        assertEquals(LocalUserProfileEntity.SYNC_STATUS_PENDING_UPDATE, updated?.syncStatus)
    }

    @Test
    fun roomUserRepository_managesProfileAndPendingSync() = runBlocking {
        val profile = UserProfile(
            userId = "user_abc",
            displayName = "Ama Serwaa",
            email = "ama@example.com",
            credits = 100,
            subscriptionTier = "free",
            preferredLanguage = "English",
            preferredGenre = "Afro-fusion"
        )

        userRepository.createOrUpdateProfile(profile)

        val fetched = userRepository.getUserProfile("user_abc")
        assertNotNull(fetched)
        assertEquals("Ama Serwaa", fetched?.displayName)

        val pending = userRepository.getPendingSyncProfiles()
        assertEquals(1, pending.size)
        assertEquals("user_abc", pending[0].userId)

        // Mark as synced
        userRepository.markAsSynced("user_abc")
        val pendingAfterSync = userRepository.getPendingSyncProfiles()
        assertTrue(pendingAfterSync.isEmpty())
    }

    @Test
    fun videoProjectDao_insertAndRetrieveDraft() = runBlocking {
        val videoDao = database.videoProjectDao()
        val videoEntity = com.osei.aimusicstudio.data.local.entity.LocalVideoProjectEntity(
            id = "vid_101",
            userId = "user_abc",
            title = "Accra Street Vibe",
            prompt = "Cyberpunk trotro cruising through Osu night market",
            visualStyle = "Accra Neon Cyberpunk",
            aspectRatio = "9:16",
            linkedSongId = "song_1",
            linkedSongTitle = "Hiplife Anthem",
            localAudioUri = "content://media/audio/1",
            localVideoUri = null,
            status = "draft",
            durationSeconds = 0L,
            phaseVersion = "phase_4_foundation",
            createdAtEpoch = System.currentTimeMillis(),
            updatedAtEpoch = System.currentTimeMillis()
        )

        videoDao.insertVideoProject(videoEntity)

        val retrieved = videoDao.getVideoProject("user_abc", "vid_101")
        assertNotNull(retrieved)
        assertEquals("Accra Street Vibe", retrieved?.title)
        assertEquals("draft", retrieved?.status)
        assertEquals("LOCAL_ONLY", retrieved?.syncStatus)
        assertEquals("9:16", retrieved?.aspectRatio)

        val projects = videoDao.observeVideoProjects("user_abc").first()
        assertEquals(1, projects.size)
        assertEquals("vid_101", projects[0].id)
    }

    @Test
    fun migration1To2_executesCleanlyAndPreservesExistingData() {
        val helper = androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory()
            .create(
                androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(ApplicationProvider.getApplicationContext())
                    .name("test_migration.db")
                    .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(1) {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            // Version 1 schema for local_songs
                            db.execSQL("""
                                CREATE TABLE IF NOT EXISTS local_songs (
                                    id TEXT NOT NULL PRIMARY KEY,
                                    userId TEXT NOT NULL,
                                    title TEXT NOT NULL,
                                    description TEXT NOT NULL,
                                    genre TEXT NOT NULL,
                                    mood TEXT NOT NULL,
                                    vocalPreference TEXT NOT NULL,
                                    language TEXT NOT NULL,
                                    lyrics TEXT NOT NULL,
                                    isInstrumental INTEGER NOT NULL,
                                    status TEXT NOT NULL,
                                    audioUrl TEXT NOT NULL,
                                    durationSeconds INTEGER NOT NULL,
                                    isFavorite INTEGER NOT NULL,
                                    phaseVersion TEXT NOT NULL,
                                    createdAtEpoch INTEGER NOT NULL,
                                    updatedAtEpoch INTEGER NOT NULL,
                                    syncStatus TEXT NOT NULL,
                                    lastSyncedAtEpoch INTEGER
                                )
                            """.trimIndent())
                            // Insert a v1 draft song
                            db.execSQL("""
                                INSERT INTO local_songs (
                                    id, userId, title, description, genre, mood, vocalPreference, language,
                                    lyrics, isInstrumental, status, audioUrl, durationSeconds, isFavorite,
                                    phaseVersion, createdAtEpoch, updatedAtEpoch, syncStatus
                                ) VALUES (
                                    'v1_song', 'user_1', 'Kumasi Dawn', 'Highlife draft', 'Ghanaian Highlife', 'Mellow',
                                    'Male', 'Twi', 'Me do wo', 0, 'draft', '', 0, 0, 'phase_3_offline_first', 100, 100, 'SYNCED'
                                )
                            """.trimIndent())
                        }

                        override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                    })
                    .build()
            )

        val db = helper.writableDatabase

        // Execute MIGRATION_1_2
        StudioDatabase.MIGRATION_1_2.migrate(db)

        // Verify tempo column exists in local_songs and defaults to 'Moderate (110 BPM)'
        val cursor = db.query("SELECT id, title, tempo, status FROM local_songs WHERE id = 'v1_song'")
        assertTrue(cursor.moveToFirst())
        assertEquals("Kumasi Dawn", cursor.getString(1))
        assertEquals("Moderate (110 BPM)", cursor.getString(2))
        assertEquals("draft", cursor.getString(3))
        cursor.close()

        // Verify local_video_projects table was created and accepts inserts
        db.execSQL("""
            INSERT INTO local_video_projects (
                id, userId, title, prompt, visualStyle, aspectRatio, status, durationSeconds,
                phaseVersion, createdAtEpoch, updatedAtEpoch, syncStatus
            ) VALUES (
                'v1_vid', 'user_1', 'Video Draft 1', 'Neon sunset in Accra', 'Afrofuturism 3D', '9:16',
                'draft', 0, 'phase_4_foundation', 200, 200, 'LOCAL_ONLY'
            )
        """.trimIndent())

        val videoCursor = db.query("SELECT id, title, visualStyle, status FROM local_video_projects WHERE id = 'v1_vid'")
        assertTrue(videoCursor.moveToFirst())
        assertEquals("Video Draft 1", videoCursor.getString(1))
        assertEquals("Afrofuturism 3D", videoCursor.getString(2))
        assertEquals("draft", videoCursor.getString(3))
        videoCursor.close()

        db.close()
    }

    @Test
    fun projectDao_insertQueryDeleteAudioProject() = runBlocking {
        val projectDao = database.projectDao()
        val audioEntity = com.osei.aimusicstudio.data.local.entity.LocalAudioProjectEntity(
            id = "audio_proj_1",
            userId = "user_proj_test",
            title = "Highlife Groove",
            prompt = "Authentic palmwine guitar and brass",
            genre = "Ghanaian Highlife",
            mood = "Joyful",
            tempo = "Moderate (110 BPM)",
            vocalPreference = "Male",
            language = "Twi",
            lyrics = "Odo yewu",
            isInstrumental = false,
            localAudioUri = "content://media/audio/1",
            audioUrl = "",
            status = "draft",
            durationSeconds = 180,
            isFavorite = true,
            phaseVersion = "phase_4_foundation",
            createdAtEpoch = 1000L,
            updatedAtEpoch = 1000L
        )

        // 1. Insert
        projectDao.insertAudioProject(audioEntity)

        // 2. Query
        val retrieved = projectDao.getAudioProject("user_proj_test", "audio_proj_1")
        assertNotNull(retrieved)
        assertEquals("Highlife Groove", retrieved?.title)
        assertEquals("Joyful", retrieved?.mood)
        assertEquals("draft", retrieved?.status)
        assertEquals(true, retrieved?.isFavorite)

        val retrievedById = projectDao.getAudioProjectById("audio_proj_1")
        assertNotNull(retrievedById)
        assertEquals("Highlife Groove", retrievedById?.title)

        val observedList = projectDao.observeAudioProjects("user_proj_test").first()
        assertEquals(1, observedList.size)
        assertEquals("audio_proj_1", observedList[0].id)

        // 3. Delete
        projectDao.deleteAudioProjectById("audio_proj_1")
        val afterDelete = projectDao.getAudioProjectById("audio_proj_1")
        assertNull(afterDelete)
    }

    @Test
    fun projectDao_insertQueryDeleteVideoProject() = runBlocking {
        val projectDao = database.projectDao()
        val videoEntity = com.osei.aimusicstudio.data.local.entity.LocalVideoProjectEntity(
            id = "video_proj_10",
            userId = "user_proj_test",
            title = "Afrobeats Accra Visualizer",
            prompt = "Night market neon lights with dancers",
            visualStyle = "Accra Neon Cyberpunk",
            aspectRatio = "9:16",
            linkedSongId = "audio_proj_1",
            linkedSongTitle = "Highlife Groove",
            localAudioUri = "content://media/audio/1",
            localVideoUri = "content://media/video/10",
            status = "draft",
            durationSeconds = 30,
            phaseVersion = "phase_4_foundation",
            createdAtEpoch = 2000L,
            updatedAtEpoch = 2000L
        )

        // 1. Insert
        projectDao.insertVideoProject(videoEntity)

        // 2. Query
        val retrieved = projectDao.getVideoProject("user_proj_test", "video_proj_10")
        assertNotNull(retrieved)
        assertEquals("Afrobeats Accra Visualizer", retrieved?.title)
        assertEquals("Accra Neon Cyberpunk", retrieved?.visualStyle)
        assertEquals("draft", retrieved?.status)

        val retrievedById = projectDao.getVideoProjectById("video_proj_10")
        assertNotNull(retrievedById)
        assertEquals("Afrobeats Accra Visualizer", retrievedById?.title)

        val observedList = projectDao.observeVideoProjects("user_proj_test").first()
        assertEquals(1, observedList.size)
        assertEquals("video_proj_10", observedList[0].id)

        // 3. Delete
        projectDao.deleteVideoProject("user_proj_test", "video_proj_10")
        val afterDelete = projectDao.getVideoProject("user_proj_test", "video_proj_10")
        assertNull(afterDelete)
    }

    @Test
    fun audioProjectDao_insertQueryDelete() = runBlocking {
        val audioDao = database.audioProjectDao()
        val entity1 = com.osei.aimusicstudio.data.local.entity.LocalAudioProjectEntity(
            id = "audio_alpha",
            userId = "user_alpha",
            title = "Afro Fusion Draft",
            genre = "Afrobeats",
            status = "draft",
            createdAtEpoch = 100L,
            updatedAtEpoch = 100L
        )
        val entity2 = com.osei.aimusicstudio.data.local.entity.LocalAudioProjectEntity(
            id = "audio_beta",
            userId = "user_alpha",
            title = "Asakaa Drill",
            genre = "Asakaa Drill",
            status = "draft",
            createdAtEpoch = 200L,
            updatedAtEpoch = 200L
        )

        // Insert batch
        audioDao.insertAudioProjects(listOf(entity1, entity2))

        val list = audioDao.observeAudioProjects("user_alpha").first()
        assertEquals(2, list.size)

        val byStatus = audioDao.observeAudioProjectsByStatus("user_alpha", "draft").first()
        assertEquals(2, byStatus.size)

        // Delete single
        audioDao.deleteAudioProjectById("audio_alpha")
        val remaining = audioDao.observeAudioProjects("user_alpha").first()
        assertEquals(1, remaining.size)
        assertEquals("audio_beta", remaining[0].id)

        // Clear
        audioDao.clearAudioProjects("user_alpha")
        val emptyList = audioDao.observeAudioProjects("user_alpha").first()
        assertTrue(emptyList.isEmpty())
    }

    @Test
    fun migration2To3_executesCleanlyAndPreservesExistingData() {
        val helper = androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory()
            .create(
                androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(ApplicationProvider.getApplicationContext())
                    .name("test_migration_2_3.db")
                    .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(2) {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            // Version 2 schema: local_songs and local_video_projects
                            db.execSQL("""
                                CREATE TABLE IF NOT EXISTS local_songs (
                                    id TEXT NOT NULL PRIMARY KEY,
                                    userId TEXT NOT NULL,
                                    title TEXT NOT NULL,
                                    description TEXT NOT NULL,
                                    genre TEXT NOT NULL,
                                    mood TEXT NOT NULL,
                                    vocalPreference TEXT NOT NULL,
                                    language TEXT NOT NULL,
                                    tempo TEXT NOT NULL,
                                    lyrics TEXT NOT NULL,
                                    isInstrumental INTEGER NOT NULL,
                                    status TEXT NOT NULL,
                                    audioUrl TEXT NOT NULL,
                                    durationSeconds INTEGER NOT NULL,
                                    isFavorite INTEGER NOT NULL,
                                    phaseVersion TEXT NOT NULL,
                                    createdAtEpoch INTEGER NOT NULL,
                                    updatedAtEpoch INTEGER NOT NULL,
                                    syncStatus TEXT NOT NULL,
                                    lastSyncedAtEpoch INTEGER
                                )
                            """.trimIndent())
                            db.execSQL("""
                                CREATE TABLE IF NOT EXISTS local_video_projects (
                                    id TEXT NOT NULL PRIMARY KEY,
                                    userId TEXT NOT NULL,
                                    title TEXT NOT NULL,
                                    prompt TEXT NOT NULL,
                                    visualStyle TEXT NOT NULL,
                                    aspectRatio TEXT NOT NULL,
                                    linkedSongId TEXT,
                                    linkedSongTitle TEXT,
                                    localAudioUri TEXT,
                                    localVideoUri TEXT,
                                    status TEXT NOT NULL,
                                    durationSeconds INTEGER NOT NULL,
                                    phaseVersion TEXT NOT NULL,
                                    createdAtEpoch INTEGER NOT NULL,
                                    updatedAtEpoch INTEGER NOT NULL,
                                    syncStatus TEXT NOT NULL
                                )
                            """.trimIndent())
                        }

                        override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                    })
                    .build()
            )

        val db = helper.writableDatabase

        // Execute MIGRATION_2_3
        StudioDatabase.MIGRATION_2_3.migrate(db)

        // Verify local_audio_projects table was created and accepts inserts
        db.execSQL("""
            INSERT INTO local_audio_projects (
                id, userId, title, prompt, genre, mood, tempo, vocalPreference, language,
                lyrics, isInstrumental, localAudioUri, audioUrl, status, durationSeconds,
                isFavorite, phaseVersion, createdAtEpoch, updatedAtEpoch, syncStatus
            ) VALUES (
                'v3_audio', 'user_m', 'Cape Coast Sunrise', 'Acoustic guitar and ocean sounds',
                'Ghanaian Highlife', 'Peaceful', 'Moderate (110 BPM)', 'Female', 'Fante',
                'Nsuo tsen', 0, NULL, '', 'draft', 120, 0, 'phase_4_foundation', 300, 300, 'LOCAL_ONLY'
            )
        """.trimIndent())

        val cursor = db.query("SELECT id, title, genre, tempo, status, syncStatus FROM local_audio_projects WHERE id = 'v3_audio'")
        assertTrue(cursor.moveToFirst())
        assertEquals("Cape Coast Sunrise", cursor.getString(1))
        assertEquals("Ghanaian Highlife", cursor.getString(2))
        assertEquals("Moderate (110 BPM)", cursor.getString(3))
        assertEquals("draft", cursor.getString(4))
        assertEquals("LOCAL_ONLY", cursor.getString(5))
        cursor.close()

        db.close()
    }
}

