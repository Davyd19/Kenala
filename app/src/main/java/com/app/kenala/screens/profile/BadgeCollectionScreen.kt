package com.app.kenala.screens.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.kenala.data.remote.dto.BadgeDto
import com.app.kenala.ui.theme.*
import com.app.kenala.viewmodel.BadgeViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeCollectionScreen(
    onNavigateBack: () -> Unit,
    badgeViewModel: BadgeViewModel = viewModel()
) {
    val badges by badgeViewModel.badges.collectAsState()
    val isLoading by badgeViewModel.isLoading.collectAsState()
    val error by badgeViewModel.error.collectAsState()
    var selectedBadge by remember { mutableStateOf<BadgeDto?>(null) }

    LaunchedEffect(Unit) {
        badgeViewModel.fetchBadges()
    }

    val unlockedCount = badges.count { it.is_unlocked }
    val totalCount = badges.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Koleksi Badge", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading && badges.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (error != null && badges.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = error!!,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { badgeViewModel.fetchBadges() }) {
                        Text("Coba Lagi")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 25.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$unlockedCount dari $totalCount Badge",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (totalCount > 0) {
                            LinearProgressIndicator(
                                progress = { unlockedCount.toFloat() / totalCount },
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(8.dp),
                                color = AccentColor,
                                trackColor = AccentColor.copy(alpha = 0.2f),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${(unlockedCount.toFloat() / totalCount * 100).toInt()}% Terkumpul",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 25.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(badges) { index, badge ->
                            BadgeCardFromApi(
                                badge = badge,
                                onClick = { selectedBadge = badge },
                                animationDelay = index * 50L
                            )
                        }
                    }
                }
            }
        }
    }

    selectedBadge?.let { badge ->
        BadgeDetailDialogFromApi(
            badge = badge,
            onDismiss = { selectedBadge = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BadgeCardFromApi(
    badge: BadgeDto,
    onClick: () -> Unit,
    animationDelay: Long
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay)
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val alpha = if (badge.is_unlocked) 1f else 0.4f
    val badgeColor = Color(android.graphics.Color.parseColor(badge.color))

    Card(
        onClick = onClick,
        modifier = Modifier
            .aspectRatio(1f)
            .scale(scale)
            .alpha(alpha),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (badge.is_unlocked) {
                badgeColor.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (badge.is_unlocked) 4.dp else 1.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                val icon = when (badge.icon_name) {
                    "hiking" -> Icons.Default.Hiking
                    "location_city" -> Icons.Default.LocationCity
                    "restaurant" -> Icons.Default.Restaurant
                    "palette" -> Icons.Default.Palette
                    "local_fire_department" -> Icons.Default.LocalFireDepartment
                    "group" -> Icons.Default.Group
                    "camera_alt" -> Icons.Default.CameraAlt
                    "directions_run" -> Icons.Default.DirectionsRun
                    "nights_stay" -> Icons.Default.NightsStay
                    "emoji_events" -> Icons.Default.EmojiEvents
                    else -> Icons.Default.EmojiEvents
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            if (badge.is_unlocked) badgeColor.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                            MaterialTheme.shapes.large
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = badge.name,
                        modifier = Modifier.size(40.dp),
                        tint = if (badge.is_unlocked) badgeColor
                        else MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = badge.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (badge.is_unlocked) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = if (badge.is_unlocked) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!badge.is_unlocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Terkunci",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgeDetailDialogFromApi(
    badge: BadgeDto,
    onDismiss: () -> Unit
) {
    val badgeColor = Color(android.graphics.Color.parseColor(badge.color))

    val icon = when (badge.icon_name) {
        "hiking" -> Icons.Default.Hiking
        "location_city" -> Icons.Default.LocationCity
        "restaurant" -> Icons.Default.Restaurant
        "palette" -> Icons.Default.Palette
        "local_fire_department" -> Icons.Default.LocalFireDepartment
        "group" -> Icons.Default.Group
        "camera_alt" -> Icons.Default.CameraAlt
        "directions_run" -> Icons.Default.DirectionsRun
        "nights_stay" -> Icons.Default.NightsStay
        "emoji_events" -> Icons.Default.EmojiEvents
        else -> Icons.Default.EmojiEvents
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        if (badge.is_unlocked) badgeColor.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.shapes.large
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = if (badge.is_unlocked) badgeColor else MaterialTheme.colorScheme.outline
                )
            }
        },
        title = {
            Text(
                text = badge.name,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (badge.is_unlocked && badge.unlocked_at != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = badgeColor.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = badgeColor
                            )
                            Text(
                                text = "Terbuka ${badge.unlocked_at}",
                                style = MaterialTheme.typography.bodySmall,
                                color = badgeColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Belum Terbuka",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        },
        shape = MaterialTheme.shapes.extraLarge
    )
}