package com.example.fitapp.ai.ui

sealed interface AiUiState {
    object Idle : AiUiState

    object Loading : AiUiState

    data class Success<T>(val data: T) : AiUiState

    data class Error(
        val type: AiErrorType,
        val message: String? = null,
    ) : AiUiState

    data class Unavailable(
        val reason: AiUnavailableReason,
        val message: String? = null,
    ) : AiUiState
}

enum class AiErrorType {
    KeyInvalid,
    KeyMissing,
    Network,
    RateLimit,
    ProviderUnavailable,
    Unknown,
}

enum class AiUnavailableReason {
    NotConfigured,
    DisabledByUser,
    Restricted,
}
