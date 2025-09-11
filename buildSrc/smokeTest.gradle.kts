import org.gradle.api.tasks.testing.logging.TestLogEvent

tasks.register("smokeTest") {
    group = "verification"
    description = "Führt vereinfachte Prüfungen für RC Smoke Tests aus."
    dependsOn(
        "assembleDebug"
    )
    doLast {
        println(
            """
            Smoke Test Task abgeschlossen. Manuelle Schritte:
            - App starten
            - Checkliste in docs/SMOKE_TEST_CHECKLIST.md abarbeiten
            """.trimIndent()
        )
    }
}