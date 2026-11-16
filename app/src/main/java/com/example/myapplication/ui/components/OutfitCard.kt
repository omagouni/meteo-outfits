package com.example.myapplication.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.OutfitItem
import com.example.myapplication.data.model.OutfitRecommendation
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OutfitCard(
    outfit: OutfitRecommendation,
    modifier: Modifier = Modifier
) {
    var showItems by remember { mutableStateOf(false) }

    LaunchedEffect(outfit) {
        showItems = false
        delay(300)
        showItems = true
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // Main outfit section
        SectionTitle(text = "👗  What to Wear", delay = 200, show = showItems)

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            outfit.mainOutfit.forEachIndexed { index, item ->
                AnimatedOutfitChip(item = item, index = index, show = showItems)
            }
        }

        if (outfit.accessories.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))

            SectionTitle(text = "🎒  Accessories & Extras", delay = 500, show = showItems)

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                outfit.accessories.forEachIndexed { index, item ->
                    AnimatedOutfitChip(
                        item = item,
                        index = index + outfit.mainOutfit.size,
                        show = showItems
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tip card
        AnimatedVisibility(
            visible = showItems,
            enter = fadeIn(tween(600, delayMillis = 800)) +
                    slideInHorizontally(tween(600, delayMillis = 800)) { it / 4 }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(16.dp)
            ) {
                Text(
                    text = outfit.tip,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.95f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, delay: Int, show: Boolean) {
    AnimatedVisibility(
        visible = show,
        enter = fadeIn(tween(500, delayMillis = delay)) +
                slideInHorizontally(tween(500, delayMillis = delay)) { -it / 4 }
    ) {
        Text(
            text = text,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = 0.3.sp
        )
    }
}

@Composable
private fun AnimatedOutfitChip(
    item: OutfitItem,
    index: Int,
    show: Boolean
) {
    val delayMs = 300 + (index * 80)

    AnimatedVisibility(
        visible = show,
        enter = scaleIn(
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialScale = 0.5f
        ) + fadeIn(tween(400, delayMillis = delayMs))
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.emoji,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = item.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = item.description,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
