package com.example.myapplication.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.data.local.OutfitSnapshotStore
import com.example.myapplication.data.local.WardrobeRepository
import com.example.myapplication.data.model.AiStatus
import com.example.myapplication.data.model.WeatherUiState
import com.example.myapplication.data.remote.RetrofitClient
import com.example.myapplication.data.remote.WeatherRepository
import com.example.myapplication.domain.ClaudeOutfitPlanner
import com.example.myapplication.domain.LocalDays
import com.example.myapplication.domain.OutfitRecommender
import com.example.myapplication.domain.RuleBasedOutfitPlanner
import com.example.myapplication.domain.WardrobeOutfit
import com.example.myapplication.ui.UiText
import com.example.myapplication.widget.MorningOutfitWorker
import com.example.myapplication.widget.OutfitWidget
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(MorningOutfitWorker.PREFS, Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _city = MutableStateFlow(prefs.getString(MorningOutfitWorker.KEY_CITY, DEFAULT_CITY) ?: DEFAULT_CITY)
    val city: StateFlow<String> = _city.asStateFlow()

    private val wardrobe = WardrobeRepository.get(application)
    private val claude = ClaudeOutfitPlanner()

    /** Vrai si une clé Claude est configurée (sinon l'option IA est masquée). */
    val aiAvailable: Boolean = claude.isConfigured

    private val _aiEnabled = MutableStateFlow(aiAvailable && prefs.getBoolean(KEY_AI, false))
    val aiEnabled: StateFlow<Boolean> = _aiEnabled.asStateFlow()

    private val _morningNotification = MutableStateFlow(MorningOutfitWorker.isEnabled(application))
    val morningNotification: StateFlow<Boolean> = _morningNotification.asStateFlow()

    /** Réponses IA déjà reçues, par (jour, météo, garde-robe) : évite de repayer un appel identique. */
    private val aiCache = mutableMapOf<String, WardrobeOutfit>()
    private var aiJob: Job? = null
    private var aiJobKey: String? = null
    private var fetchJob: Job? = null

    init {
        fetchWeather()
        viewModelScope.launch { wardrobe.items.collect { refreshOutfits() } }
    }

    fun setAiEnabled(enabled: Boolean) {
        if (!aiAvailable) return
        _aiEnabled.value = enabled
        prefs.edit { putBoolean(KEY_AI, enabled) }
        refreshOutfits()
    }

    /** À appeler une fois la permission de notification accordée (ou non nécessaire). */
    fun setMorningNotification(enabled: Boolean) {
        _morningNotification.value = enabled
        MorningOutfitWorker.setEnabled(getApplication(), enabled)
    }

    private fun refreshOutfits() {
        _uiState.update { state -> if (state is WeatherUiState.Success) withOutfits(state) else state }
        requestAiIfNeeded()
    }

    private fun aiKey(state: WeatherUiState.Success): String =
        "${state.cityLabel}|${state.selectedDay.hashCode()}|${wardrobe.items.value.hashCode()}|${LocalDays.today()}"

    /** Recalcule les tenues (générique + garde-robe) pour le jour sélectionné. */
    private fun withOutfits(state: WeatherUiState.Success): WeatherUiState.Success {
        val day = state.selectedDay
        val today = LocalDays.today()
        val items = wardrobe.items.value
        val base = state.copy(
            outfit = OutfitRecommender.getOutfit(day),
            canWearToday = day.isCurrent || day.epochDay == today
        )
        val result = if (items.isEmpty()) base.copy(wardrobeOutfit = null, aiStatus = AiStatus.Off)
        else {
            val rules = RuleBasedOutfitPlanner.planNow(day, items, referenceDay = day.epochDay ?: today)
            if (!_aiEnabled.value) base.copy(wardrobeOutfit = rules, aiStatus = AiStatus.Off)
            else {
                val cached = aiCache[aiKey(base)]
                if (cached != null) base.copy(wardrobeOutfit = cached, aiStatus = AiStatus.Ready)
                else base.copy(wardrobeOutfit = rules, aiStatus = AiStatus.Loading)
            }
        }
        if (day.isCurrent) publishSnapshot(result)
        return result
    }

    /** Alimente le widget avec la tenue de "maintenant". */
    private fun publishSnapshot(state: WeatherUiState.Success) {
        val context = getApplication<Application>()
        val day = state.selectedDay
        val names = state.wardrobeOutfit?.picks?.map { it.item.name }.orEmpty()
            .ifEmpty { state.outfit.mainOutfit.map { it.name.resolve(context) } }
        val snapshot = MorningOutfitWorker.buildSnapshot(context, _city.value.trim(), day).copy(items = names)
        OutfitSnapshotStore.write(context, snapshot)
        viewModelScope.launch { runCatching { OutfitWidget.updateAll(context) } }
    }

    private fun requestAiIfNeeded() {
        val state = _uiState.value as? WeatherUiState.Success ?: return
        if (state.aiStatus != AiStatus.Loading) return
        val key = aiKey(state)
        if (key == aiJobKey && aiJob?.isActive == true) return

        aiJob?.cancel()
        aiJobKey = key
        val day = state.selectedDay
        val items = wardrobe.items.value
        val referenceDay = day.epochDay ?: LocalDays.today()
        aiJob = viewModelScope.launch {
            delay(AI_DEBOUNCE_MS)
            val result: Result<WardrobeOutfit> = try {
                Result.success(claude.plan(day, items, referenceDay))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "AI stylist failed", e)
                Result.failure(e)
            }
            result.onSuccess { aiCache[key] = it }
            _uiState.update { current ->
                if (current !is WeatherUiState.Success || aiKey(current) != key) current
                else result.fold(
                    onSuccess = { current.copy(wardrobeOutfit = it, aiStatus = AiStatus.Ready) },
                    onFailure = { current.copy(aiStatus = AiStatus.Failed) }
                )
            }
        }
    }

    /** Enregistre que l'utilisateur porte aujourd'hui la tenue proposée. */
    fun wearSuggestedOutfit() {
        val state = _uiState.value as? WeatherUiState.Success ?: return
        val ids = state.wardrobeOutfit?.picks?.map { it.item.id }.orEmpty()
        if (!state.canWearToday || ids.isEmpty()) return
        viewModelScope.launch { wardrobe.markWorn(ids, LocalDays.today()) }
    }

    fun updateCity(newCity: String) {
        _city.value = newCity
    }

    fun selectDay(index: Int) {
        _uiState.update { state ->
            if (state is WeatherUiState.Success && index in state.days.indices) withOutfits(state.copy(selectedIndex = index))
            else state
        }
        requestAiIfNeeded()
    }

    fun fetchWeather() {
        val cityName = _city.value.trim()
        if (cityName.isBlank()) {
            _uiState.value = WeatherUiState.Error(UiText.res(R.string.err_enter_city)); return
        }
        if (RetrofitClient.apiKey.isBlank()) {
            _uiState.value = WeatherUiState.Error(UiText.res(WeatherRepository.missingKeyRes())); return
        }

        val previousIndex = (_uiState.value as? WeatherUiState.Success)?.selectedIndex ?: 0
        _uiState.value = WeatherUiState.Loading

        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                val (label, days) = WeatherRepository.load(cityName)
                val index = previousIndex.coerceIn(0, days.lastIndex)
                prefs.edit { putString(MorningOutfitWorker.KEY_CITY, cityName) }
                _uiState.value = withOutfits(
                    WeatherUiState.Success(
                        cityLabel = label, days = days, selectedIndex = index,
                        outfit = OutfitRecommender.getOutfit(days[index])
                    )
                )
                requestAiIfNeeded()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Weather fetch failed", e)
                _uiState.value = WeatherUiState.Error(WeatherRepository.errorText(e))
            }
        }
    }

    private companion object {
        const val TAG = "WeatherViewModel"
        const val KEY_AI = "ai_stylist_enabled"
        const val DEFAULT_CITY = "Paris"
        const val AI_DEBOUNCE_MS = 600L
    }
}
