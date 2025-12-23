package com.app.kenala.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.kenala.ui.theme.*
import com.app.kenala.viewmodel.ProfileViewModel
import com.app.kenala.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var name by remember(user) { mutableStateOf(user?.name ?: "") }
    var email by remember(user) { mutableStateOf(user?.email ?: "") }
    var phone by remember(user) { mutableStateOf(user?.phone ?: "") }
    var bio by remember(user) { mutableStateOf(user?.bio ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    val isChanged = (name != (user?.name ?: "")) ||
            (phone != (user?.phone ?: "")) ||
            (bio != (user?.bio ?: "")) ||
            (imageUri != null)

    var nameError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        if (name.isBlank()) {
            nameError = "Nama tidak boleh kosong"
            return false
        }
        nameError = null
        return true
    }

    val canSave = !isLoading && isChanged && name.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Profil",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, enabled = !isLoading) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 25.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        val displayImage: Any? = imageUri ?: user?.profile_image_url

                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(displayImage)
                                .placeholder(R.drawable.logo_kenala1)
                                .error(R.drawable.logo_kenala1)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar Profil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(OceanBlue, DeepBlue)
                                    )
                                )
                                .clickable(enabled = !isLoading) {
                                    imagePickerLauncher.launch("image/*")
                                }
                        )

                        FilledIconButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.size(36.dp),
                            enabled = !isLoading,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = AccentColor,
                                contentColor = DeepBlue
                            )
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Ubah Foto",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ubah Foto Profil",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    LaunchedEffect(error) {
                        kotlinx.coroutines.delay(3000)
                        viewModel.clearError()
                    }
                }

                ProfileTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = "Nama Lengkap",
                    icon = Icons.Default.Person,
                    isError = nameError != null,
                    supportingText = nameError,
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(16.dp))

                ProfileTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    icon = Icons.Default.Email,
                    enabled = false
                )
                Spacer(modifier = Modifier.height(16.dp))

                ProfileTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Nomor Telepon",
                    icon = Icons.Default.Phone,
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(16.dp))

                ProfileTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = "Bio",
                    icon = Icons.Default.Description,
                    singleLine = false,
                    maxLines = 3,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (validate()) {
                            viewModel.updateProfile(
                                name = name,
                                phone = phone.ifBlank { null },
                                bio = bio.ifBlank { null },
                                imageUri = imageUri,
                                existingImageUrl = user?.profile_image_url,
                                onSuccess = {
                                    onNavigateBack()
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    enabled = canSave,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor,
                        contentColor = DeepBlue
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = DeepBlue
                        )
                    } else {
                        Text(
                            "Simpan Perubahan",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

            }
        }
    }
}

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    isError: Boolean = false,
    supportingText: String? = null,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        maxLines = maxLines,
        isError = isError,
        supportingText = {
            if (supportingText != null) {
                Text(text = supportingText)
            }
        },
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentColor,
            focusedLabelColor = AccentColor,
            cursorColor = AccentColor
        )
    )
}