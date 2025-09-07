package com.example.fitapp.infrastructure.ai

/** Repräsentiert normalisierte AI Fehlerkategorien für Logging & UI Mapping. */
sealed class AiError(val code: String) {
    data object Auth : AiError("auth")
    data object Quota : AiError("quota")
    data object RateLimit : AiError("rate_limit")
    data object InvalidRequest : AiError("invalid_request")
    data object Server : AiError("server")
    data object Network : AiError("network")
    data object Unknown : AiError("unknown")
}

class ClassifiedAiException(
    message: String,
    val aiError: AiError,
    val httpCode: Int? = null,
    cause: Throwable? = null
) : IllegalStateException(message, cause)

/** Klassifiziert HTTP Code + Body Snippet in AiError. */
fun classifyHttpError(code: Int, body: String?): AiError = when (code) {
    400 -> AiError.InvalidRequest
    401, 403 -> AiError.Auth
    402 -> AiError.Quota
    429 -> AiError.RateLimit
    in 500..599 -> AiError.Server
    else -> AiError.Unknown
}

/** Versucht Text auf Rate Limit / Quota Hinweise zu prüfen falls kein Code vorhanden. */
fun inferFromMessage(msg: String?): AiError = when {
    msg == null -> AiError.Unknown
    msg.contains("rate", true) && msg.contains("limit", true) -> AiError.RateLimit
    msg.contains("quota", true) -> AiError.Quota
    msg.contains("auth", true) || msg.contains("unauthorized", true) || msg.contains("api-schlüssel", true) -> AiError.Auth
    msg.contains("timeout", true) || msg.contains("netzwerk", true) -> AiError.Network
    msg.contains("server", true) || msg.contains("internal", true) -> AiError.Server
    else -> AiError.Unknown
}
