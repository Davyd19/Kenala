package com.app.kenala.screens.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.kenala.data.journalList
import com.app.kenala.ui.theme.LightTextColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalDetailScreen(
    journalId: Int,
    onBackClick: () -> Unit,
    onEditClick: (Int) -> Unit
) {
    val journal = journalList.find { it.id == journalId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Petualangan") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick(journalId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Jurnal")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        // Menggunakan Column yang bisa di-scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()) // <-- KUNCI PERBAIKAN
        ) {
            if (journal != null) {
                // Gambar Jurnal
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(journal.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = journal.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )

                // Konten Teks (Judul, Tanggal, Cerita)
                Column(
                    modifier = Modifier.padding(25.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = journal.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = journal.date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LightTextColor
                    )
                    Text(
                        text = journal.story,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            } else {
                Text(
                    text = "Jurnal tidak ditemukan.",
                    modifier = Modifier.padding(25.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

