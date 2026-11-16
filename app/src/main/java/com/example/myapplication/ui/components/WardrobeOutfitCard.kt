package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.data.model.AiStatus
import com.example.myapplication.data.model.ClothingItem
import com.example.myapplication.domain.LocalDays
import com.example.myapplication.domain.MissingSlot
import com.example.myapplication.domain.WardrobeOutfit
import com.example.myapplication.domain.WardrobePick
import com.example.myapplication.ui.asString
import com.example.myapplication.ui.wardrobe.ClothingThumbnail

private val glass = Color.White.copy(alpha = 0.18f)

/** "Dans ta garde-robe" : la tenue choisie parmi les vêtements de l'utilisateur. */
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

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.section_from_wardrobe),
                fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White,
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
                        stringResource(if (aiEnabled) R.string.ai_stylist_on else R.string.ai_stylist),
                        color = Color.White, fontSize = 12.sp,
                        fontWeight = if (aiEnabled) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        when (aiStatus) {
            AiStatus.Loading -> AiNote {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.ai_asking), color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
            }
            AiStatus.Failed -> AiNote {
                Text(stringResource(R.string.ai_unavailable), color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
            }
            else -> Unit
        }
        outfit.tip?.let { tip ->
            AiNote { Text("✨ $tip", color = Color.White, fontSize = 13.sp, lineHeight = 18.sp) }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            outfit.picks.forEach { pick -> PickRow(pick) }
            outfit.missing.forEach { slot -> MissingRow(slot) }
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
                Text(stringResource(if (alreadyWorn) R.string.wearing_today_done else R.string.wearing_this_today))
            }
        }
    }
}

/** Une pièce choisie, avec sa raison. Réutilisée par l'écran Rendez-vous. */
@Composable
fun PickRow(pick: WardrobePick, modifier: Modifier = Modifier, light: Boolean = true) {
    val fg = if (light) Color.White else Color.Unspecified
    val sub = if (light) Color.White.copy(alpha = 0.75f) else Color.Gray
    val bg = if (light) glass else Color.Gray.copy(alpha = 0.12f)
    Row(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(bg).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ClothingThumbnail(pick.item, size = 48)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                pick.item.name.ifBlank { stringResource(pick.item.category.labelRes) },
                color = fg, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(pick.reason.asString(), color = sub, fontSize = 11.sp, maxLines = 2)
        }
    }
}

@Composable
fun MissingRow(slot: MissingSlot, modifier: Modifier = Modifier, light: Boolean = true) {
    val warmth = stringResource(ClothingItem.warmthRes(slot.targetWarmth)).lowercase() +
        if (slot.needsWaterproof) stringResource(R.string.missing_waterproof) else ""
    Text(
        stringResource(R.string.missing_slot, slot.category.emoji, stringResource(slot.category.labelRes).lowercase(), warmth),
        color = if (light) Color.White.copy(alpha = 0.8f) else Color.Gray, fontSize = 12.sp,
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(Color.Gray.copy(alpha = 0.12f)).padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

/** Invitation affichée quand la garde-robe est vide. */
@Composable
fun WardrobePromptCard(onOpenWardrobe: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(RoundedCornerShape(16.dp)).background(glass).padding(16.dp)
    ) {
        Text(stringResource(R.string.wardrobe_prompt_title), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.wardrobe_prompt_body), color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onOpenWardrobe,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.3f), contentColor = Color.White)
        ) { Text(stringResource(R.string.open_wardrobe)) }
    }
}

@Composable
private fun AiNote(content: @Composable RowScope.() -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f)).padding(horizontal = 12.dp, vertical = 8.dp),
        content = content
    )
}
