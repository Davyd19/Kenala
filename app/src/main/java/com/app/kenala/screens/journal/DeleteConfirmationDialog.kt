package com.app.kenala.screens.journal

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Hapus Jurnal",
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = "Konfirmasi Hapus",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = "Apakah Anda yakin ingin menghapus jurnal ini secara permanen? Tindakan ini tidak dapat diurungkan.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    "Hapus",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    "Batal",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}