package com.example.fitapp.core.health

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registry for managing multiple API health checkers
 */
@Singleton
class ApiHealthRegistry
    @Inject
    constructor(
        private val geminiHealthChecker: GeminiHealthChecker,
        // TODO: Add other health checkers (Perplexity, Health Connect, etc.)
    ) {
        private val checkers: List<HealthCheckable> =
            listOf(
                geminiHealthChecker,
                // TODO: Add perplexityHealthChecker, healthConnectChecker, etc.
            )

        /**
         * Get all registered health checkers
         */
        fun getAllCheckers(): List<HealthCheckable> = checkers

        /**
         * Get a specific checker by provider name
         */
        fun getChecker(providerName: String): HealthCheckable? = checkers.find { it.providerName == providerName }

        /**
         * Perform health check on all providers
         */
        suspend fun checkAllHealth(): Map<String, HealthStatus> {
            return checkers.associate { checker ->
                checker.providerName to checker.checkHealth()
            }
        }

        /**
         * Flow of combined health status from all providers
         */
        fun healthStatusFlow(): Flow<Map<String, HealthStatus>> {
            return if (checkers.isEmpty()) {
                kotlinx.coroutines.flow.flowOf(emptyMap())
            } else if (checkers.size == 1) {
                checkers[0].healthStatusFlow().map { status ->
                    mapOf(status.provider to status)
                }
            } else {
                combine(checkers.map { it.healthStatusFlow() }) { statusArray ->
                    statusArray.associate { status ->
                        status.provider to status
                    }
                }
            }
        }
    }
