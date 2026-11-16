package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.WeatherScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.wardrobe.WardrobeScreen
import com.example.myapplication.viewmodel.WardrobeViewModel
import com.example.myapplication.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppRoot()
            }
        }
    }
}

private const val TAB_WEATHER = 0
private const val TAB_WARDROBE = 1

@Composable
private fun AppRoot() {
    val weatherViewModel: WeatherViewModel = viewModel()
    val wardrobeViewModel: WardrobeViewModel = viewModel()
    var tab by rememberSaveable { mutableIntStateOf(TAB_WEATHER) }

    // Écran météo = fond coloré + texte blanc → icônes de barre d'état claires.
    // Garde-robe = thème Material → icônes sombres en mode clair.
    // Lu ici (et non dans le SideEffect) pour que ce scope se recompose au changement d'onglet.
    val lightStatusBarIcons = tab == TAB_WARDROBE && !isSystemInDarkTheme()
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as ComponentActivity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = lightStatusBarIcons
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == TAB_WEATHER,
                    onClick = { tab = TAB_WEATHER },
                    icon = { Icon(Icons.Rounded.WbSunny, contentDescription = null) },
                    label = { Text("Weather") }
                )
                NavigationBarItem(
                    selected = tab == TAB_WARDROBE,
                    onClick = { tab = TAB_WARDROBE },
                    icon = { Icon(Icons.Rounded.Checkroom, contentDescription = null) },
                    label = { Text("Wardrobe") }
                )
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            when (tab) {
                TAB_WEATHER -> WeatherScreen(
                    viewModel = weatherViewModel,
                    onOpenWardrobe = { tab = TAB_WARDROBE }
                )
                else -> WardrobeScreen(viewModel = wardrobeViewModel)
            }
        }
    }
}
