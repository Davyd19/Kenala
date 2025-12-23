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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.kenala.ui.theme.*
import com.app.kenala.viewmodel.JournalViewModel
import com.app.kenala.viewmodel.MissionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEntryScreen(
    realDistance: Double = 0.0,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    missionViewModel: MissionViewModel = viewModel(),
    journalViewModel: JournalViewModel = viewModel()
) {
    val selectedMission by missionViewModel.selectedMission.collectAsState()
    var journalTitle by remember { mutableStateOf("") }
    var journalStory by remember { mutableStateOf("") }
    val isSaving by journalViewModel.isLoading.collectAsState()
    val journalSaved by journalViewModel.journalSaved.collectAsState()

    var isMissionCompletedOnServer by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    LaunchedEffect(selectedMission) {
        selectedMission?.let { mission ->
            if (journalTitle.isEmpty()) {
                journalTitle = "Petualangan di ${mission.name}"
            }
        }
    }

    LaunchedEffect(journalSaved) {
        if (journalSaved) {
            journalViewModel.resetJournalSaved()
            onSaveClick()
        }
    }

    fun ensureMissionCompleted(onSuccess: () -> Unit) {
        if (isMissionCompletedOnServer) {
            onSuccess()
            return
        }

        selectedMission?.let { mission ->
            missionViewModel.completeMission(mission.id, realDistance) {
                isMissionCompletedOnServer = true
                onSuccess()
            }
        } ?: onSuccess()
    }

    fun handleSave() {
        if (journalTitle.isBlank() || journalStory.isBlank() || isSaving) {
            return
        }

        ensureMissionCompleted {
            journalViewModel.createJournal(
                title = journalTitle,
                story = journalStory,
                imageUri = imageUri,
                locationName = selectedMission?.location_name,
                latitude = selectedMission?.latitude,
                longitude = selectedMission?.longitude
            )
        }
    }

    fun handleFinishWithoutJournal() {
        ensureMissionCompleted {
            onSaveClick()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Selesaikan Misi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { handleFinishWithoutJournal() }, enabled = !isSaving) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
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
                .verticalScroll(rememberScrollState())
        ) {
            selectedMission?.let { mission ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = AccentColor.copy(alpha = 0.12f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(AccentColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Place,
                                    contentDescription = null,
                                    tint = AccentColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Misi: ${mission.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = mission.location_name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        if (realDistance > 0) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(
                                color = AccentColor.copy(alpha = 0.2f),
                                thickness = 1.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = AccentColor.copy(alpha = 0.08f),
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Jarak ditempuh: ${"%.2f".format(realDistance / 1000)} km",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 24.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(AccentColor)
                )
                Text(
                    text = "Abadikan Momen",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "(Opsional)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )
            }

            OutlinedTextField(
                value = journalTitle,
                onValueChange = { journalTitle = it },
                label = { Text("Judul Cerita") },
                placeholder = { Text("Contoh: Petualangan Seru di Gunung...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSaving,
                shape = MaterialTheme.shapes.large,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = journalStory,
                onValueChange = { journalStory = it },
                label = { Text("Ceritamu di sini...") },
                placeholder = { Text("Bagikan pengalaman petualanganmu...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp),
                enabled = !isSaving,
                shape = MaterialTheme.shapes.large,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isSaving,
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(
                    1.5.dp,
                    if (imageUri == null) AccentColor.copy(alpha = 0.5f) else AccentColor
                )
            ) {
                Icon(
                    Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = if (imageUri == null) AccentColor else AccentColor
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    if (imageUri == null) "Tambah Foto" else "Ganti Foto",
                    fontWeight = FontWeight.SemiBold
                )
            }

            imageUri?.let {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(it)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Preview Gambar Jurnal",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(MaterialTheme.shapes.extraLarge)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = { handleSave() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentColor,
                    contentColor = DeepBlue
                ),
                enabled = !isSaving && journalTitle.isNotBlank() && journalStory.isNotBlank(),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 2.dp
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = DeepBlue,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        "Simpan Jurnal & Selesai",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = { handleFinishWithoutJournal() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.extraLarge,
                enabled = !isSaving,
                border = BorderStroke(
                    1.5.dp,
                    MaterialTheme.colorScheme.outline
                )
            ) {
                Text(
                    "Selesai Tanpa Menulis Jurnal",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}