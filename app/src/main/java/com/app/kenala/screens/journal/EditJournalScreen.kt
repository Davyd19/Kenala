package com.app.kenala.screens.journal

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.kenala.ui.theme.LightTextColor
import com.app.kenala.ui.theme.PrimaryBlue
import com.app.kenala.ui.theme.WhiteColor
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
    // Get journals from ViewModel
    val journals by viewModel.journals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Find the journal by ID
    val journal = remember(journals, journalId) {
        journals.find { it.id == journalId }
    }

    var title by remember { mutableStateOf("") }
    var story by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // --- 1. TAMBAHAN BARU: State untuk Image Picker ---
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }
    // ---------------------------------------------

    // Initialize fields when journal is loaded
    LaunchedEffect(journal) {
        journal?.let {
            title = it.title
            story = it.story
            // Jangan set imageUri di sini, biarkan null
            // Kita akan gunakan journal.imageUrl untuk preview
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Jurnal") },
                navigationIcon = {
                    IconButton(onClick = onBackClick, enabled = !isLoading) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
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

        // Dialog konfirmasi hapus
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

        // Show loading or content
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (journal == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
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
                    .verticalScroll(rememberScrollState()), // Tambahkan scroll
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- 2. TAMBAHAN BARU: Preview Gambar ---
                val displayImage: Any? = imageUri ?: journal.imageUrl

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(displayImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Preview Gambar Jurnal",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.large)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") }, // Panggil image picker
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text("Ubah Foto")
                }
                // ----------------------------------------

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Cerita") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = LightTextColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = story,
                    onValueChange = { story = it },
                    label = { Text("Ceritamu...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp), // Beri tinggi minimal
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = LightTextColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // --- 3. PERBAIKAN: Panggil fungsi ViewModel yang benar ---
                        viewModel.updateJournal(
                            id = journal.id,
                            title = title,
                            story = story,
                            imageUri = imageUri, // (Error 2) Kirim URI baru jika ada
                            existingImageUrl = journal.imageUrl // (Error 3) Kirim URL lama
                        )
                        // --------------------------------------------------
                        onSaveClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = MaterialTheme.shapes.large,
                    enabled = title.isNotBlank() && story.isNotBlank() && !isLoading
                ) {
                    Text("Simpan Perubahan", color = WhiteColor)
                }
            }
        }
    }
}