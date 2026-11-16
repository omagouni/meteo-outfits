package com.example.myapplication.ui.wardrobe

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.myapplication.R
import com.example.myapplication.ui.UiText
import com.example.myapplication.ui.asString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.ClothingCategory
import com.example.myapplication.data.model.ClothingItem
import com.example.myapplication.data.remote.LinkPreview
import com.example.myapplication.data.remote.LinkPreviewFetcher
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItemEditorScreen(
    initial: ClothingItem,
    isNew: Boolean,
    onFetchPreview: suspend (String) -> Result<LinkPreview>,
    onSave: (ClothingItem) -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit
) {
    var draft by remember(initial.id) { mutableStateOf(initial) }
    var fetching by remember { mutableStateOf(false) }
    var fetchMessage by remember { mutableStateOf<UiText?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun openUri(uri: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
        } catch (_: ActivityNotFoundException) {
            fetchMessage = UiText.res(R.string.no_browser)
        }
    }

    fun importFromLink() {
        fetching = true
        fetchMessage = null
        scope.launch {
            onFetchPreview(draft.productUrl).fold(
                onSuccess = { preview ->
                    draft = draft.copy(
                        name = draft.name.ifBlank { preview.title.orEmpty() },
                        imageUrl = preview.imageUrl ?: draft.imageUrl,
                        brand = draft.brand.ifBlank { preview.brand.orEmpty() }
                    )
                    fetchMessage = UiText.res(
                        if (preview.title == null && preview.imageUrl == null) R.string.import_none else R.string.imported_ok
                    )
                },
                onFailure = { e ->
                    fetchMessage = UiText.res(R.string.import_failed, e.message ?: "error")
                }
            )
            fetching = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (isNew) R.string.new_item else R.string.edit_item)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (!isNew) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                    TextButton(onClick = { onSave(draft) }, enabled = draft.name.isNotBlank()) {
                        Text(stringResource(R.string.save), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- Lien produit ---
            Text(stringResource(R.string.product_link), style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = draft.productUrl,
                onValueChange = { draft = draft.copy(productUrl = it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.link_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val validUrl = LinkPreviewFetcher.normalizeUrl(draft.productUrl)
                Button(onClick = ::importFromLink, enabled = validUrl != null && !fetching) {
                    if (fetching) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.import_label))
                }
                OutlinedButton(onClick = { validUrl?.let { openUri(it.toString()) } }, enabled = validUrl != null) {
                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.open_label))
                }
            }
            fetchMessage?.let {
                Text(it.asString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (draft.imageUrl.isNotBlank() || draft.name.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ClothingThumbnail(draft, size = 96)
                    Spacer(Modifier.width(12.dp))
                    if (draft.imageUrl.isNotBlank()) {
                        TextButton(onClick = { draft = draft.copy(imageUrl = "") }) { Text(stringResource(R.string.remove_photo)) }
                    }
                }
            }

            // --- Infos ---
            OutlinedTextField(
                value = draft.name,
                onValueChange = { draft = draft.copy(name = it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.name_required)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = draft.brand,
                    onValueChange = { draft = draft.copy(brand = it) },
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.brand)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                OutlinedTextField(
                    value = draft.reference,
                    onValueChange = { draft = draft.copy(reference = it) },
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.reference)) },
                    singleLine = true
                )
            }
            val searchQuery = listOf(draft.brand, draft.reference, draft.name).filter { it.isNotBlank() }.joinToString(" ")
            OutlinedButton(
                onClick = { openUri("https://www.google.com/search?q=" + Uri.encode(searchQuery)) },
                enabled = searchQuery.isNotBlank()
            ) {
                Icon(Icons.Rounded.TravelExplore, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.find_online))
            }

            // --- Catégorie ---
            Text(stringResource(R.string.category), style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ClothingCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = draft.category == cat,
                        onClick = { draft = draft.copy(category = cat) },
                        label = { Text("${cat.emoji} ${stringResource(cat.labelRes)}") }
                    )
                }
            }

            // --- Chaleur & protection ---
            Text(
                stringResource(R.string.warmth_label, stringResource(ClothingItem.warmthRes(draft.warmth)), draft.warmth),
                style = MaterialTheme.typography.titleSmall
            )
            Slider(
                value = draft.warmth.toFloat(),
                onValueChange = { draft = draft.copy(warmth = it.roundToInt()) },
                valueRange = ClothingItem.MIN_WARMTH.toFloat()..ClothingItem.MAX_WARMTH.toFloat(),
                steps = ClothingItem.MAX_WARMTH - ClothingItem.MIN_WARMTH - 1
            )
            SwitchRow(stringResource(R.string.waterproof), draft.waterproof) { draft = draft.copy(waterproof = it) }
            SwitchRow(stringResource(R.string.windproof), draft.windproof) { draft = draft.copy(windproof = it) }
            SwitchRow(stringResource(R.string.favorite), draft.favorite) { draft = draft.copy(favorite = it) }

            OutlinedTextField(
                value = draft.color,
                onValueChange = { draft = draft.copy(color = it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.color)) },
                placeholder = { Text(stringResource(R.string.color_hint)) },
                singleLine = true
            )
            OutlinedTextField(
                value = draft.notes,
                onValueChange = { draft = draft.copy(notes = it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.notes)) },
                minLines = 2
            )

            Button(
                onClick = { onSave(draft) },
                enabled = draft.name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(if (isNew) R.string.add_to_wardrobe else R.string.save_changes)) }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_q)) },
            text = { Text(stringResource(R.string.delete_body, draft.name)) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
