package com.example.myapplication.ui.wardrobe

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.example.myapplication.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.myapplication.data.model.ClothingCategory
import com.example.myapplication.data.model.ClothingItem
import com.example.myapplication.viewmodel.WardrobeSort
import com.example.myapplication.viewmodel.WardrobeViewModel

@Composable
fun WardrobeScreen(viewModel: WardrobeViewModel) {
    // null = liste ; sinon le vêtement en cours d'édition (nouveau ou existant)
    var editing by remember { mutableStateOf<ClothingItem?>(null) }
    var isNew by remember { mutableStateOf(false) }

    val current = editing
    if (current != null) {
        BackHandler { editing = null }
        ItemEditorScreen(
            initial = current,
            isNew = isNew,
            onFetchPreview = viewModel::fetchPreview,
            onSave = { viewModel.save(it); editing = null },
            onDelete = { viewModel.delete(current.id); editing = null },
            onClose = { editing = null }
        )
    } else {
        WardrobeList(
            viewModel = viewModel,
            onAdd = { isNew = true; editing = ClothingItem() },
            onOpen = { isNew = false; editing = it }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WardrobeList(
    viewModel: WardrobeViewModel,
    onAdd: () -> Unit,
    onOpen: (ClothingItem) -> Unit
) {
    val items by viewModel.visibleItems.collectAsState()
    val total by viewModel.totalCount.collectAsState()
    val query by viewModel.query.collectAsState()
    val category by viewModel.category.collectAsState()
    val sort by viewModel.sort.collectAsState()
    var sortMenuOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.wardrobe_title), fontWeight = FontWeight.Bold)
                        Text(
                            stringResource(R.string.wardrobe_subtitle, pluralStringResource(R.plurals.items_count, total, total), stringResource(sort.labelRes)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { sortMenuOpen = true }) {
                            Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = stringResource(R.string.sort_cd))
                        }
                        DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                            WardrobeSort.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            stringResource(option.labelRes),
                                            fontWeight = if (option == sort) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = { viewModel.setSort(option); sortMenuOpen = false }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_item)) }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text(stringResource(R.string.search_wardrobe_hint)) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setQuery("") }) {
                            Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.clear_search))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = category == null,
                        onClick = { viewModel.setCategory(null) },
                        label = { Text(stringResource(R.string.filter_all)) }
                    )
                }
                items(ClothingCategory.entries) { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { viewModel.setCategory(if (category == cat) null else cat) },
                        label = { Text("${cat.emoji} ${stringResource(cat.labelRes)}") }
                    )
                }
            }

            if (items.isEmpty()) {
                EmptyWardrobe(hasAnyItem = total > 0)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        ClothingItemRow(
                            item = item,
                            onClick = { onOpen(item) },
                            onToggleFavorite = { viewModel.toggleFavorite(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyWardrobe(hasAnyItem: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(if (hasAnyItem) "🔍" else "👚", fontSize = 56.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(if (hasAnyItem) R.string.empty_no_match else R.string.empty_title),
            style = MaterialTheme.typography.titleMedium
        )
        if (!hasAnyItem) {
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ClothingItemRow(
    item: ClothingItem,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClothingThumbnail(item, size = 64)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.name.ifBlank { stringResource(R.string.untitled) },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val subtitle = listOf(item.brand, item.reference).filter { it.isNotBlank() }.joinToString(" · ")
                if (subtitle.isNotEmpty()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WarmthDots(item.warmth)
                    if (item.waterproof) Badge("💧")
                    if (item.windproof) Badge("🌬️")
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (item.wearCount == 0) stringResource(R.string.never_worn) else stringResource(R.string.worn_times, item.wearCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (item.favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = stringResource(if (item.favorite) R.string.fav_remove else R.string.fav_add),
                    tint = if (item.favorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun Badge(text: String) {
    Text(text, fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp))
}

@Composable
fun WarmthDots(warmth: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        (ClothingItem.MIN_WARMTH..ClothingItem.MAX_WARMTH).forEach { level ->
            Box(
                Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(
                        if (level <= warmth) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
            )
        }
    }
}

/** Photo du vêtement si disponible, sinon l'emoji de sa catégorie. */
@Composable
fun ClothingThumbnail(item: ClothingItem, size: Int, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    val fallback: @Composable () -> Unit = {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) { Text(item.category.emoji, fontSize = (size / 2).sp) }
    }
    Box(modifier.size(size.dp).clip(shape)) {
        if (item.imageUrl.isBlank()) fallback()
        else SubcomposeAsyncImage(
            model = item.imageUrl,
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = { fallback() },
            error = { fallback() }
        )
    }
}
