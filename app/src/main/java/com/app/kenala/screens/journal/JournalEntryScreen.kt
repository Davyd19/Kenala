package com.app.kenala.screens.journal

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.kenala.ui.theme.AccentColor
import com.app.kenala.ui.theme.BrightBlue
import com.app.kenala.ui.theme.ForestGreen
import com.app.kenala.ui.theme.PrimaryBlue
import com.app.kenala.ui.theme.WhiteColor
import com.app.kenala.viewmodel.JournalViewModel
import com.app.kenala.viewmodel.MissionViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.kenala.ui.theme.*
import androidx.compose.ui.text.input.ImeAction

/**
 * Layar untuk menulis entri jurnal baru setelah menyelesaikan misi.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEntryScreen(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    missionViewModel: MissionViewModel = viewModel(),
    journalViewModel: JournalViewModel = viewModel()
) {
    val selectedMission by missionViewModel.selectedMission.collectAsState()
    var journalTitle by remember { mutableStateOf("") }
    var journalStory by remember { mutableStateOf("") }
    val isSaving by journalViewModel.isLoading.collectAsState() // Gunakan state dari ViewModel

    // --- TAMBAHAN BARU: State untuk Image Picker ---
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }
    // ---------------------------------------------

    // Auto-fill title dengan nama misi
    LaunchedEffect(selectedMission) {
        selectedMission?.let { mission ->
            if (journalTitle.isEmpty()) {
                journalTitle = "Petualangan di ${mission.name}"
            }
        }
    }

    fun handleSave() {
        if (journalTitle.isBlank() || journalStory.isBlank() || isSaving) {
            return
        }

        selectedMission?.let { mission ->
            // Selesaikan misi
            missionViewModel.completeMission(mission.id) {
                // Buat jurnal dengan imageUri
                journalViewModel.createJournal(
                    title = journalTitle,
                    story = journalStory,
                    imageUri = imageUri, // Kirim Uri
                    locationName = mission.location_name,
                    latitude = mission.latitude,
                    longitude = mission.longitude
                )
                onSaveClick()
            }
        } ?: run {
            // Buat jurnal tanpa misi
            journalViewModel.createJournal(
                title = journalTitle,
                story = journalStory,
                imageUri = imageUri // Kirim Uri
            )
            onSaveClick()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tulis Jurnal Petualanganmu",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, enabled = !isSaving) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 25.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()) // Tambahkan scroll
        ) {
            // Mission info card (if available)
            selectedMission?.let { mission ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = AccentColor.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            tint = AccentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Misi: ${mission.name}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentColor
                            )
                            Text(
                                text = mission.location_name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ForestGreen
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Input Judul
            OutlinedTextField(
                value = journalTitle,
                onValueChange = { journalTitle = it },
                label = { Text("Judul Cerita") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Input Cerita
            OutlinedTextField(
                value = journalStory,
                onValueChange = { journalStory = it },
                label = { Text("Ceritamu di sini...") },
                placeholder = { Text("Bagikan pengalaman petualanganmu...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp), // Beri tinggi minimal
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
            )
            Spacer(modifier = Modifier.height(20.dp))

            // --- PERUBAHAN: Tombol Tambah Foto & Preview ---
            OutlinedButton(
                onClick = { imagePickerLauncher.launch("image/*") }, // Panggil image picker
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isSaving
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (imageUri == null) "Tambah Foto" else "Ganti Foto")
            }

            // Preview gambar jika sudah dipilih
            imageUri?.let {
                Spacer(modifier = Modifier.height(16.dp))
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(it)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Preview Gambar Jurnal",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.large)
                )
            }
            // -------------------------------------------

            Spacer(modifier = Modifier.height(20.dp))

            // Tombol Simpan
            Button(
                onClick = { handleSave() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 16.dp), // Tambahkan padding bawah
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = WhiteColor
                ),
                enabled = !isSaving && journalTitle.isNotBlank() && journalStory.isNotBlank()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = WhiteColor
                    )
                } else {
                    Text(
                        "Simpan Jurnal",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}