package com.example.myapplication

import android.content.Intent
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
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.MeetupPayload
import com.example.myapplication.ui.WeatherScreen
import com.example.myapplication.ui.meetup.MeetupScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.wardrobe.WardrobeScreen
import com.example.myapplication.viewmodel.MeetupViewModel
import com.example.myapplication.viewmodel.WardrobeViewModel
import com.example.myapplication.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {

    /** Lien d'invitation reçu (au lancement ou pendant que l'appli tourne). */
    private val incomingLink = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        incomingLink.value = intent?.dataString
        setContent {
            MyApplicationTheme {
                AppRoot(incomingLink.value) { incomingLink.value = null }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        incomingLink.value = intent.dataString
    }
}

private const val TAB_WEATHER = 0
private const val TAB_WARDROBE = 1
private const val TAB_MEETUP = 2

@Composable
private fun AppRoot(incomingLink: String?, onLinkConsumed: () -> Unit) {
    val weatherViewModel: WeatherViewModel = viewModel()
    val wardrobeViewModel: WardrobeViewModel = viewModel()
    val meetupViewModel: MeetupViewModel = viewModel()
    var tab by rememberSaveable { mutableIntStateOf(TAB_WEATHER) }

    // Un lien d'invitation ouvre directement le rendez-vous reçu.
    LaunchedEffect(incomingLink) {
        if (incomingLink != null && MeetupPayload.decode(incomingLink) != null) {
            meetupViewModel.openPayload(incomingLink)
            tab = TAB_MEETUP
        }
        if (incomingLink != null) onLinkConsumed()
    }

    // Écran météo = fond coloré + texte blanc → icônes de barre d'état claires.
    // Autres onglets = thème Material → icônes sombres en mode clair.
    val lightStatusBarIcons = tab != TAB_WEATHER && !isSystemInDarkTheme()
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
                    label = { Text(stringResource(R.string.tab_weather)) }
                )
                NavigationBarItem(
                    selected = tab == TAB_WARDROBE,
                    onClick = { tab = TAB_WARDROBE },
                    icon = { Icon(Icons.Rounded.Checkroom, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_wardrobe)) }
                )
                NavigationBarItem(
                    selected = tab == TAB_MEETUP,
                    onClick = { tab = TAB_MEETUP },
                    icon = { Icon(Icons.Rounded.Group, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_meetup)) }
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
                TAB_WARDROBE -> WardrobeScreen(viewModel = wardrobeViewModel)
                else -> MeetupScreen(viewModel = meetupViewModel)
            }
        }
    }
}
