package com.example.myapplication.ui.meetup

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.data.model.ClothingCategory
import com.example.myapplication.data.model.Meetup
import com.example.myapplication.data.model.MeetupDirection
import com.example.myapplication.data.model.OutfitPiece
import com.example.myapplication.domain.ColorFamily
import com.example.myapplication.ui.asString
import com.example.myapplication.ui.components.DaySelector
import com.example.myapplication.ui.components.MissingRow
import com.example.myapplication.ui.components.PickRow
import com.example.myapplication.ui.components.dayLabel
import com.example.myapplication.ui.components.getWeatherEmoji
import com.example.myapplication.viewmodel.MeetupScreenState
import com.example.myapplication.viewmodel.MeetupViewModel
import kotlin.math.roundToInt

@Composable
fun MeetupScreen(viewModel: MeetupViewModel) {
    val screen by viewModel.screen.collectAsState()
    val share by viewModel.share.collectAsState()
    val context = LocalContext.current

    // Feuille de partage Android dès qu'un texte d'invitation est prêt.
    LaunchedEffect(share) {
        val content = share ?: return@LaunchedEffect
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, content.subject.resolve(context))
            putExtra(Intent.EXTRA_TEXT, content.text)
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.meetup_share_cd)))
        viewModel.shareConsumed()
    }

    when (val s = screen) {
        is MeetupScreenState.List -> MeetupList(viewModel)
        is MeetupScreenState.Create -> { BackHandler { viewModel.backToList() }; CreateMeetup(s, viewModel) }
        is MeetupScreenState.Detail -> { BackHandler { viewModel.backToList() }; MeetupDetail(s, viewModel) }
    }
}

// ---------------------------------------------------------------- Liste

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeetupList(viewModel: MeetupViewModel) {
    val meetups by viewModel.meetups.collectAsState()
    val pasteError by viewModel.pasteError.collectAsState()
    var pasteOpen by remember { mutableStateOf(false) }
    var pasted by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Column {
                    Text(stringResource(R.string.meetup_title), fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.meetup_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                }
            })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::startCreate,
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.meetup_new)) }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedButton(
                onClick = { pasteOpen = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            ) { Text(stringResource(R.string.meetup_received)) }

            if (meetups.isEmpty()) {
                Column(
                    Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("👯", fontSize = 56.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.meetup_empty_title), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(R.string.meetup_empty_body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(meetups.sortedByDescending { it.createdAt }, key = { it.id }) { meetup ->
                        MeetupRow(meetup) { viewModel.open(meetup) }
                    }
                }
            }
        }
    }

    if (pasteOpen) {
        AlertDialog(
            onDismissRequest = { pasteOpen = false; viewModel.clearPasteError() },
            title = { Text(stringResource(R.string.meetup_paste_title)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = pasted,
                        onValueChange = { pasted = it; viewModel.clearPasteError() },
                        placeholder = { Text(stringResource(R.string.meetup_paste_hint)) },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pasteError) {
                        Spacer(Modifier.height(6.dp))
                        Text(stringResource(R.string.meetup_paste_invalid), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { if (viewModel.openPayload(pasted)) { pasteOpen = false; pasted = "" } }) {
                    Text(stringResource(R.string.meetup_open))
                }
            },
            dismissButton = { TextButton(onClick = { pasteOpen = false; viewModel.clearPasteError() }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun MeetupRow(meetup: Meetup, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (meetup.direction == MeetupDirection.SENT) "📤" else "📥", fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "${meetup.friendName} · ${meetup.city}",
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.meetup_weather_line, dayLabel(meetup.dayLabel), meetup.feelsLike ?: 0, meetup.condition),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Row { (meetup.myOutfit + meetup.friendOutfit).map { it.color }.distinct().take(6).forEach { ColorDot(it) } }
            }
        }
    }
}

// ---------------------------------------------------------------- Création

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateMeetup(form: MeetupScreenState.Create, viewModel: MeetupViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.meetup_new)) },
                navigationIcon = { IconButton(onClick = viewModel::backToList) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back)) } }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = form.myName, onValueChange = { v -> viewModel.updateCreate { copy(myName = v) } },
                label = { Text(stringResource(R.string.meetup_my_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            OutlinedTextField(
                value = form.friendName, onValueChange = { v -> viewModel.updateCreate { copy(friendName = v) } },
                label = { Text(stringResource(R.string.meetup_friend_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            OutlinedTextField(
                value = form.city, onValueChange = { v -> viewModel.updateCreate { copy(city = v, days = emptyList()) } },
                label = { Text(stringResource(R.string.meetup_city)) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { viewModel.loadForecast() })
            )
            Button(onClick = viewModel::loadForecast, enabled = form.city.isNotBlank() && !form.loading) {
                if (form.loading) { CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
                Text(if (form.loading) stringResource(R.string.meetup_loading_weather, form.city) else stringResource(R.string.meetup_find_weather))
            }
            form.error?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            if (form.days.isNotEmpty()) {
                Text(stringResource(R.string.meetup_day), style = MaterialTheme.typography.titleSmall)
                Box(Modifier.clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)).padding(vertical = 8.dp)) {
                    DaySelector(
                        days = form.days, selectedIndex = form.selectedIndex,
                        onSelect = { i -> viewModel.updateCreate { copy(selectedIndex = i) } }, light = false
                    )
                }
                form.selectedDay?.let { day ->
                    Text(
                        "${getWeatherEmoji(day.condition, day.isNight)} ${form.cityLabel} · ${day.feelsLike.roundToInt()}° · ${day.description}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Button(
                    onClick = viewModel::createMeetup,
                    enabled = form.friendName.isNotBlank() && form.myName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.meetup_my_outfit)) }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ---------------------------------------------------------------- Détail

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun MeetupDetail(detail: MeetupScreenState.Detail, viewModel: MeetupViewModel) {
    val meetup = detail.meetup
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${meetup.friendName} · ${meetup.city}") },
                navigationIcon = { IconButton(onClick = viewModel::backToList) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back)) } },
                actions = { IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.meetup_delete)) } }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val day = detail.day
            Text(
                if (day != null) "${getWeatherEmoji(day.condition, day.isNight)} ${dayLabel(day.label)} · ${day.feelsLike.roundToInt()}° · ${day.description}"
                else stringResource(R.string.meetup_weather_line, dayLabel(meetup.dayLabel), meetup.feelsLike ?: 0, meetup.condition),
                style = MaterialTheme.typography.titleMedium
            )
            if (meetup.direction == MeetupDirection.RECEIVED) {
                Text(stringResource(R.string.meetup_received_label, meetup.friendName), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (detail.loading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.meetup_loading_weather, meetup.city))
                }
            }
            detail.error?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }

            // --- Tenue de l'ami (si connue) ---
            if (meetup.friendOutfit.isNotEmpty()) {
                SectionCard(title = stringResource(R.string.meetup_outfit_of, meetup.friendName)) {
                    meetup.friendOutfit.forEach { PieceRow(it) }
                }
            }

            // --- Garde-robe vide : choix des couleurs ---
            if (detail.usesPalette) {
                SectionCard(title = stringResource(R.string.meetup_pick_colors_title)) {
                    Text(stringResource(R.string.meetup_pick_colors_body), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    listOf(ClothingCategory.OUTERWEAR, ClothingCategory.TOP, ClothingCategory.BOTTOM, ClothingCategory.SHOES).forEach { cat ->
                        Spacer(Modifier.height(6.dp))
                        Text(stringResource(R.string.meetup_pick_color_for, "${cat.emoji} ${stringResource(cat.labelRes)}"), style = MaterialTheme.typography.labelLarge)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            ColorFamily.pickable.forEach { color ->
                                val selected = detail.palette[cat] == color
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.setPaletteColor(cat, if (selected) null else color) },
                                    leadingIcon = { ColorDot(color) },
                                    label = { Text(stringResource(color.labelRes)) }
                                )
                            }
                        }
                    }
                }
            }

            // --- Ma tenue ---
            val picks = detail.myPicks
            if (picks.isNotEmpty() || (detail.matchy?.outfit ?: detail.outfit)?.missing?.isNotEmpty() == true) {
                SectionCard(title = stringResource(R.string.meetup_you_wear)) {
                    detail.matchy?.let { m ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(m.matchLevelRes), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            m.linkColor?.let { link ->
                                Spacer(Modifier.width(8.dp)); ColorDot(link); Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.meetup_link_color, stringResource(link.labelRes)), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                    picks.forEach { PickRow(it, light = false); Spacer(Modifier.height(6.dp)) }
                    (detail.matchy?.outfit ?: detail.outfit)?.missing?.forEach { MissingRow(it, light = false); Spacer(Modifier.height(6.dp)) }
                }

                Button(onClick = viewModel::shareOutfit, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                    Text(
                        if (meetup.direction == MeetupDirection.SENT && meetup.friendOutfit.isEmpty()) stringResource(R.string.meetup_send_invite)
                        else stringResource(R.string.meetup_send_back, meetup.friendName)
                    )
                }
                if (!detail.usesPalette) {
                    OutlinedButton(onClick = viewModel::wearMyOutfit, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.meetup_wear)) }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.meetup_delete)) },
            confirmButton = { TextButton(onClick = { confirmDelete = false; viewModel.delete(meetup) }) { Text(stringResource(R.string.delete)) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun PieceRow(piece: OutfitPiece) {
    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(piece.emoji.ifBlank { piece.category.emoji }, fontSize = 20.sp)
        Spacer(Modifier.width(10.dp))
        ColorDot(piece.color)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(piece.name.ifBlank { stringResource(piece.category.labelRes) }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(stringResource(piece.color.labelRes), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ColorDot(color: ColorFamily) {
    Box(
        Modifier.padding(end = 4.dp).size(16.dp).clip(CircleShape)
            .background(Color(color.hex))
            .padding(1.dp)
            .clip(CircleShape)
    )
}
