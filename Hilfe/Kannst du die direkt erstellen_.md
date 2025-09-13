<img src="https://r2cdn.perplexity.ai/pplx-full-logo-primary-dark%402x.png" style="height:64px;margin-right:32px"/>

# Kannst du die direkt erstellen?

Hier sind fertige Copilot-Prompts, als Comments, die du (z.B. im jeweiligen Feature-Modul, Repository oder als Issue) direkt einfügen kannst. Sie sind optimal für Copilot (Kotlin/Compose Syntax, klare todos, akzeptanzkriterien):

***

```kotlin
/*
Feature: Adaptive KI-Trainingspersonalisierung nach Freeletics-Vorbild
Ziel: Trainingspläne passen sich automatisch nach jedem Workout anhand von User-Feedback an.

ToDo:
- Trainingsfeedback UI einbauen (Skala, Checkboxen, Freitext) am Ende von TrainingExecutionScreen.
- Feedback als WorkoutFeedbackEntity (weight, difficulty, motivation, muscle soreness, etc.) speichern.
- PlanScreen: Algorithmen zum dynamischen Anpassen der nächsten Trainingsparameter (Intensität, Muskelgruppen, Pause).
- Replay: Historische Feedbacks differenziert gewichten.
- Optional: KI-Modul bauen, das Vorschläge generiert (API-Call oder lokal).

Akzeptanz:
- Feedback beeinflusst spürbar den nächsten Trainingstag.
- Feedback erscheint in Analytics/Verlauf.
*/
```


***

```kotlin
/*
Feature: Streaks, Badges & Social Challenges wie Freeletics
Ziel: Aufbau eines leistungsfähigen Gamification-Systems aus Streaks, Badges und Challenges.

ToDo:
- PersonalAchievementEntity erweitern: Kategorien Streak, Challenge, Social, verschiedene Badgetypen.
- Automatische Vergabe bei Zielerreichen (z.B. erste 7 Tage Streak, 100km, 10 Workouts).
- Social Challenge System: public Challenges, Gruppenbeitritt, Gruppenfortschritt, Fortschrittsbalken.
- Push-Notifications für Streaks/Badges.
- Anzeige: Homescreen-Widget (Streak), Badgeliste, Challenge-UI.

Check:
- Neue Erfolge werden nach Regeln automatisch erkannt.
- Nutzer kann Challenges starten und Streaks einsehen.
*/
```


***

```kotlin
/*
Feature: Community-Feed und Social-Mechanismen
Ziel: Community-Feed, in dem User Workouts, Rezepte, Achievements posten und mit einem "Clap"-Button feiern können.

ToDo:
- CommunityFeedEntity anlegen (Typ: Workout | Meal | Achievement | Text, Zeitstempel, User-Id).
- FeedScreen: Chronologische Feedliste, Clap/Unclap, Kommentare, Gruppenfilter, "Beitreten".
- Gruppen/ Follow: Freunde hinzufügen, Gruppen joinen.
- Privacy-Settings: Öffentliche/Private Feeds.
- Social Sharing zu WhatsApp, Instagram etc. für ausgewählte Achievements/Workouts.

Akzeptanz:
- User sieht Feed, interagiert, kann eigene Workouts posten.
- Gruppenfeatures ("Gruppen-Challenge gestartet") funktionsfähig.
*/
```


***

```kotlin
/*
Feature: Bodyweight Progression & HIIT-Builder-Modus
Ziel: Progressionslogik nach Freeletics: Jede Übung (z.B. Pushup, Pullup) existiert in mehreren Schwierigkeitsgraden. Automatisches Hochstufen nach Erfolgen; HIIT-Session-Builder für individuelles Training.

ToDo:
- ExerciseEntity um Level (enum: ENDURANCE, STANDARD, STRENGTH) erweitern.
- Trainingsengine: Je nach User-Feedback Level anpassen.
- HIIT-Builder-Screen: User stellt HIIT-Workout aus x Minuten Übungen/Pausen selbst zusammen.
- Fortschritt pro Übung und Gesamtfortschritt sichtbar machen (ProgressBar, LevelUp).
*/
```


***

```kotlin
/*
Feature: Digitaler Coach (Coach+ Stil)
Ziel: KI-Coach, der proaktiv motiviert, Tipps gibt und individuelle Hinweise liefert.

ToDo:
- KI-API für Tagesziele, Trainingstipps, Motivation ("Du hast dein Wochenziel fast erreicht!", "Probiere Pause xy, damit du schneller regenerierst.")
- Tageskarte/Push-Notification mit individuellem Kontext, Trigger z.B. nach Training, bei längerer Pause.
- Coach-Komponente im Homescreen/Sidebar als Hook für User-Kommunikation.
- Feedback-Toasts ("Hilfreich?", "Mehr davon?").

Check:
- Motivierender Coach taucht zum passenden Zeitpunkt und personalisiert auf.
- Feedback für die Tipps kann erfasst werden.
*/
```


***

```kotlin
/*
Feature: Ernährung & Training koppeln
Ziel: Automatisierte Makro- und Rezeptvorschläge nach Sport, Anpassung an Trainingsplan.

ToDo:
- Trainingsdaten (Kalorienverbrauch, Fokus Muskelgruppen) nach jedem Training speichern.
- Algorithmus/AI-Service, der Rezepte und Makros dynamisch zuordnet (z.B. nach Legday mehr Protein, nach Ausdauer mehr Kohlenhydrate).
- Rezepte und Einkaufsliste automatisch personalisieren.
- Analytics: Vorschlagsannahme/-nutzung tracken, Empfehlungen verfeinern.
*/
```


***

**Vorgehen:**

- In entsprechende Klassen/Dateien (z.B. unterhalb eines bestehenden Features, im Repository oder als Documentation) einfügen.
- Alternativ als Markdown-Task in ein GitHub-Issue übertragen.
- Ggf. durch kleine Sub-Todos für Copilot weiter ausdifferenzieren.

Du hast damit die perfekte Copilot-Grundlage, um die Features direkt zielgerichtet zu implementieren.

