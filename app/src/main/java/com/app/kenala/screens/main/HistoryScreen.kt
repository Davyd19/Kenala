package com.app.kenala.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.kenala.ui.theme.LightTextColor

// Data dummy yang lebih kaya untuk riwayat jurnal
private data class JournalHistory(
    val id: Int,
    val title: String,
    val date: String,
    val snippet: String,
    val imageUrl: String
)

private val journalList = listOf(
    JournalHistory(
        id = 1,
        title = "Secangkir Ketenangan di Kopi Seroja",
        date = "15 Okt 2025",
        snippet = "Menemukan kedai kopi tersembunyi ini adalah sebuah anugerah. Suasananya tenang, kopinya nikmat, dan aku bisa membaca buku selama berjam-jam tanpa gangguan...",
        imageUrl = "https://images.pexels.com/photos/312418/pexels-photo-312418.jpeg"
    ),
    JournalHistory(
        id = 2,
        title = "Pameran Seni Kontemporer 'Ruang Hening'",
        date = "11 Okt 2025",
        snippet = "Tidak menyangka ada galeri seni sekecil ini di tengah kota. Lukisan-lukisannya sangat menyentuh dan membuatku berpikir tentang banyak hal.",
        imageUrl = "https://images.pexels.com/photos/1025804/pexels-photo-1025804.jpeg"
    ),
    JournalHistory(
        id = 3,
        title = "Menyatu dengan Alam di Hutan Kota",
        date = "05 Okt 2025",
        snippet = "Udara segar dan suara alam di sini benar-benar menyegarkan. Tempat yang sempurna untuk lari pagi atau sekadar berjalan-jalan santai di akhir pekan.",
        imageUrl = "https://images.pexels.com/photos/167699/pexels-photo-167699.jpeg"
    )
)

/**
 * Layar History (Riwayat Jurnal)
 * Menampilkan daftar semua petualangan yang pernah dijalani.
 */
@Composable
fun HistoryScreen(
    onJournalClick: (String) -> Unit,
    viewModel: JournalViewModel = viewModel() // Inject ViewModel
) {
    val journals by viewModel.journals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(all = 25.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Riwayat Petualangan",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        // Refresh button
                        IconButton(onClick = { viewModel.syncJournals() }) {
                            Icon(Icons.Default.Refresh, "Refresh")
                        }
                    }
                }

                if (journals.isEmpty() && !isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Belum ada jurnal")
                        }
                    }
                }

                items(journals) { journal ->
                    JournalCard(
                        journal = journal,
                        onClick = { onJournalClick(journal.id) }
                    )
                }
            }

            // Loading indicator
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Error snackbar
            error?.let { errorMessage ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(errorMessage)
                }
            }
        }
    }
}

/**
 * Composable pribadi untuk satu kartu jurnal dalam daftar.
 */
@Composable
private fun JournalCard(journal: JournalHistory, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large, // 16dp
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Putih
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
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
                    .height(150.dp)
            )

            // Konten Teks (Judul, Tanggal, Snippet)
            Column(modifier = Modifier.padding(15.dp)) {
                Text(
                    text = journal.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = journal.date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightTextColor,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
                Text(
                    text = journal.snippet,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

