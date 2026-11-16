package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.AiStatus
import com.example.myapplication.data.model.ClothingItem
import com.example.myapplication.domain.LocalDays
import com.example.myapplication.domain.WardrobeOutfit
import com.example.myapplication.ui.wardrobe.ClothingThumbnail

private val glass = Color.White.copy(alpha = 0.18f)

/** "From your wardrobe" : la tenue choisie parmi les vêtements de l'utilisateur. */
@Composable
fun WardrobeOutfitCard(
    outfit: WardrobeOutfit,
    canWearToday: Boolean,
    onWear: () -> Unit,
    modifier: Modifier = Modifier,
    aiAvailable: Boolean = false,
    aiEnabled: Boolean = false,
    aiStatus: AiStatus = AiStatus.Off,
    onToggleAi: (Boolean) -> Unit = {}
) {
    val today = LocalDays.today()
    val alreadyWorn = outfit.picks.isNotEmpty() && outfit.picks.all { it.item.lastWornEpochDay == today }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "👚  From your wardrobe",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            if (aiAvailable) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .toggleable(value = aiEnabled, role = Role.Switch, onValueChange = onToggleAi)
                        .background(Color.White.copy(alpha = if (aiEnabled) 0.35f else 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        if (aiEnabled) "✨ AI stylist on" else "✨ AI stylist",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (aiEnabled) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        when (aiStatus) {
            AiStatus.Loading -> AiNote {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("Asking the AI stylist…", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
            }
            is AiStatus.Failed -> AiNote {
                Text(aiStatus.message, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
            }
            else -> Unit
        }
        outfit.tip?.let { tip ->
            AiNote { Text("✨ $tip", color = Color.White, fontSize = 13.sp, lineHeight = 18.sp) }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            outfit.picks.forEach { pick ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(glass)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClothingThumbnail(pick.item, size = 48)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            pick.item.name,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            pick.reason,
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            maxLines = 2
                        )
                    }
                }
            }

            outfit.missing.forEach { slot ->
                val what = buildString {
                    append(ClothingItem.warmthLabel(slot.targetWarmth).lowercase())
                    if (slot.needsWaterproof) append(", waterproof")
                    append(" ")
                    append(slot.category.label.lowercase())
                }
                Text(
                    "${slot.category.emoji}  Missing: a $what",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        if (canWearToday && outfit.picks.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onWear,
                enabled = !alreadyWorn,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.3f),
                    contentColor = Color.White,
                    disabledContainerColor = Color.White.copy(alpha = 0.12f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (alreadyWorn) "✓ Wearing this today" else "I'm wearing this today")
            }
        }
    }
}

/** Invitation affichée quand la garde-robe est vide. */
@Composable
fun WardrobePromptCard(onOpenWardrobe: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(glass)
            .padding(16.dp)
    ) {
        Text("👚  Use your own clothes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "Add your clothes to the wardrobe and get an outfit picked from them for each day.",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onOpenWardrobe,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.3f), contentColor = Color.White)
        ) { Text("Open wardrobe") }
    }
}

@Composable
private fun AiNote(content: @Composable RowScope.() -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        content = content
    )
}
