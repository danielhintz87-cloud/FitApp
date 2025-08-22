package com.example.fitapp.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong

/**
 * Leichtgewichtiges In-Memory-Repository. Später einfach durch Room ersetzen.
 */
object AppRepository {
    // Settings
    private val _calorieSettings = MutableStateFlow(CalorieSettings())
    val calorieSettings: StateFlow<CalorieSettings> = _calorieSettings.asStateFlow()

    // Plan
    private val _plan = MutableStateFlow<Plan?>(null)
    val plan: StateFlow<Plan?> = _plan.asStateFlow()

    // Logs
    private val _exerciseLogs = MutableStateFlow<List<ExerciseLog>>(emptyList())
    val exerciseLogs: StateFlow<List<ExerciseLog>> = _exerciseLogs.asStateFlow()

    private val _foodLogs = MutableStateFlow<List<FoodLog>>(emptyList())
    val foodLogs: StateFlow<List<FoodLog>> = _foodLogs.asStateFlow()

    // Shopping
    private val _shopping = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val shopping: StateFlow<List<ShoppingItem>> = _shopping.asStateFlow()
    private val idGen = AtomicLong(1)

    fun setCalorieSettings(new: CalorieSettings) { _calorieSettings.value = new }

    fun setPlan(plan: Plan) { _plan.value = plan }

    fun logExercise(title: String, durationMin: Int, caloriesOut: Int = 0) {
        val log = ExerciseLog(LocalDate.now(), title, durationMin, caloriesOut)
        _exerciseLogs.value = _exerciseLogs.value + log
    }

    fun logFood(title: String, caloriesIn: Int) {
        val log = FoodLog(LocalDate.now(), title, caloriesIn)
        _foodLogs.value = _foodLogs.value + log
    }

    fun addShoppingItems(items: List<Pair<String, String>>) {
        // Aggregation: gleiche Namen werden zusammengezogen (einfach)
        val current = _shopping.value.associateBy { it.name }.toMutableMap()
        for ((name, qty) in items) {
            val existing = current[name]
            if (existing == null) {
                current[name] = ShoppingItem(idGen.getAndIncrement(), name, qty, false)
            } else {
                val mergedQty = if (existing.quantity.isBlank()) qty
                else if (qty.isBlank()) existing.quantity
                else existing.quantity + " + " + qty
                current[name] = existing.copy(quantity = mergedQty)
            }
        }
        _shopping.value = current.values.sortedBy { it.name }
    }

    fun toggleShoppingChecked(id: Long) {
        _shopping.value = _shopping.value.map { if (it.id == id) it.copy(checked = !it.checked) else it }
    }

    fun removeShoppingItem(id: Long) {
        _shopping.value = _shopping.value.filterNot { it.id == id }
    }
}
