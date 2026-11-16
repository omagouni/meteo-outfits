package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    modifier: Modifier = Modifier
) {
    val today = LocalDays.today()
    val alreadyWorn = outfit.picks.isNotEmpty() && outfit.picks.all { it.item.lastWornEpochDay == today }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            "👚  From your wardrobe",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(Modifier.height(8.dp))

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
