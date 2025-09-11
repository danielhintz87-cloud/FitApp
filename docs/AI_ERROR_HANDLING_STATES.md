# AI Error & Status Handling

## Ui State Modell
```
sealed interface AiUiState {
  object Idle
  object Loading
  data class Success<T>(val data: T)
  data class Error(val type: AiErrorType, val message: String?)
  data class Unavailable(val reason: AiUnavailableReason, val message: String? = null)
}
```

## Fehlerarten
| Typ | Anzeige |
|-----|---------|
| KeyInvalid | Inline Panel + Snackbar |
| KeyMissing | Konfig-Aufforderung |
| Network | Snackbar + Retry |
| RateLimit | Hinweis mit Wartezeit |
| ProviderUnavailable | Degradierter Modus Hinweis |
| Unknown | Generischer Fehlertext |

## Prinzipien
1. Nie silent fail
2. Immer visuelles Feedback
3. Retry anbieten wo sinnvoll
4. Degradation sichtbar machen

## Mini Test Matrix
| Szenario | Erwartung |
|----------|-----------|
| Kein Key | Konfig Hinweis |
| Falscher Key | Fehlerpaneel |
| Netzwerk weg | Retry Option |
| Timeout | Error + Retry |
| Rate Limit | Warten Hinweis |
| Erfolg | Content sichtbar |
