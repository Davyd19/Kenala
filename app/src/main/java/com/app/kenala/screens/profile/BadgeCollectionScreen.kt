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
import com.app.kenala.ui.theme.*

private data class Badge(
    val id: Int,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val isUnlocked: Boolean,
    val unlockedDate: String? = null
)

private val badgesList = listOf(
    Badge(1, "Petualang Pemula", "Selesaikan misi pertamamu", Icons.Default.Hiking, ForestGreen, true, "15 Okt 2025"),
    Badge(2, "Penjelajah Kota", "Selesaikan 10 misi", Icons.Default.LocationCity, OceanBlue, true, "20 Okt 2025"),
    Badge(3, "Kuliner Hunter", "Selesaikan 5 misi kuliner", Icons.Default.Restaurant, AccentColor, true, "22 Okt 2025"),
    Badge(4, "Pecinta Seni", "Kunjungi 3 galeri seni", Icons.Default.Palette, SkyBlue, false),
    Badge(5, "Penjaga Streak", "Pertahankan streak 7 hari", Icons.Default.LocalFireDepartment, ErrorColor, true, "24 Okt 2025"),
    Badge(6, "Sosial Butterfly", "Ajak 3 teman bergabung", Icons.Default.Group, ForestGreen, false),
    Badge(7, "Fotografer", "Upload 20 foto petualangan", Icons.Default.CameraAlt, OceanBlue, false),
    Badge(8, "Maraton Petualang", "Tempuh jarak 50km", Icons.Default.DirectionsRun, AccentColor, false),
    Badge(9, "Malam Gemilang", "Selesaikan misi malam hari", Icons.Default.NightsStay, MidnightBlue, false),
    Badge(10, "Master Explorer", "Selesaikan 50 misi", Icons.Default.EmojiEvents, AccentColor, false),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeCollectionScreen(onNavigateBack: () -> Unit) {
    val unlockedCount = badgesList.count { it.isUnlocked }
    val totalCount = badgesList.size
    var selectedBadge by remember { mutableStateOf<Badge?>(null) }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Progress Section
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

            // Badge Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 25.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(badgesList) { index, badge ->
                    BadgeCard(
                        badge = badge,
                        onClick = { selectedBadge = badge },
                        animationDelay = index * 50L
                    )
                }
            }
        }
    }

    // Badge Detail Dialog
    selectedBadge?.let { badge ->
        BadgeDetailDialog(
            badge = badge,
            onDismiss = { selectedBadge = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BadgeCard(
    badge: Badge,
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

    val alpha = if (badge.isUnlocked) 1f else 0.4f

    Card(
        onClick = onClick,
        modifier = Modifier
            .aspectRatio(1f)
            .scale(scale)
            .alpha(alpha),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) {
                badge.color.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (badge.isUnlocked) 4.dp else 1.dp
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
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            if (badge.isUnlocked) badge.color.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                            MaterialTheme.shapes.large
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = badge.icon,
                        contentDescription = badge.name,
                        modifier = Modifier.size(40.dp),
                        tint = if (badge.isUnlocked) badge.color
                        else MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = badge.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (badge.isUnlocked) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!badge.isUnlocked) {
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
private fun BadgeDetailDialog(
    badge: Badge,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        if (badge.isUnlocked) badge.color.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.shapes.large
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = badge.icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = if (badge.isUnlocked) badge.color else MaterialTheme.colorScheme.outline
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
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (badge.isUnlocked && badge.unlockedDate != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = badge.color.copy(alpha = 0.1f),
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
                                tint = badge.color
                            )
                            Text(
                                text = "Terbuka ${badge.unlockedDate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = badge.color,
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