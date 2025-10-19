package com.app.kenala.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.app.kenala.ui.theme.LightBlue
import com.app.kenala.ui.theme.LightTextColor
import com.app.kenala.ui.theme.PrimaryBlue
import com.app.kenala.ui.theme.SuccessColor

private data class NotificationItem(
    val icon: ImageVector,
    val title: String,
    val time: String,
    val isRead: Boolean
)

private val notificationsList = listOf(
    NotificationItem(Icons.Default.CardGiftcard, "Misi baru tersedia di sekitarmu!", "5 menit yang lalu", false),
    NotificationItem(Icons.Default.Edit, "Jangan lupa tulis jurnal petualanganmu di Kopi Seroja.", "2 jam yang lalu", false),
    NotificationItem(Icons.Default.CheckCircle, "Jurnal 'Pameran Seni Kontemporer' berhasil disimpan.", "1 hari yang lalu", true),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsCenterScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifikasi") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notificationsList) { notification ->
                NotificationCard(item = notification)
            }
        }
    }
}

@Composable
private fun NotificationCard(item: NotificationItem) {
    val backgroundColor = if (item.isRead) {
        MaterialTheme.colorScheme.surface
    } else {
        PrimaryBlue.copy(alpha = 0.05f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = if (item.isRead) LightTextColor else PrimaryBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = item.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = LightTextColor
                )
            }
        }
    }
}
