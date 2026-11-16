package com.example.myapplication.ui

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.WeatherUiState
import com.example.myapplication.ui.components.DaySelector
import com.example.myapplication.ui.components.ErrorView
import com.example.myapplication.ui.components.LoadingView
import com.example.myapplication.ui.components.OutfitCard
import com.example.myapplication.ui.components.WeatherBackground
import com.example.myapplication.ui.components.WeatherHeader
import com.example.myapplication.viewmodel.WeatherViewModel

@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val city by viewModel.city.collectAsState()
    val focusManager = LocalFocusManager.current

    // Le fond suit la journée sélectionnée
    val selectedDay = (uiState as? WeatherUiState.Success)?.selectedDay
    val condition = selectedDay?.condition ?: "Clear"
    val isNight = selectedDay?.isNight ?: false

    WeatherBackground(
        condition = condition,
        isNight = isNight
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // App Title
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Outfit Weather",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Text(
                text = "Advisor",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.7f),
                letterSpacing = 2.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = city,
                    onValueChange = { viewModel.updateCity(it) },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp)),
                    placeholder = {
                        Text(
                            "Search city...",
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.15f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search,
                        capitalization = KeyboardCapitalization.Words
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            viewModel.fetchWeather()
                            focusManager.clearFocus()
                        }
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )

                IconButton(
                    onClick = {
                        viewModel.fetchWeather()
                        focusManager.clearFocus()
                    },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    fadeIn(tween(500)) togetherWith fadeOut(tween(300))
                },
                label = "weatherContent",
                // Ne pas rejouer la transition quand on change simplement de jour
                contentKey = { it::class },
                modifier = Modifier.weight(1f)
            ) { state ->
                when (state) {
                    is WeatherUiState.Idle -> LoadingView()
                    is WeatherUiState.Loading -> LoadingView()
                    is WeatherUiState.Error -> ErrorView(
                        message = state.message,
                        onRetry = { viewModel.fetchWeather() }
                    )
                    is WeatherUiState.Success -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            if (state.days.size > 1) {
                                DaySelector(
                                    days = state.days,
                                    selectedIndex = state.selectedIndex,
                                    onSelect = viewModel::selectDay
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            WeatherHeader(
                                cityLabel = state.cityLabel,
                                day = state.selectedDay
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Divider
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 48.dp)
                                    .height(1.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.White.copy(alpha = 0.3f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutfitCard(outfit = state.outfit)

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}
