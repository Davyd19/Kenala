package com.app.kenala.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.app.kenala.ui.theme.*

data class AdventureSuggestion(
    val id: Int,
    val locationName: String,
    val category: String,
    val description: String,
    val submittedBy: String,
    val submittedDate: String,
    val status: SuggestionStatus
)

enum class SuggestionStatus {
    PENDING, APPROVED, REJECTED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventureSuggestionScreen(onNavigateBack: () -> Unit) {
    var suggestions by remember {
        mutableStateOf(
            listOf(
                AdventureSuggestion(
                    1,
                    "Warung Kopi Pak Udin",
                    "Kuliner",
                    "Kedai kopi legendaris yang sudah ada sejak tahun 1970-an. Menyajikan kopi susu khas dengan resep turun temurun.",
                    "Anda",
                    "20 Okt 2025",
                    SuggestionStatus.APPROVED
                ),
                AdventureSuggestion(
                    2,
                    "Taman Miniatur Kota",
                    "Rekreasi",
                    "Taman yang menampilkan miniatur bangunan bersejarah di kota ini. Cocok untuk foto dan belajar sejarah.",
                    "Anda",
                    "18 Okt 2025",
                    SuggestionStatus.PENDING
                ),
                AdventureSuggestion(
                    3,
                    "Studio Batik Ibu Siti",
                    "Seni & Budaya",
                    "Workshop membatik tradisional yang membuka kelas untuk umum setiap akhir pekan.",
                    "Anda",
                    "15 Okt 2025",
                    SuggestionStatus.APPROVED
                )
            )
        )
    }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedSuggestion by remember { mutableStateOf<AdventureSuggestion?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saran Lokasi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Usulkan Lokasi") },
                containerColor = AccentColor,
                contentColor = DeepBlue
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (suggestions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Explore,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Belum Ada Saran",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Usulkan lokasi petualangan favoritmu!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 25.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Saran Anda",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = OceanBlue.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "${suggestions.size} saran",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = OceanBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                items(suggestions) { suggestion ->
                    SuggestionCard(
                        suggestion = suggestion,
                        onClick = { selectedSuggestion = suggestion }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddSuggestionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name: String, category: String, description: String ->
                val newSuggestion = AdventureSuggestion(
                    id = suggestions.size + 1,
                    locationName = name,
                    category = category,
                    description = description,
                    submittedBy = "Anda",
                    submittedDate = "Hari ini",
                    status = SuggestionStatus.PENDING
                )
                suggestions = suggestions + newSuggestion
                showAddDialog = false
            }
        )
    }

    selectedSuggestion?.let { suggestion ->
        SuggestionDetailDialog(
            suggestion = suggestion,
            onDismiss = { selectedSuggestion = null },
            onEdit = { editedSuggestion: AdventureSuggestion ->
                suggestions = suggestions.map {
                    if (it.id == editedSuggestion.id) editedSuggestion else it
                }
                selectedSuggestion = null
            },
            onDelete = { suggestionId: Int ->
                suggestions = suggestions.filter { it.id != suggestionId }
                selectedSuggestion = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionCard(
    suggestion: AdventureSuggestion,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = suggestion.locationName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Category,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = suggestion.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    </Column>
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(suggestion.status)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = suggestion.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Diusulkan ${suggestion.submittedDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    @Composable
    fun StatusBadge(status: SuggestionStatus) {
        val (text, color) = when (status) {
            SuggestionStatus.PENDING -> "Menunggu" to Color(0xFFF59E0B)
            SuggestionStatus.APPROVED -> "Disetujui" to ForestGreen
            SuggestionStatus.REJECTED -> "Ditolak" to ErrorColor
        }

        Surface(
            color = color.copy(alpha = 0.12f),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AddSuggestionDialog(
        onDismiss: () -> Unit,
        onConfirm: (String, String, String) -> Unit
    ) {
        var locationName by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf("Kuliner") }
        var description by remember { mutableStateOf("") }
        var showCategoryMenu by remember { mutableStateOf(false) }

        val categories = listOf("Kuliner", "Rekreasi", "Seni & Budaya", "Sejarah", "Belanja", "Alam")

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Usulkan Lokasi Baru") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = locationName,
                        onValueChange = { locationName = it },
                        label = { Text("Nama Lokasi") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    ExposedDropdownMenuBox(
                        expanded = showCategoryMenu,
                        onExpandedChange = { showCategoryMenu = !showCategoryMenu }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kategori") },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        selectedCategory = category
                                        showCategoryMenu = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Deskripsi") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (locationName.isNotBlank() && description.isNotBlank()) {
                            onConfirm(locationName, selectedCategory, description)
                        }
                    },
                    enabled = locationName.isNotBlank() && description.isNotBlank()
                ) {
                    Text("Kirim")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Batal")
                }
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SuggestionDetailDialog(
        suggestion: AdventureSuggestion,
        onDismiss: () -> Unit,
        onEdit: (AdventureSuggestion) -> Unit,
        onDelete: (Int) -> Unit
    ) {
        var isEditing by remember { mutableStateOf(false) }
        var editedName by remember { mutableStateOf(suggestion.locationName) }
        var editedDescription by remember { mutableStateOf(suggestion.description) }
        var showDeleteConfirm by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(if (isEditing) "Edit Saran" else "Detail Saran")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            label = { Text("Nama Lokasi") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editedDescription,
                            onValueChange = { editedDescription = it },
                            label = { Text("Deskripsi") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusBadge(suggestion.status)
                            Text(
                                text = suggestion.locationName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Category,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(suggestion.category, style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = suggestion.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Diusulkan ${suggestion.submittedDate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (isEditing) {
                    TextButton(
                        onClick = {
                            onEdit(
                                suggestion.copy(
                                    locationName = editedName,
                                    description = editedDescription
                                )
                            )
                        }
                    ) {
                        Text("Simpan")
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (suggestion.status == SuggestionStatus.PENDING) {
                            IconButton(onClick = { isEditing = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Hapus",
                                    tint = ErrorColor
                                )
                            }
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (isEditing) {
                            isEditing = false
                        } else {
                            onDismiss()
                        }
                    }
                ) {
                    Text(if (isEditing) "Batal" else "Tutup")
                }
            }
        )

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                icon = { Icon(Icons.Default.Warning, contentDescription = null) },
                title = { Text("Hapus Saran?") },
                text = { Text("Apakah Anda yakin ingin menghapus saran lokasi ini?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDelete(suggestion.id)
                            showDeleteConfirm = false
                        }
                    ) {
                        Text("Hapus", color = ErrorColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Batal")
                    }
                }
            )
        }
    }