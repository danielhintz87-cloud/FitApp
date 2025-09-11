# Smoke Test Checkliste (RC1)

Diese Liste vollständig durchgehen bevor Release Kandidat freigeben.

## Setup
- [ ] Frische Installation
- [ ] Test mit vorhandener v14 DB (Migration)
- [ ] Test mit leerer DB

## Migration
- [ ] Start mit v14 DB ohne Crash
- [ ] Log: Migration 14→15 OK
- [ ] Daten weiterhin sichtbar

## Ernährung & Rezepte
- [ ] Button/Quick Action Rezeptgenerierung sichtbar
- [ ] Rezept generiert & angezeigt
- [ ] Favorit speichern & Favoriten-Tab sichtbar
- [ ] Einkaufsliste / Historie erreichbar (falls implementiert)

## AI / Personal Trainer (Key Szenarien)
1. Kein Key
2. Ungültiger Key
3. Gültiger Key
- [ ] Kein Key → Konfig-Hinweis
- [ ] Ungültiger Key → Fehlerpaneel
- [ ] Gültiger Key → Workout / Analyse generiert
- [ ] Netzverlust simuliert → Snackbar / Hinweis
- [ ] (Optional) Rate Limit → Hinweis

## HIIT
- [ ] Einstieg Today Screen
- [ ] Einstieg Training Hub
- [ ] Intervalle hinzufügen/entfernen
- [ ] Execution läuft & wechselt Intervalle
- [ ] Abbruch ohne Crash

## Meal Logging / Streaks
- [ ] Mehrere Mahlzeiten → Streak verändert
- [ ] Keine Exceptions

## Wasser
- [ ] Ziel gesetzt & sichtbar in Diary/Analytics/Reminder
- [ ] Fallback bei fehlendem Ziel = 2000 ml

## ML Pose Detection
- [ ] Pose-Test ohne Crash
- [ ] Degradierter Mode Anzeige korrekt (falls simuliert)

## Performance
- [ ] Keine ANR
- [ ] Keine NetworkOnMainThreadException

## Abschluss
- [ ] Logs WARN/ERROR geprüft
- [ ] Release Notes aktualisiert

Notizen:
