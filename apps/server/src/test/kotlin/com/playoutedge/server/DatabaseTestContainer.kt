package com.playoutedge.server

import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
abstract class DatabaseTestContainer {

    companion object {
        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true)

        private var initialized = false

        @JvmStatic
        fun initDatabase(): Database {
            if (!initialized) {
                postgres.start()

                // Run Flyway migrations
                Flyway.configure()
                    .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
                    .locations("classpath:db/migration")
                    .load()
                    .migrate()

                initialized = true
            }

            return Database.connect(
                url = postgres.jdbcUrl,
                driver = "org.postgresql.Driver",
                user = postgres.username,
                password = postgres.password
            )
        }

        @JvmStatic
        fun getJdbcUrl(): String = postgres.jdbcUrl

        @JvmStatic
        fun getUsername(): String = postgres.username

        @JvmStatic
        fun getPassword(): String = postgres.password
    }
}
