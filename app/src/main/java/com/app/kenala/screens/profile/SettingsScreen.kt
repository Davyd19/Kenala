package com.app.kenala.screens.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.kenala.ui.theme.*
import com.app.kenala.viewmodel.AuthViewModel
import com.app.kenala.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    // 1. State dari SettingsViewModel (DataStore)
    // Mengambil nilai pengaturan yang tersimpan
    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsState()
    val locationEnabled by settingsViewModel.locationEnabled.collectAsState()
    val darkModeEnabled by settingsViewModel.darkModeEnabled.collectAsState()

    // 2. State Lokal untuk Dialog & UI
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Fungsi helper untuk membuka URL di browser
    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar("Tidak dapat membuka browser")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Pengaturan",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Scrollable Content Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 25.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- SECTION 1: PREFERENSI ---
                SettingsSection(title = "Preferensi") {
                    SettingsSwitchItem(
                        title = "Notifikasi",
                        description = "Terima notifikasi misi baru",
                        checked = notificationsEnabled,
                        onCheckedChange = { settingsViewModel.toggleNotifications(it) },
                        icon = Icons.Default.Notifications
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = BorderColor)

                    SettingsSwitchItem(
                        title = "Lokasi",
                        description = "Izinkan akses lokasi untuk misi",
                        checked = locationEnabled,
                        onCheckedChange = { settingsViewModel.toggleLocation(it) },
                        icon = Icons.Default.LocationOn
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = BorderColor)

                    SettingsSwitchItem(
                        title = "Mode Gelap",
                        description = "Tampilan dengan tema gelap",
                        checked = darkModeEnabled,
                        onCheckedChange = { settingsViewModel.toggleDarkMode(it) },
                        icon = Icons.Default.DarkMode
                    )
                }

                // --- SECTION 2: AKUN ---
                SettingsSection(title = "Akun") {
                    SettingsActionItem(
                        title = "Ganti Password",
                        description = "Perbarui kata sandi Anda",
                        icon = Icons.Default.Lock,
                        onClick = { showPasswordDialog = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = BorderColor)

                    SettingsActionItem(
                        title = "Bahasa",
                        description = "Indonesia",
                        icon = Icons.Default.Language,
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Saat ini hanya Bahasa Indonesia yang tersedia")
                            }
                        }
                    )
                }

                // --- SECTION 3: TENTANG ---
                SettingsSection(title = "Tentang") {
                    SettingsActionItem(
                        title = "Versi Aplikasi",
                        description = "1.0.0 (Beta)",
                        icon = Icons.Default.Info,
                        onClick = { } // Tidak ada aksi, hanya info
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = BorderColor)

                    SettingsActionItem(
                        title = "Kebijakan Privasi",
                        description = "Baca kebijakan privasi kami",
                        icon = Icons.Default.PrivacyTip,
                        onClick = { openUrl("https://www.google.com") } // Ganti URL sesuai kebutuhan
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = BorderColor)

                    SettingsActionItem(
                        title = "Syarat & Ketentuan",
                        description = "Baca syarat dan ketentuan",
                        icon = Icons.Default.Description,
                        onClick = { openUrl("https://www.google.com") } // Ganti URL sesuai kebutuhan
                    )
                }
            }

            // Fixed Logout Button at Bottom
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp) // Sedikit lebih tinggi untuk area sentuh yang nyaman
                        .padding(horizontal = 25.dp, vertical = 12.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorColor
                    )
                ) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Keluar",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // --- DIALOGS ---

    // 1. Dialog Ganti Password
    if (showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onSubmit = { oldPass, newPass ->
                authViewModel.changePassword(
                    currentPass = oldPass,
                    newPass = newPass,
                    onSuccess = {
                        showPasswordDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Password berhasil diubah!")
                        }
                    },
                    onError = { msg ->
                        scope.launch {
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                )
            }
        )
    }

    // 2. Dialog Konfirmasi Logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Default.Logout, contentDescription = null, tint = ErrorColor) },
            title = { Text("Keluar dari Akun?") },
            text = { Text("Apakah Anda yakin ingin keluar? Anda perlu login kembali untuk mengakses aplikasi.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorColor
                    )
                ) {
                    Text("Ya, Keluar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

// --- HELPER COMPONENTS ---

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ganti Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = oldPass,
                    onValueChange = { oldPass = it },
                    label = { Text("Password Lama") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = { Text("Password Baru") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        if (newPass.isNotEmpty() && newPass.length < 6) {
                            Text("Minimal 6 karakter")
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (oldPass.isNotEmpty() && newPass.length >= 6) {
                        isLoading = true
                        onSubmit(oldPass, newPass)
                    }
                },
                enabled = !isLoading && oldPass.isNotEmpty() && newPass.length >= 6
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Simpan")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        if (checked) AccentColor.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (checked) AccentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AccentColor,
                checkedTrackColor = AccentColor.copy(alpha = 0.5f),
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsActionItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}