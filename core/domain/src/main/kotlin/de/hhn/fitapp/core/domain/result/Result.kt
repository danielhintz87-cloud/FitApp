package de.hhn.fitapp.core.domain.result

/**
 * Generische Result-Klasse für eine einheitliche Fehlerbehandlung in der Anwendung.
 * Ermöglicht ein Railway-Oriented Programming mit expliziten Erfolgs- und Fehlerpfaden.
 */
sealed class Result<out T> {
    /**
     * Repräsentiert ein erfolgreiches Ergebnis mit Daten.
     */
    data class Success<T>(val data: T) : Result<T>()
    
    /**
     * Repräsentiert ein Fehlergebnis mit Fehlermeldung und optionalem Stacktrace.
     */
    data class Error(
        val message: String, 
        val exception: Throwable? = null
    ) : Result<Nothing>()
    
    /**
     * Repräsentiert einen laufenden Prozess ohne Ergebnis.
     */
    object Loading : Result<Nothing>()
    
    /**
     * Führt eine Funktion aus, wenn das Ergebnis erfolgreich ist.
     */
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }
    
    /**
     * Führt eine Funktion aus, wenn das Ergebnis ein Fehler ist.
     */
    inline fun onError(action: (message: String, exception: Throwable?) -> Unit): Result<T> {
        if (this is Error) action(message, exception)
        return this
    }
    
    /**
     * Führt eine Funktion aus, wenn das Ergebnis im Loading-Zustand ist.
     */
    inline fun onLoading(action: () -> Unit): Result<T> {
        if (this is Loading) action()
        return this
    }
    
    /**
     * Transformiert ein erfolgreiches Ergebnis mit einer Mapping-Funktion.
     */
    inline fun <R> map(transform: (T) -> R): Result<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(message, exception)
            is Loading -> Loading
        }
    }
    
    /**
     * Extrahiert die Daten aus einem erfolgreichen Ergebnis oder gibt null zurück.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    /**
     * Extrahiert die Daten aus einem erfolgreichen Ergebnis oder gibt den Fallback-Wert zurück.
     */
    fun getOrDefault(defaultValue: @UnsafeVariance T): T = when (this) {
        is Success -> data
        else -> defaultValue
    }
    
    /**
     * Wirft die enthaltene Exception, wenn das Ergebnis ein Fehler ist, oder gibt die Daten zurück.
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception ?: RuntimeException(message)
        is Loading -> throw IllegalStateException("Result is still loading")
    }
    
    companion object {
        /**
         * Erstellt ein erfolgreiches Ergebnis mit den angegebenen Daten.
         */
        fun <T> success(data: T): Result<T> = Success(data)
        
        /**
         * Erstellt ein Fehlergebnis mit der angegebenen Nachricht und optionaler Exception.
         */
        fun <T> error(message: String, exception: Throwable? = null): Result<T> = Error(message, exception)
        
        /**
         * Erstellt ein Loading-Ergebnis.
         */
        fun <T> loading(): Result<T> = Loading
        
        /**
         * Führt eine Funktion aus und fängt Exceptions ab, um sie in ein Result zu wandeln.
         */
        inline fun <T> runCatching(block: () -> T): Result<T> {
            return try {
                Success(block())
            } catch (e: Exception) {
                Error(e.message ?: "An unknown error occurred", e)
            }
        }
    }
}