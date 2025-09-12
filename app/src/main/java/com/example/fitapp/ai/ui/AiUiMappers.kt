package com.example.fitapp.ai.ui

import com.example.fitapp.infrastructure.ai.AiError

fun AiError.toUiState(): AiUiState.Error =
    when (this) {
        is AiError.Auth -> AiUiState.Error(AiErrorType.KeyInvalid, this.code)
        is AiError.Network -> AiUiState.Error(AiErrorType.Network, this.code)
        is AiError.RateLimit -> AiUiState.Error(AiErrorType.RateLimit, this.code)
        is AiError.Quota -> AiUiState.Error(AiErrorType.RateLimit, this.code)
        is AiError.Server -> AiUiState.Error(AiErrorType.ProviderUnavailable, this.code)
        is AiError.InvalidRequest -> AiUiState.Error(AiErrorType.Unknown, this.code)
        is AiError.Unknown -> AiUiState.Error(AiErrorType.Unknown, this.code)
    }
