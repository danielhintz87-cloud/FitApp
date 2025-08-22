package com.example.fitapp.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong

object AppRepository {

    /* ----------------------------- Settings ----------------------------- */
    private val _calorieSettings = MutableStateFlow(CalorieSettings())
    val calorieSettings: StateFlow<CalorieSettings> = _calorieSettings.asStateFlow()
    fun setCalorieSettings(new: CalorieSettings) { _calorieSettings.value = new }

    /* ----------------------- PlanState + Legacy Plan -------------------- */
    private val _planState = MutableStateFlow<PlanState?>(null)
    val planState: StateFlow<PlanState?> = _planState.asStateFlow()

    // Legacy-Kompatibilität: einige Screens lesen noch "plan"
    private val _planLegacy = MutableStateFlow<Plan?>(null)
    val plan: StateFlow<Plan?> = _planLegacy.asStateFlow()

    fun setBasePlan(plan: Plan) {
        val old = _planState.value
        _planState.value = PlanState(base = plan, overrides = old?.overrides ?: emptyMap())
        _planLegacy.value = plan
    }
    /** Für alten Code: ruft setBasePlan auf. */
    fun setPlan(plan: Plan) = setBasePlan(plan)

    /** Nur heute (oder beliebiges Datum) überschreiben – Baseplan bleibt erhalten. */
    fun setOverrideFor(date: LocalDate, replacement: WorkoutDay, reason: OverrideReason) {
        val ps = _planState.value ?: return
        _planState.value = ps.copy(
            overrides = ps.overrides.toMutableMap().apply { put(date, DayOverride(date, replacement, reason)) }
        )
    }
    fun clearOverride(date: LocalDate) {
        val ps = _planState.value ?: return
        _planState.value = ps.copy(
            overrides = ps.overrides.toMutableMap().apply { remove(date) }
        )
    }

    /* ------------------------------ Logs -------------------------------- */
    private val _exerciseLogs = MutableStateFlow<List<ExerciseLog>>(emptyList())
    val exerciseLogs: StateFlow<List<ExerciseLog>> = _exerciseLogs.asStateFlow()

    private val _foodLogs = MutableStateFlow<List<FoodLog>>(emptyList())
    val foodLogs: StateFlow<List<FoodLog>> = _foodLogs.asStateFlow()

    fun logExercise(title: String, durationMin: Int, caloriesOut: Int = 0) {
        val log = ExerciseLog(LocalDate.now(), title, durationMin, caloriesOut)
        _exerciseLogs.value = _exerciseLogs.value + log
    }
    fun logFood(title: String, caloriesIn: Int) {
        val log = FoodLog(LocalDate.now(), title, caloriesIn)
        _foodLogs.value = _foodLogs.value + log
    }

    /* ---------------------------- Shopping ------------------------------ */
    private val _shopping = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val shopping: StateFlow<List<ShoppingItem>> = _shopping.asStateFlow()
    private val idGen = AtomicLong(1)

    fun addShoppingItems(items: List<Pair<String, String>>) {
        val current = _shopping.value.associateBy { it.name }.toMutableMap()
        for ((nameRaw, qtyRaw) in items) {
            val name = nameRaw.trim()
            val qty = qtyRaw.trim()
            if (name.isEmpty()) continue

            val existing = current[name]
            if (existing == null) {
                current[name] = ShoppingItem(idGen.getAndIncrement(), name, qty, false)
            } else {
                val merged = when {
                    existing.quantity.isBlank() -> qty
                    qty.isBlank() -> existing.quantity
                    else -> "${existing.quantity} + $qty"
                }
                current[name] = existing.copy(quantity = merged)
            }
        }
        _shopping.value = current.values.sortedBy { it.name.lowercase() }
    }
    fun addShoppingItem(name: String, quantity: String = "") = addShoppingItems(listOf(name to quantity))
    fun setShopping(newList: List<ShoppingItem>) { _shopping.value = newList }
    fun toggleShoppingChecked(id: Long) {
        _shopping.value = _shopping.value.map { if (it.id == id) it.copy(checked = !it.checked) else it }
    }
    fun removeShoppingItem(id: Long) { _shopping.value = _shopping.value.filterNot { it.id == id } }
    fun clearShopping() { _shopping.value = emptyList() }

    /* ----------------------------- Devices ------------------------------ */
    private val _devices = MutableStateFlow<List<Device>>(
        listOf(
            Device("Kurzhantel"),
            Device("Kettlebell"),
            Device("Bänder"),
            Device("Klimmzugstange"),
            Device("Rudergerät"),
            Device("Laufband"),
            Device("Matte")
        )
    )
    val devices: StateFlow<List<Device>> = _devices.asStateFlow()

    private val _selectedDeviceNames = MutableStateFlow<Set<String>>(emptySet())
    val selectedDeviceNames: StateFlow<Set<String>> = _selectedDeviceNames.asStateFlow()

    fun addDevice(name: String) {
        val trimmed = name.trim(); if (trimmed.isEmpty()) return
        if (_devices.value.any { it.name.equals(trimmed, ignoreCase = true) }) return
        _devices.value = _devices.value + Device(trimmed)
        _selectedDeviceNames.value = _selectedDeviceNames.value + trimmed
    }
    fun toggleDevice(name: String) {
        val cur = _selectedDeviceNames.value.toMutableSet()
        if (cur.contains(name)) cur.remove(name) else cur.add(name)
        _selectedDeviceNames.value = cur
    }
    fun getSelectedDevices(): List<Device> =
        _devices.value.filter { _selectedDeviceNames.value.contains(it.name) }
}
