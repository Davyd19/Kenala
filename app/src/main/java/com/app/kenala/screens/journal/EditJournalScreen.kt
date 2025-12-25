package com.app.kenala.screens.journal

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.app.kenala.ui.theme.AccentColor
import com.app.kenala.ui.theme.DeepBlue
import com.app.kenala.utils.UrlUtils
import com.app.kenala.viewmodel.JournalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditJournalScreen(
    journalId: String,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    viewModel: JournalViewModel = viewModel()
) {
    val journals by viewModel.journals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val journalSaved by viewModel.journalSaved.collectAsState()

    val journal = remember(journals, journalId) {
        journals.find { it.id == journalId }
    }

    var title by remember { mutableStateOf("") }
    var story by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    LaunchedEffect(journal) {
        journal?.let {
            if (title.isEmpty()) title = it.title
            if (story.isEmpty()) story = it.story
        }
    }

    LaunchedEffect(journalSaved) {
        if (journalSaved) {
            viewModel.resetJournalSaved()
            onSaveClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Jurnal") },
                navigationIcon = {
                    IconButton(onClick = onBackClick, enabled = !isLoading) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }, enabled = !isLoading) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus Jurnal")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        if (showDeleteDialog) {
            DeleteConfirmationDialog(
                onConfirm = {
                    showDeleteDialog = false
                    journal?.let { viewModel.deleteJournal(it.id) }
                    onDeleteClick()
                },
                onDismiss = {
                    showDeleteDialog = false
                }
            )
        }

        if (isLoading && journal == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (journal == null && !isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Jurnal tidak ditemukan",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 25.dp, vertical = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(24.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(AccentColor)
                    )
                    Text(
                        text = "Foto Jurnal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                val modelData = if (imageUri != null) {
                    imageUri
                } else {
                    UrlUtils.getFullImageUrl(journal?.imageUrl)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(modelData)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Preview Gambar Jurnal",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(MaterialTheme.shapes.extraLarge),
                        loading = {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = AccentColor)
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.BrokenImage,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text("Gagal memuat / Tidak ada foto", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !isLoading,
                    shape = MaterialTheme.shapes.large,
                    border = BorderStroke(1.5.dp, AccentColor.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = AccentColor)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Ubah Foto", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(24.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(AccentColor)
                    )
                    Text(
                        text = "Konten Jurnal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Cerita") },
                    placeholder = { Text("Contoh: Petualangan Seru di Gunung...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    shape = MaterialTheme.shapes.large,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentColor)
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = story,
                    onValueChange = { story = it },
                    label = { Text("Ceritamu...") },
                    placeholder = { Text("Bagikan pengalaman petualanganmu...") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp),
                    enabled = !isLoading,
                    shape = MaterialTheme.shapes.large,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentColor)
                )

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                        journal?.let { j ->
                            viewModel.updateJournal(
                                id = j.id,
                                title = title,
                                story = story,
                                imageUri = imageUri,
                                existingImageUrl = j.imageUrl
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor, contentColor = DeepBlue),
                    enabled = title.isNotBlank() && story.isNotBlank() && !isLoading,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DeepBlue, strokeWidth = 2.5.dp)
                    } else {
                        Text("Simpan Perubahan", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}