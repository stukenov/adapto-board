package com.playoutedge.server

import com.playoutedge.auth.AuthConfig
import com.playoutedge.auth.JwtService
import com.playoutedge.auth.PasswordService
import com.playoutedge.persistence.config.DatabaseConfig
import com.playoutedge.persistence.config.DatabaseFactory
import com.playoutedge.server.plugins.RequestIdPlugin
import com.playoutedge.server.plugins.TenantPlugin
import com.playoutedge.server.plugins.configureErrorHandling
import com.playoutedge.server.plugins.configureJwtAuth
import com.playoutedge.server.routes.authRoutes
import com.playoutedge.server.routes.deviceAuthRoutes
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

    // Install plugins
    install(RequestIdPlugin)
    install(ContentNegotiation) { json() }
    configureErrorHandling()
    configureJwtAuth(authConfig)
    install(TenantPlugin)

    // Initialize database
    configureDatabase()

    // Configure routes
    routing {
        authRoutes(jwtService, passwordService, authConfig)
        deviceAuthRoutes(jwtService, authConfig)
    }
}

fun Application.configureDatabase() {
    val config = DatabaseConfig.fromEnvironment()
    DatabaseFactory.init(config)

    // Graceful shutdown
    environment.monitor.subscribe(ApplicationStopped) {
        DatabaseFactory.close()
    }
}
