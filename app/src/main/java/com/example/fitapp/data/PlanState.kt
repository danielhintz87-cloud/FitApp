package com.example.fitapp.data

import java.time.LocalDate

data class DayOverride(
    val date: LocalDate,
    val replacement: WorkoutDay,
    val reason: OverrideReason
)
enum class OverrideReason { Alternative, Swap, AdHoc }

/** Baseplan + tagesweise Abweichungen (Overrides). */
data class PlanState(
    val base: Plan,
    val overrides: Map<LocalDate, DayOverride> = emptyMap()
)

/** Einheit für Datum – berücksichtigt Overrides, sonst rotierender Baseplan. */
fun PlanState.unitFor(date: LocalDate): WorkoutDay {
    overrides[date]?.let { return it.replacement }
    if (base.week.isEmpty()) return WorkoutDay("Tag 1", base.timeBudgetMin, emptyList())
    val idx = ((date.dayOfWeek.value - 1) % base.week.size).coerceIn(0, base.week.lastIndex)
    return base.week[idx]
}
