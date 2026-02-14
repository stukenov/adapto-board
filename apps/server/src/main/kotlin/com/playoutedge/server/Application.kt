package com.playoutedge.server

import com.playoutedge.auth.AuthConfig
import com.playoutedge.auth.JwtService
import com.playoutedge.auth.NoOpEmailService
import com.playoutedge.auth.PasswordService
import com.playoutedge.server.services.AlertService
import com.playoutedge.server.services.AsrunService
import com.playoutedge.server.services.AuditService
import com.playoutedge.server.services.ChannelService
import com.playoutedge.server.services.DeviceActionService
import com.playoutedge.server.services.DeviceHealthCheckService
import com.playoutedge.server.services.DeviceService
import com.playoutedge.server.services.MaintenanceService
import com.playoutedge.server.services.RegistrationService
import com.playoutedge.server.services.WebhookService
import com.playoutedge.server.services.OverlayService
import com.playoutedge.server.services.OverlaySubscribers
import com.playoutedge.server.services.OverlayPollService
import com.playoutedge.server.services.PlaylistService
import com.playoutedge.server.services.ScheduleService
import com.playoutedge.server.services.ScheduleTemplateService
import com.playoutedge.server.services.TtsService
import com.playoutedge.persistence.config.DatabaseConfig
import com.playoutedge.persistence.config.DatabaseFactory
import com.playoutedge.persistence.repositories.impl.AlertRepositoryImpl
import com.playoutedge.persistence.repositories.impl.UserRepositoryImpl
import com.playoutedge.persistence.repositories.impl.AssetRepositoryImpl
import com.playoutedge.persistence.repositories.impl.AsrunRepositoryImpl
import com.playoutedge.persistence.repositories.impl.AuditRepositoryImpl
import com.playoutedge.persistence.repositories.impl.ChannelRepositoryImpl
import com.playoutedge.persistence.repositories.impl.DeviceActionRepositoryImpl
import com.playoutedge.persistence.repositories.impl.DeviceRepositoryImpl
import com.playoutedge.persistence.repositories.impl.OverlayRepositoryImpl
import com.playoutedge.persistence.repositories.impl.QuotaServiceImpl
import com.playoutedge.persistence.repositories.impl.JobRepositoryImpl
import com.playoutedge.persistence.repositories.impl.ScheduleRepositoryImpl
import com.playoutedge.persistence.repositories.impl.TenantRepositoryImpl
import com.playoutedge.persistence.repositories.impl.WebhookLogRepositoryImpl
import com.playoutedge.persistence.repositories.impl.RefreshTokenRepositoryImpl
import com.playoutedge.persistence.repositories.impl.ApiKeyRepositoryImpl
import com.playoutedge.persistence.repositories.impl.DeviceLogRepositoryImpl
import com.playoutedge.persistence.repositories.impl.OnboardingStateRepositoryImpl
import com.playoutedge.server.jobs.AsrunCleanupJobHandler
import com.playoutedge.server.jobs.CleanupJobHandler
import com.playoutedge.server.jobs.JobScheduler
import com.playoutedge.server.plugins.AdminSessionPlugin
import com.playoutedge.server.plugins.MaintenancePlugin
import com.playoutedge.server.plugins.RateLimitPlugin
import com.playoutedge.server.plugins.RequestIdPlugin
import com.playoutedge.server.plugins.TenantPlugin
import com.playoutedge.server.plugins.configureErrorHandling
import com.playoutedge.server.plugins.configureJwtAuth
import com.playoutedge.server.routes.admin.adminAssetRoutes
import com.playoutedge.server.routes.admin.adminAuthRoutes
import com.playoutedge.server.routes.admin.adminChannelRoutes
import com.playoutedge.server.routes.admin.adminDeviceRoutes
import com.playoutedge.server.routes.admin.adminHomeRoutes
import com.playoutedge.server.routes.admin.adminOnboardingRoutes
import com.playoutedge.server.routes.admin.adminOverlayRoutes
import com.playoutedge.server.routes.admin.adminReportsRoutes
import com.playoutedge.server.routes.admin.adminScheduleRoutes
import com.playoutedge.server.routes.admin.adminSettingsRoutes
import com.playoutedge.server.routes.admin.adminStaticRoutes
import com.playoutedge.server.routes.alertsRoutes
import com.playoutedge.server.routes.assetsRoutes
import com.playoutedge.server.routes.asrunRoutes
import com.playoutedge.server.routes.auditRoutes
import com.playoutedge.server.routes.healthRoutes
import com.playoutedge.server.routes.authRoutes
import com.playoutedge.server.routes.channelsRoutes
import com.playoutedge.server.routes.deviceAuthRoutes
import com.playoutedge.server.routes.devicesRoutes
import com.playoutedge.server.routes.jobRoutes
import com.playoutedge.server.routes.maintenanceRoutes
import com.playoutedge.server.routes.overlayRoutes
import com.playoutedge.server.routes.schedulesRoutes
import com.playoutedge.server.routes.storageRoutes
import com.playoutedge.server.routes.webhookRoutes
import com.playoutedge.server.routes.player.devicePlayerRoutes
import com.playoutedge.server.routes.player.overlayStreamRoutes
import com.playoutedge.server.routes.player.playerAsrunRoutes
import com.playoutedge.server.routes.player.playlistRoutes
import com.playoutedge.server.routes.landing.publicLandingRoutes
import com.playoutedge.server.routes.landing.publicSignupRoutes
import com.playoutedge.server.routes.embedRoutes
import com.playoutedge.server.routes.publicPollRoutes
import com.playoutedge.server.routes.docsRoutes
import com.playoutedge.server.routes.llmsTxtRoutes
import com.playoutedge.server.mcp.McpServer
import com.playoutedge.server.mcp.McpToolRegistry
import com.playoutedge.server.mcp.mcpRoutes
import com.playoutedge.server.mcp.registerAllTools
import com.playoutedge.server.plugins.configureOpenApi
import com.playoutedge.storage.AssetUploadService
import com.playoutedge.storage.LocalStorageService
import com.playoutedge.storage.StorageConfig
import com.playoutedge.storage.StorageMode
import com.playoutedge.storage.StorageServiceFactory
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Auth services
    val authConfig = AuthConfig.fromEnvironment()
    val jwtService = JwtService(authConfig)
    val passwordService = PasswordService()

    // Storage services
    val storageConfig = StorageConfig.fromEnvironment()
    val storageService = StorageServiceFactory.create(storageConfig)
    val localStorageService = if (storageConfig.mode == StorageMode.LOCAL) {
        storageService as LocalStorageService
    } else null
    val assetUploadService = AssetUploadService(storageService)

    // Repositories
    val assetRepository = AssetRepositoryImpl()
    val deviceRepository = DeviceRepositoryImpl()
    val deviceActionRepository = DeviceActionRepositoryImpl()
    val channelRepository = ChannelRepositoryImpl()
    val scheduleRepository = ScheduleRepositoryImpl()
    val overlayRepository = OverlayRepositoryImpl()
    val auditRepository = AuditRepositoryImpl()
    val asrunRepository = AsrunRepositoryImpl()
    val alertRepository = AlertRepositoryImpl()
    val tenantRepository = TenantRepositoryImpl()
    val userRepository = UserRepositoryImpl()
    val webhookLogRepository = WebhookLogRepositoryImpl()
    val jobRepository = JobRepositoryImpl()
    val quotaService = QuotaServiceImpl(assetRepository, deviceRepository)
    val refreshTokenRepository = RefreshTokenRepositoryImpl()
    val apiKeyRepository = ApiKeyRepositoryImpl()
    val onboardingStateRepository = OnboardingStateRepositoryImpl()
    val deviceLogRepository = DeviceLogRepositoryImpl()

    // Domain services
    val channelService = ChannelService(channelRepository)
    val scheduleService = ScheduleService(scheduleRepository, assetRepository)
    val playlistService = PlaylistService(scheduleRepository, assetRepository, storageService)
    val deviceService = DeviceService(deviceRepository)
    val deviceActionService = DeviceActionService(deviceActionRepository)
    val overlaySubscribers = OverlaySubscribers()
    val overlayService = OverlayService(overlayRepository, overlaySubscribers)
    val webhookService = WebhookService(webhookLogRepository, overlayService)
    val auditService = AuditService(auditRepository)
    val asrunService = AsrunService(asrunRepository)
    val alertService = AlertService(alertRepository)
    val emailService = NoOpEmailService()
    val registrationService = RegistrationService(tenantRepository, userRepository, passwordService)
    val maintenanceService = MaintenanceService()
    val ttsService = TtsService(storageService)
    val scheduleTemplateService = ScheduleTemplateService()
    val overlayPollService = OverlayPollService(overlayRepository, overlaySubscribers)
    val deviceHealthCheckService = DeviceHealthCheckService(deviceRepository, alertService)

    // Install plugins
    install(RequestIdPlugin)
    install(ContentNegotiation) { json() }
    configureErrorHandling()
    configureJwtAuth(authConfig)
    install(TenantPlugin)
    install(RateLimitPlugin)
    install(MaintenancePlugin) {
        this.maintenanceService = maintenanceService
    }
    install(AdminSessionPlugin) {
        this.jwtService = jwtService
        this.authConfig = authConfig
    }

    // Initialize database
    configureDatabase()

    // Job scheduler (must be after database init)
    val jobScheduler = JobScheduler(jobRepository)
    jobScheduler.register(CleanupJobHandler(auditRepository, webhookLogRepository))
    jobScheduler.register(AsrunCleanupJobHandler(asrunRepository))
    jobScheduler.start()

    // MCP server
    val mcpToolRegistry = McpToolRegistry()
    registerAllTools(
        registry = mcpToolRegistry,
        channelService = channelService,
        deviceService = deviceService,
        deviceActionService = deviceActionService,
        scheduleService = scheduleService,
        overlayService = overlayService,
        auditService = auditService,
        asrunService = asrunService,
        alertService = alertService,
        assetRepository = assetRepository
    )
    val mcpServer = McpServer(mcpToolRegistry)

    // OpenAPI spec
    configureOpenApi()

    // Start overlay REST pull polling service
    overlayPollService.start()

    // Start device health check service
    deviceHealthCheckService.start()

    // Graceful shutdown for all background services
    environment.monitor.subscribe(ApplicationStopped) {
        overlayPollService.stop()
        deviceHealthCheckService.stop()
        ttsService.close()
        kotlinx.coroutines.runBlocking { jobScheduler.stop() }
        DatabaseFactory.close()
    }

    // Configure routes
    routing {
        // Public routes (before admin to avoid auth)
        publicLandingRoutes()
        publicSignupRoutes(registrationService, jwtService)
        embedRoutes(channelRepository, playlistService, overlayRepository)
        publicPollRoutes(overlayRepository, overlaySubscribers)
        docsRoutes()
        llmsTxtRoutes()

        // Admin web routes (SSR)
        adminAuthRoutes(userRepository, jwtService, passwordService, tenantRepository, emailService)
        adminHomeRoutes(deviceRepository, alertRepository, channelRepository, assetRepository, auditRepository)
        adminChannelRoutes(channelRepository, deviceRepository, scheduleRepository, storageService, scheduleService)
        adminDeviceRoutes(deviceRepository, channelRepository, deviceLogRepository)
        adminAssetRoutes(assetRepository, quotaService, assetUploadService)
        adminOverlayRoutes(overlayRepository, channelRepository, webhookLogRepository, webhookService)
        adminReportsRoutes(asrunRepository, auditRepository, deviceRepository, channelRepository)
        adminSettingsRoutes(userRepository, assetRepository, deviceRepository, passwordService, tenantRepository, apiKeyRepository)
        adminOnboardingRoutes(assetRepository, channelRepository, deviceRepository, tenantRepository, assetUploadService, scheduleRepository, onboardingStateRepository)
        adminScheduleRoutes(channelRepository, scheduleRepository, assetRepository, scheduleService, ttsService)
        adminStaticRoutes()

        // API routes
        healthRoutes(storageService)
        authRoutes(jwtService, passwordService, authConfig, refreshTokenRepository)
        deviceAuthRoutes(jwtService, authConfig, refreshTokenRepository)
        assetsRoutes(assetRepository, quotaService, assetUploadService, storageService)
        storageRoutes(storageConfig, localStorageService, assetRepository)
        channelsRoutes(channelService)
        schedulesRoutes(scheduleService)
        devicesRoutes(deviceService, deviceActionService)
        overlayRoutes(overlayService)
        webhookRoutes(webhookService)
        auditRoutes(auditService)
        asrunRoutes(asrunService)
        alertsRoutes(alertService)
        maintenanceRoutes(maintenanceService)
        jobRoutes(jobRepository)
        playlistRoutes(playlistService)
        playerAsrunRoutes(asrunService)
        devicePlayerRoutes(deviceRepository, deviceActionService)
        overlayStreamRoutes(overlayService)
        mcpRoutes(mcpServer)
    }
}

fun Application.configureDatabase() {
    val config = DatabaseConfig.fromEnvironment()
    DatabaseFactory.init(config)
}
