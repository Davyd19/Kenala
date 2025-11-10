package com.app.kenala.screens.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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

    // Find the journal by ID (convert Int to String if needed)
    val journal = remember(journals, journalId) {
        journals.find { it.id == journalId.toString() }
    }

    var title by remember { mutableStateOf("") }
    var story by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Initialize fields when journal is loaded
    LaunchedEffect(journal) {
        journal?.let {
            title = it.title
            story = it.story
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Jurnal") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
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
                    .padding(horizontal = 25.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Judul Cerita") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = LightTextColor
                        )
                    )
                    OutlinedTextField(
                        value = story,
                        onValueChange = { story = it },
                        label = { Text("Ceritamu...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = LightTextColor
                        )
                    )
                }
                Button(
                    onClick = {
                        viewModel.updateJournal(
                            id = journal.id,
                            title = title,
                            story = story,
                            imageUrl = journal.imageUrl
                        )
                        onSaveClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = MaterialTheme.shapes.large,
                    enabled = title.isNotBlank() && story.isNotBlank()
                ) {
                    Text("Simpan Perubahan", color = WhiteColor)
                }
            }
        }
    }
}