package com.example.myapplication.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.data.local.OutfitSnapshot
import com.example.myapplication.data.local.OutfitSnapshotStore

/** Widget d'écran d'accueil : la tenue du jour pour la météo. Lit la dernière tenue calculée. */
class OutfitWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = OutfitSnapshotStore.read(context)
        provideContent {
            GlanceTheme {
                WidgetContent(snapshot, context.getString(R.string.widget_no_data))
            }
        }
    }

    companion object {
        /** Redessine tous les widgets posés sur l'écran d'accueil. */
        suspend fun updateAll(context: Context) {
            val manager = GlanceAppWidgetManager(context)
            val widget = OutfitWidget()
            manager.getGlanceIds(OutfitWidget::class.java).forEach { widget.update(context, it) }
        }
    }
}

class OutfitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = OutfitWidget()
}

private val bg = ColorProvider(Color(0xFF3B6EA8))
private val white = ColorProvider(Color.White)
private val whiteSoft = ColorProvider(Color(0xCCFFFFFF))

@Composable
private fun WidgetContent(snapshot: OutfitSnapshot?, emptyText: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bg)
            .cornerRadius(20.dp)
            .padding(14.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (snapshot == null || snapshot.items.isEmpty()) {
            Text(emptyText, style = TextStyle(color = white, fontSize = 14.sp, fontWeight = FontWeight.Medium))
            return@Column
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(snapshot.emoji, style = TextStyle(fontSize = 26.sp))
            Spacer(GlanceModifier.width(8.dp))
            Column {
                Text(
                    "${snapshot.temp}° · ${snapshot.city}",
                    style = TextStyle(color = white, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
                Text(snapshot.condition, style = TextStyle(color = whiteSoft, fontSize = 12.sp))
            }
        }
        Spacer(GlanceModifier.height(8.dp))
        Text(
            snapshot.items.take(4).joinToString(" · "),
            style = TextStyle(color = white, fontSize = 13.sp, fontWeight = FontWeight.Medium),
            maxLines = 2
        )
    }
}
