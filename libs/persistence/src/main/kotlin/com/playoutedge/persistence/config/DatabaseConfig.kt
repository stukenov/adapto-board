package com.playoutedge.persistence.config

data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String,
    val maxPoolSize: Int = 10,
    val minIdle: Int = 2,
    val idleTimeout: Long = 600000, // 10 minutes
    val connectionTimeout: Long = 30000, // 30 seconds
    val maxLifetime: Long = 1800000 // 30 minutes
) {
    companion object {
        fun fromEnvironment(): DatabaseConfig = DatabaseConfig(
            url = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/playoutedge",
            user = System.getenv("DATABASE_USER") ?: "dev",
            password = System.getenv("DATABASE_PASSWORD") ?: "dev"
        )
    }
}
