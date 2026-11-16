package com.example.myapplication.viewmodel

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.data.local.MeetupRepository
import com.example.myapplication.data.local.WardrobeRepository
import com.example.myapplication.data.model.ClothingCategory
import com.example.myapplication.data.model.ClothingItem
import com.example.myapplication.data.model.DayWeather
import com.example.myapplication.data.model.Meetup
import com.example.myapplication.data.model.MeetupDirection
import com.example.myapplication.data.model.MeetupPayload
import com.example.myapplication.data.model.OutfitPiece
import com.example.myapplication.data.remote.RetrofitClient
import com.example.myapplication.data.remote.WeatherRepository
import com.example.myapplication.domain.ColorFamily
import com.example.myapplication.domain.LocalDays
import com.example.myapplication.domain.MatchyOutfit
import com.example.myapplication.domain.MatchyPlanner
import com.example.myapplication.domain.RuleBasedOutfitPlanner
import com.example.myapplication.domain.WardrobeOutfit
import com.example.myapplication.ui.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Contenu à partager via la feuille de partage Android. */
data class ShareContent(val subject: UiText, val text: String)

/** Ce que l'écran Rendez-vous affiche. */
sealed class MeetupScreenState {
    data object List : MeetupScreenState()

    data class Create(
        val myName: String = "",
        val friendName: String = "",
        val city: String = "",
        val days: kotlin.collections.List<DayWeather> = emptyList(),
        val cityLabel: String = "",
        val selectedIndex: Int = 0,
        val loading: Boolean = false,
        val error: UiText? = null
    ) : MeetupScreenState() {
        val selectedDay: DayWeather? get() = days.getOrNull(selectedIndex)
    }

    data class Detail(
        val meetup: Meetup,
        val day: DayWeather? = null,
        val matchy: MatchyOutfit? = null,
        /** Tenue "simple" (sans ami à coordonner) quand on crée l'invitation. */
        val outfit: WardrobeOutfit? = null,
        val loading: Boolean = false,
        val error: UiText? = null,
        /** Couleurs choisies à la main quand la garde-robe est vide. */
        val palette: Map<ClothingCategory, ColorFamily> = emptyMap(),
        val usesPalette: Boolean = false
    ) : MeetupScreenState() {
        val myPicks get() = (matchy?.outfit ?: outfit)?.picks.orEmpty()
    }
}

class MeetupViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MeetupRepository.get(application)
    private val wardrobe = WardrobeRepository.get(application)
    private val prefs = application.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)

    val meetups: StateFlow<List<Meetup>> = repository.items

    private val _screen = MutableStateFlow<MeetupScreenState>(MeetupScreenState.List)
    val screen: StateFlow<MeetupScreenState> = _screen.asStateFlow()

    private val _share = MutableStateFlow<ShareContent?>(null)
    val share: StateFlow<ShareContent?> = _share.asStateFlow()

    private val _pasteError = MutableStateFlow(false)
    val pasteError: StateFlow<Boolean> = _pasteError.asStateFlow()

    private val myName: String get() = prefs.getString(KEY_MY_NAME, "") ?: ""

    // ---------- Navigation ----------

    fun backToList() { _screen.value = MeetupScreenState.List }

    fun startCreate() {
        _screen.value = MeetupScreenState.Create(myName = myName, city = prefs.getString("last_city", "") ?: "")
    }

    fun open(meetup: Meetup) {
        _screen.value = MeetupScreenState.Detail(meetup, loading = true)
        viewModelScope.launch { computeDetail(meetup, palette = emptyMap()) }
    }

    fun delete(meetup: Meetup) {
        viewModelScope.launch { repository.delete(meetup.id) }
        backToList()
    }

    fun shareConsumed() { _share.value = null }

    // ---------- Création ----------

    fun updateCreate(transform: MeetupScreenState.Create.() -> MeetupScreenState.Create) {
        _screen.update { if (it is MeetupScreenState.Create) it.transform() else it }
    }

    fun loadForecast() {
        val form = _screen.value as? MeetupScreenState.Create ?: return
        val city = form.city.trim()
        if (city.isBlank()) return
        if (RetrofitClient.apiKey.isBlank()) {
            updateCreate { copy(error = UiText.res(WeatherRepository.missingKeyRes())) }; return
        }
        updateCreate { copy(loading = true, error = null, days = emptyList()) }
        viewModelScope.launch {
            try {
                val result = WeatherRepository.load(city)
                // Pour un rendez-vous on raisonne par journée : on retire "maintenant" s'il y a des prévisions.
                val days = if (result.days.size > 1) result.days.drop(1) else result.days
                updateCreate { copy(loading = false, days = days, cityLabel = result.cityLabel, selectedIndex = 0) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                updateCreate { copy(loading = false, error = WeatherRepository.errorText(e)) }
            }
        }
    }

    /** Crée le rendez-vous avec ma tenue pour ce jour-là et ouvre son détail. */
    fun createMeetup() {
        val form = _screen.value as? MeetupScreenState.Create ?: return
        val day = form.selectedDay ?: return
        prefs.edit { putString(KEY_MY_NAME, form.myName.trim()) }
        val meetup = Meetup(
            direction = MeetupDirection.SENT,
            friendName = form.friendName.trim(),
            city = form.cityLabel.ifBlank { form.city.trim() },
            day = day.epochDay ?: LocalDays.today(),
            dayLabel = day.label,
            feelsLike = day.feelsLike.roundToInt(),
            condition = day.description.ifBlank { day.condition }
        )
        _screen.value = MeetupScreenState.Detail(meetup, day = day, loading = true)
        viewModelScope.launch { computeDetail(meetup, palette = emptyMap(), knownDay = day) }
    }

    // ---------- Invitation reçue ----------

    /** Ouvre une invitation (lien du téléphone ou texte collé). Retourne false si invalide. */
    fun openPayload(text: String): Boolean {
        val payload = MeetupPayload.decode(text) ?: run { _pasteError.value = true; return false }
        _pasteError.value = false
        val existing = repository.items.value.firstOrNull { it.id == payload.id }
        val meetup = existing ?: Meetup(
            id = payload.id,
            direction = MeetupDirection.RECEIVED,
            friendName = payload.from.ifBlank { "?" },
            city = payload.city,
            day = payload.day,
            dayLabel = payload.dayLabel,
            friendOutfit = payload.outfit,
            feelsLike = payload.feelsLike,
            condition = payload.condition
        )
        _screen.value = MeetupScreenState.Detail(meetup, loading = true)
        viewModelScope.launch { computeDetail(meetup, palette = emptyMap()) }
        return true
    }

    fun clearPasteError() { _pasteError.value = false }

    // ---------- Palette (garde-robe vide) ----------

    fun setPaletteColor(category: ClothingCategory, color: ColorFamily?) {
        val detail = _screen.value as? MeetupScreenState.Detail ?: return
        val palette = detail.palette.toMutableMap().apply { if (color == null) remove(category) else put(category, color) }
        _screen.value = detail.copy(palette = palette)
        viewModelScope.launch { computeDetail(detail.meetup, palette, knownDay = detail.day) }
    }

    // ---------- Partage ----------

    /** Prépare le texte d'invitation (ou de réponse) avec ma tenue. */
    fun shareOutfit() {
        val detail = _screen.value as? MeetupScreenState.Detail ?: return
        val meetup = detail.meetup
        val day = detail.day
        val context = getApplication<Application>()
        val payload = MeetupPayload(
            id = meetup.id,
            from = myName.ifBlank { context.getString(R.string.meetup_you_wear) },
            city = meetup.city,
            day = meetup.day,
            dayLabel = meetup.dayLabel,
            outfit = meetup.myOutfit,
            feelsLike = day?.feelsLike?.roundToInt() ?: meetup.feelsLike,
            condition = day?.description?.ifBlank { day.condition } ?: meetup.condition
        )
        val text = context.getString(
            R.string.meetup_share_text,
            payload.from, dayLabelText(context, meetup.dayLabel), meetup.city,
            payload.condition, payload.feelsLike ?: 0, payload.toLink(), MeetupPayload.STORE_URL
        )
        _share.value = ShareContent(UiText.res(R.string.meetup_share_subject), text)
    }

    fun wearMyOutfit() {
        val detail = _screen.value as? MeetupScreenState.Detail ?: return
        val ids = detail.myPicks.map { it.item.id }.filterNot { it.startsWith("virtual-") }
        if (ids.isEmpty()) return
        viewModelScope.launch { wardrobe.markWorn(ids, LocalDays.today()) }
    }

    // ---------- Calcul ----------

    /** Météo du lieu pour le jour du rendez-vous, puis ma tenue (coordonnée si l'ami a envoyé la sienne). */
    private suspend fun computeDetail(meetup: Meetup, palette: Map<ClothingCategory, ColorFamily>, knownDay: DayWeather? = null) {
        val day = knownDay ?: try {
            if (RetrofitClient.apiKey.isBlank()) null
            else {
                val result = WeatherRepository.load(meetup.city.substringBefore(','))
                result.days.firstOrNull { it.epochDay == meetup.day }
                    ?: result.days.firstOrNull { it.isCurrent && meetup.day == LocalDays.today() }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _screen.update { s -> if (s is MeetupScreenState.Detail && s.meetup.id == meetup.id) s.copy(loading = false, error = WeatherRepository.errorText(e)) else s }
            return
        }

        if (day == null) {
            _screen.update { s -> if (s is MeetupScreenState.Detail && s.meetup.id == meetup.id) s.copy(loading = false, error = UiText.res(R.string.meetup_weather_unknown)) else s }
            return
        }

        val realWardrobe = wardrobe.items.value
        val usesPalette = realWardrobe.isEmpty()
        val items: List<ClothingItem> = if (usesPalette) MatchyPlanner.virtualWardrobe(palette, day.feelsLike) else realWardrobe
        val referenceDay = meetup.day

        var matchy: MatchyOutfit? = null
        var outfit: WardrobeOutfit? = null
        if (meetup.friendOutfit.isNotEmpty()) {
            matchy = MatchyPlanner.plan(day, items, meetup.friendOutfit.map { it.color }, meetup.friendName, referenceDay)
        } else {
            outfit = RuleBasedOutfitPlanner.planNow(day, items, referenceDay)
        }
        val picks = (matchy?.outfit ?: outfit)?.picks.orEmpty()
        val context = getApplication<Application>()
        val myOutfit = picks.map { pick ->
            val item = pick.item
            val color = ColorFamily.fromText(item.color)
            OutfitPiece(
                name = item.name.ifBlank { context.getString(R.string.meetup_generic_item, context.getString(item.category.labelRes), context.getString(color.labelRes)) },
                category = item.category,
                color = color,
                emoji = item.category.emoji
            )
        }
        val saved = meetup.copy(
            myOutfit = myOutfit,
            feelsLike = day.feelsLike.roundToInt(),
            condition = day.description.ifBlank { day.condition }
        )
        repository.upsert(saved)
        _screen.update { s ->
            if (s is MeetupScreenState.Detail && s.meetup.id == meetup.id)
                s.copy(meetup = saved, day = day, matchy = matchy, outfit = outfit, loading = false, error = null, palette = palette, usesPalette = usesPalette)
            else s
        }
    }

    companion object {
        private const val KEY_MY_NAME = "my_name"

        /** "Now"/"Today"/"Tomorrow" viennent du domaine en anglais : on les traduit ici. */
        fun dayLabelText(context: Context, label: String): String = when (label) {
            "Now" -> context.getString(R.string.day_now)
            "Today" -> context.getString(R.string.day_today)
            "Tomorrow" -> context.getString(R.string.day_tomorrow)
            else -> label
        }
    }
}
