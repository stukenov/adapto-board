package com.playoutedge.server

import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.testcontainers.containers.PostgreSQLContainer

/**
 * Base class for tests that need a PostgreSQL database.
 * Uses a singleton testcontainer that's reused across all test classes.
 */
abstract class DatabaseTestContainer {

    companion object {
        // Singleton container - starts once and is reused
        private val postgres: PostgreSQLContainer<*> by lazy {
            PostgreSQLContainer("postgres:16-alpine")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                .apply { start() }
        }

        private var database: Database? = null
        private val lock = Any()

        @JvmStatic
        fun initDatabase(): Database {
            synchronized(lock) {
                // Ensure container is running
                if (!postgres.isRunning) {
                    postgres.start()
                }

                // Check if we already have a valid database connection
                if (database != null && TransactionManager.defaultDatabase == database) {
                    return database!!
                }

                // Run Flyway migrations
                Flyway.configure()
                    .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
                    .locations("classpath:db/migration")
                    .cleanDisabled(false)
                    .load()
                    .apply {
                        // Clean and migrate to ensure fresh state
                        clean()
                        migrate()
                    }

                // Connect to database and make it the default
                database = Database.connect(
                    url = postgres.jdbcUrl,
                    driver = "org.postgresql.Driver",
                    user = postgres.username,
                    password = postgres.password
                )

                // Set this as the default database for transactions
                TransactionManager.defaultDatabase = database

                return database!!
            }
        }

        @JvmStatic
        fun getJdbcUrl(): String {
            synchronized(lock) {
                if (!postgres.isRunning) {
                    postgres.start()
                }
                return postgres.jdbcUrl
            }
        }

        @JvmStatic
        fun getUsername(): String = postgres.username

        @JvmStatic
        fun getPassword(): String = postgres.password
    }
}
