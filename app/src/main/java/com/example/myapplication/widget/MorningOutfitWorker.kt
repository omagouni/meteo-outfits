package com.example.myapplication.widget

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.data.local.OutfitSnapshot
import com.example.myapplication.data.local.OutfitSnapshotStore
import com.example.myapplication.data.local.WardrobeRepository
import com.example.myapplication.data.model.DayWeather
import com.example.myapplication.data.remote.RetrofitClient
import com.example.myapplication.domain.ForecastAggregator
import com.example.myapplication.domain.LocalDays
import com.example.myapplication.domain.OutfitRecommender
import com.example.myapplication.domain.RuleBasedOutfitPlanner
import com.example.myapplication.ui.components.getWeatherEmoji
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

/**
 * Chaque matin : récupère la météo de la dernière ville, calcule la tenue,
 * met à jour le widget et envoie la notification « ta tenue pour aujourd'hui ».
 */
class MorningOutfitWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val city = prefs.getString(KEY_CITY, null) ?: return Result.success()
        if (RetrofitClient.apiKey.isBlank()) return Result.success()

        val day = try {
            ForecastAggregator.fromCurrent(RetrofitClient.weatherApi.getWeather(city))
        } catch (e: Exception) {
            return if (runAttemptCount < 3) Result.retry() else Result.success()
        }

        val snapshot = buildSnapshot(context, city, day)
        OutfitSnapshotStore.write(context, snapshot)
        OutfitWidget.updateAll(context)
        if (prefs.getBoolean(KEY_NOTIF, false)) notify(context, snapshot)
        return Result.success()
    }

    companion object {
        const val PREFS = "weather_prefs"
        const val KEY_CITY = "last_city"
        const val KEY_NOTIF = "morning_notification"
        private const val WORK_NAME = "morning_outfit"
        private const val CHANNEL_ID = "morning_outfit"
        private const val HOUR = 7
        private const val MINUTE = 30

        /** Construit la tenue du jour (garde-robe si possible, sinon suggestion générique). */
        fun buildSnapshot(context: Context, city: String, day: DayWeather): OutfitSnapshot {
            val wardrobe = WardrobeRepository.get(context).readNow()
            val names = if (wardrobe.isNotEmpty()) {
                RuleBasedOutfitPlanner.planNow(day, wardrobe, LocalDays.today()).picks.map { it.item.name }
            } else emptyList()
            val generic = OutfitRecommender.getOutfit(day)
            val items = names.ifEmpty { generic.mainOutfit.map { it.name.resolve(context) } }
            return OutfitSnapshot(
                city = city,
                temp = day.temp.roundToInt(),
                feelsLike = day.feelsLike.roundToInt(),
                condition = day.description.replaceFirstChar { it.uppercase() },
                emoji = getWeatherEmoji(day.condition, day.isNight),
                items = items,
                tip = generic.tip.resolve(context),
                updatedAt = System.currentTimeMillis()
            )
        }

        fun setEnabled(context: Context, enabled: Boolean) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putBoolean(KEY_NOTIF, enabled) }
            if (enabled) schedule(context) else WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        fun isEnabled(context: Context): Boolean =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_NOTIF, false)

        /** Planifie le job quotidien, première exécution au prochain 7 h 30 local. */
        fun schedule(context: Context) {
            val now = Calendar.getInstance()
            val next = (now.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, HOUR); set(Calendar.MINUTE, MINUTE); set(Calendar.SECOND, 0)
                if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
            }
            val request = PeriodicWorkRequestBuilder<MorningOutfitWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(next.timeInMillis - now.timeInMillis, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
        }

        private fun notify(context: Context, snapshot: OutfitSnapshot) {
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) return
            val manager = context.getSystemService(NotificationManager::class.java)
            if (Build.VERSION.SDK_INT >= 26) {
                manager.createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, context.getString(R.string.notif_channel_name), NotificationManager.IMPORTANCE_DEFAULT)
                        .apply { description = context.getString(R.string.notif_channel_desc) }
                )
            }
            val open = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.notif_title, snapshot.city, snapshot.temp, snapshot.condition))
                .setContentText(context.getString(R.string.notif_text, snapshot.items.take(4).joinToString(", ")))
                .setStyle(NotificationCompat.BigTextStyle().bigText(
                    context.getString(R.string.notif_text, snapshot.items.joinToString(", ")) + "\n" + snapshot.tip
                ))
                .setContentIntent(open)
                .setAutoCancel(true)
                .build()
            manager.notify(1, notification)
        }
    }
}
