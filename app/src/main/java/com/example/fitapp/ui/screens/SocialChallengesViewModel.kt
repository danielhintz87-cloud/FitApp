package com.example.fitapp.ui.screens

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitapp.data.db.AppDatabase
import com.example.fitapp.data.db.ChallengeParticipationEntity
import com.example.fitapp.data.db.SocialChallengeEntity
import com.example.fitapp.data.repo.PersonalMotivationRepository
import com.example.fitapp.services.SocialChallengeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SocialChallengesViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.get(application)
    private val repository = PersonalMotivationRepository(database)
    private val challengeManager = SocialChallengeManager(application, repository)

    private val _challenges = MutableStateFlow<List<SocialChallengeEntity>>(emptyList())
    val challenges: StateFlow<List<SocialChallengeEntity>> = _challenges.asStateFlow()

    private val _userParticipations = MutableStateFlow<List<ChallengeParticipationEntity>>(emptyList())
    val userParticipations: StateFlow<List<ChallengeParticipationEntity>> = _userParticipations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Filter state
    val selectedCategory = mutableStateOf("all")
    val categories = listOf("all", "fitness", "nutrition", "weight_loss", "endurance", "strength")

    // Simple user ID for demo (in real app this would come from user session)
    private val userId = "demo_user_${android.os.Build.MODEL}"

    init {
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch {
            // Initialize default challenges if none exist
            challengeManager.initializeDefaultChallenges()
            loadChallenges()
            loadUserParticipations()
        }
    }

    fun loadChallenges() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                challengeManager.getAllChallenges().collect { allChallenges ->
                    _challenges.value =
                        if (selectedCategory.value == "all") {
                            allChallenges
                        } else {
                            allChallenges.filter { it.category == selectedCategory.value }
                        }
                }
            } catch (e: Exception) {
                // Handle error
                _challenges.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadUserParticipations() {
        viewModelScope.launch {
            try {
                challengeManager.getUserParticipations(userId).collect { participations ->
                    _userParticipations.value = participations
                }
            } catch (e: Exception) {
                // Handle error
                _userParticipations.value = emptyList()
            }
        }
    }

    fun selectCategory(category: String) {
        selectedCategory.value = category
        loadChallenges() // Reload with new filter
    }

    fun refreshChallenges() {
        loadChallenges()
        loadUserParticipations()
    }

    fun joinChallenge(challengeId: Long) {
        viewModelScope.launch {
            try {
                val success =
                    challengeManager.joinChallenge(
                        challengeId = challengeId,
                        userId = userId,
                        userName = "Demo User", // In real app, get from user profile
                    )

                if (success) {
                    // Refresh data to show updated state
                    loadUserParticipations()
                    loadChallenges()
                }
            } catch (e: Exception) {
                // Handle error - could show toast or snackbar
            }
        }
    }

    fun leaveChallenge(challengeId: Long) {
        viewModelScope.launch {
            try {
                challengeManager.leaveChallenge(challengeId, userId)

                // Refresh data to show updated state
                loadUserParticipations()
                loadChallenges()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun logWorkoutProgress() {
        viewModelScope.launch {
            try {
                challengeManager.trackWorkoutForChallenges(userId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun logNutritionProgress(calories: Int) {
        viewModelScope.launch {
            try {
                challengeManager.trackNutritionForChallenges(userId, calories)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
