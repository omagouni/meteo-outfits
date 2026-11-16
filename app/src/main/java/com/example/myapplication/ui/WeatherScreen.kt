package com.example.myapplication.ui

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.data.model.WeatherUiState
import com.example.myapplication.ui.components.DaySelector
import com.example.myapplication.ui.components.ErrorView
import com.example.myapplication.ui.components.LoadingView
import com.example.myapplication.ui.components.OutfitCard
import com.example.myapplication.ui.components.WardrobeOutfitCard
import com.example.myapplication.ui.components.WardrobePromptCard
import com.example.myapplication.ui.components.WeatherBackground
import com.example.myapplication.ui.components.WeatherHeader
import com.example.myapplication.viewmodel.WeatherViewModel

@Composable
fun WeatherScreen(viewModel: WeatherViewModel, onOpenWardrobe: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val city by viewModel.city.collectAsState()
    val aiEnabled by viewModel.aiEnabled.collectAsState()
    val morningNotification by viewModel.morningNotification.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // Notification du matin : sur Android 13+, il faut d'abord la permission.
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.setMorningNotification(granted)
        Toast.makeText(context, if (granted) R.string.notif_enabled_toast else R.string.notif_disabled_toast, Toast.LENGTH_SHORT).show()
    }
    fun toggleNotification() {
        if (morningNotification) {
            viewModel.setMorningNotification(false)
            Toast.makeText(context, R.string.notif_disabled_toast, Toast.LENGTH_SHORT).show()
        } else if (Build.VERSION.SDK_INT >= 33) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.setMorningNotification(true)
            Toast.makeText(context, R.string.notif_enabled_toast, Toast.LENGTH_SHORT).show()
        }
    }

    // Le fond suit la journée sélectionnée
    val selectedDay = (uiState as? WeatherUiState.Success)?.selectedDay
    WeatherBackground(condition = selectedDay?.condition ?: "Clear", isNight = selectedDay?.isNight ?: false) {
        Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 24.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.app_name), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 0.5.sp)
                    Text(stringResource(R.string.app_subtitle), fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f), letterSpacing = 1.sp)
                }
                IconButton(onClick = ::toggleNotification) {
                    Icon(
                        if (morningNotification) Icons.Rounded.Notifications else Icons.Rounded.NotificationsOff,
                        contentDescription = stringResource(R.string.notif_toggle_cd),
                        tint = Color.White.copy(alpha = if (morningNotification) 1f else 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = city,
                    onValueChange = { viewModel.updateCity(it) },
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(24.dp)),
                    placeholder = { Text(stringResource(R.string.search_city_hint), color = Color.White.copy(alpha = 0.5f)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.15f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search, capitalization = KeyboardCapitalization.Words),
                    keyboardActions = KeyboardActions(onSearch = { viewModel.fetchWeather(); focusManager.clearFocus() }),
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )
                IconButton(
                    onClick = { viewModel.fetchWeather(); focusManager.clearFocus() },
                    modifier = Modifier.padding(start = 8.dp).size(48.dp).clip(RoundedCornerShape(24.dp)).background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Rounded.Search, contentDescription = stringResource(R.string.search_cd), tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(300)) },
                label = "weatherContent",
                contentKey = { it::class },
                modifier = Modifier.weight(1f)
            ) { state ->
                when (state) {
                    is WeatherUiState.Idle, is WeatherUiState.Loading -> LoadingView()
                    is WeatherUiState.Error -> ErrorView(message = state.message, onRetry = { viewModel.fetchWeather() })
                    is WeatherUiState.Success -> {
                        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            if (state.days.size > 1) {
                                DaySelector(days = state.days, selectedIndex = state.selectedIndex, onSelect = viewModel::selectDay)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            WeatherHeader(cityLabel = state.cityLabel, day = state.selectedDay)
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp).height(1.dp).background(
                                    Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.3f), Color.Transparent))
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            val wardrobeOutfit = state.wardrobeOutfit
                            if (wardrobeOutfit != null) {
                                WardrobeOutfitCard(
                                    outfit = wardrobeOutfit,
                                    canWearToday = state.canWearToday,
                                    onWear = viewModel::wearSuggestedOutfit,
                                    aiAvailable = viewModel.aiAvailable,
                                    aiEnabled = aiEnabled,
                                    aiStatus = state.aiStatus,
                                    onToggleAi = viewModel::setAiEnabled
                                )
                            } else {
                                WardrobePromptCard(onOpenWardrobe = onOpenWardrobe)
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            OutfitCard(outfit = state.outfit)
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}
