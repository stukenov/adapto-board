package com.playoutedge.player

import android.app.Application
import com.playoutedge.player.config.ConfigManager
import com.playoutedge.player.config.ConfigRepository
import com.playoutedge.player.network.ApiClient
import com.playoutedge.player.network.NetworkMonitor
import com.playoutedge.player.network.PlayerApiClient
import com.playoutedge.player.network.TokenStorage
import com.playoutedge.player.overlay.OverlaySseClient
import com.playoutedge.player.playback.PlayerManager
import com.playoutedge.player.playlist.PlaylistManager
import com.playoutedge.player.playlist.PlaylistRepository
import com.playoutedge.player.telemetry.AsrunCollector
import com.playoutedge.player.telemetry.HeartbeatManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Application class providing dependency injection.
 */
class PlayerApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Core infrastructure
    lateinit var tokenStorage: TokenStorage
        private set
    lateinit var networkMonitor: NetworkMonitor
        private set
    lateinit var apiClient: ApiClient
        private set
    lateinit var playerApiClient: PlayerApiClient
        private set

    // Config
    lateinit var configRepository: ConfigRepository
        private set
    lateinit var configManager: ConfigManager
        private set

    // Playlist
    lateinit var playlistRepository: PlaylistRepository
        private set
    lateinit var playlistManager: PlaylistManager
        private set

    // Playback
    lateinit var playerManager: PlayerManager
        private set

    // Telemetry
    lateinit var heartbeatManager: HeartbeatManager
        private set
    lateinit var asrunCollector: AsrunCollector
        private set

    // Overlay
    lateinit var overlaySseClient: OverlaySseClient
        private set

    val appVersion: String by lazy {
        try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize core infrastructure
        tokenStorage = TokenStorage(this)
        networkMonitor = NetworkMonitor(this)
        apiClient = ApiClient(tokenStorage)
        playerApiClient = PlayerApiClient(apiClient)

        // Initialize config
        configRepository = ConfigRepository(this)
        configManager = ConfigManager(playerApiClient, configRepository, applicationScope)

        // Initialize playlist
        playlistRepository = PlaylistRepository(this)
        playlistManager = PlaylistManager(
            playerApiClient,
            playlistRepository,
            configManager,
            applicationScope
        )

        // Initialize playback
        playerManager = PlayerManager(this, playlistManager, applicationScope)

        // Initialize telemetry
        heartbeatManager = HeartbeatManager(
            playerApiClient,
            configManager,
            tokenStorage,
            applicationScope,
            appVersion
        )
        asrunCollector = AsrunCollector(
            this,
            playerApiClient,
            configManager,
            applicationScope
        )

        // Initialize overlay
        overlaySseClient = OverlaySseClient(playerApiClient, applicationScope)
    }
}

/**
 * Extension to get PlayerApp from any Context.
 */
val android.content.Context.playerApp: PlayerApp
    get() = applicationContext as PlayerApp
