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
    var fetchMessage by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun openUri(uri: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
        } catch (_: ActivityNotFoundException) {
            fetchMessage = "No browser available to open the link."
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
                    fetchMessage = if (preview.title == null && preview.imageUrl == null)
                        "No product info found on this page — fill it in manually."
                    else "Imported from the link ✓"
                },
                onFailure = { e ->
                    fetchMessage = "Couldn't read this page (${e.message ?: "error"}). " +
                        "Some shops block apps — fill it in manually."
                }
            )
            fetching = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "New item" else "Edit item") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isNew) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete")
                        }
                    }
                    TextButton(onClick = { onSave(draft) }, enabled = draft.name.isNotBlank()) {
                        Text("Save", fontWeight = FontWeight.Bold)
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
            Text("Product link", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = draft.productUrl,
                onValueChange = { draft = draft.copy(productUrl = it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("https://shop.com/product/…") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val validUrl = LinkPreviewFetcher.normalizeUrl(draft.productUrl)
                Button(onClick = ::importFromLink, enabled = validUrl != null && !fetching) {
                    if (fetching) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Import")
                }
                OutlinedButton(onClick = { validUrl?.let { openUri(it.toString()) } }, enabled = validUrl != null) {
                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Open")
                }
            }
            fetchMessage?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (draft.imageUrl.isNotBlank() || draft.name.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ClothingThumbnail(draft, size = 96)
                    Spacer(Modifier.width(12.dp))
                    if (draft.imageUrl.isNotBlank()) {
                        TextButton(onClick = { draft = draft.copy(imageUrl = "") }) { Text("Remove photo") }
                    }
                }
            }

            // --- Infos ---
            OutlinedTextField(
                value = draft.name,
                onValueChange = { draft = draft.copy(name = it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Name *") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = draft.brand,
                    onValueChange = { draft = draft.copy(brand = it) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Brand") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                OutlinedTextField(
                    value = draft.reference,
                    onValueChange = { draft = draft.copy(reference = it) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Reference") },
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
                Text("Find it online")
            }

            // --- Catégorie ---
            Text("Category", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ClothingCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = draft.category == cat,
                        onClick = { draft = draft.copy(category = cat) },
                        label = { Text("${cat.emoji} ${cat.label}") }
                    )
                }
            }

            // --- Chaleur & protection ---
            Text(
                "Warmth: ${ClothingItem.warmthLabel(draft.warmth)} (${draft.warmth}/5)",
                style = MaterialTheme.typography.titleSmall
            )
            Slider(
                value = draft.warmth.toFloat(),
                onValueChange = { draft = draft.copy(warmth = it.roundToInt()) },
                valueRange = ClothingItem.MIN_WARMTH.toFloat()..ClothingItem.MAX_WARMTH.toFloat(),
                steps = ClothingItem.MAX_WARMTH - ClothingItem.MIN_WARMTH - 1
            )
            SwitchRow("💧 Waterproof", draft.waterproof) { draft = draft.copy(waterproof = it) }
            SwitchRow("🌬️ Windproof", draft.windproof) { draft = draft.copy(windproof = it) }
            SwitchRow("❤️ Favorite", draft.favorite) { draft = draft.copy(favorite = it) }

            OutlinedTextField(
                value = draft.color,
                onValueChange = { draft = draft.copy(color = it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Color") },
                singleLine = true
            )
            OutlinedTextField(
                value = draft.notes,
                onValueChange = { draft = draft.copy(notes = it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes") },
                minLines = 2
            )

            Button(
                onClick = { onSave(draft) },
                enabled = draft.name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (isNew) "Add to wardrobe" else "Save changes") }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this item?") },
            text = { Text("“${draft.name}” will be removed from your wardrobe.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
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
