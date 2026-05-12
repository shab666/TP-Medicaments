package com.tonysely.medicamentapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tonysely.medicamentapp.model.Medicament
import com.tonysely.medicamentapp.viewmodel.MedicamentUiState
import com.tonysely.medicamentapp.viewmodel.MedicamentViewModel
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicamentScreen(
    viewModel: MedicamentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    var medicamentToDelete by remember { mutableStateOf<Int?>(null) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            "PharmaCatalog",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            "Gestion Pro de votre Stock",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            ) {
                ExtendedFloatingActionButton(
                    onClick = { showForm = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Default.Add, "Ajouter") },
                    text = { Text("Nouveau Médicament") }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            when (val state = uiState) {
                is MedicamentUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is MedicamentUiState.Success -> {
                    if (state.medicaments.isEmpty()) {
                        EmptyState()
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.medicaments) { medicament ->
                                MedicamentGridItem(
                                    medicament = medicament,
                                    onDelete = { medicamentToDelete = medicament.id }
                                )
                            }
                        }
                    }
                }
                is MedicamentUiState.Error -> {
                    ErrorState(message = state.message, onRetry = { viewModel.charger() })
                }
            }
        }

        if (showForm) {
            AddMedicamentDialog(
                onDismiss = { showForm = false },
                onConfirm = { nouveau ->
                    viewModel.ajouter(nouveau)
                    showForm = false
                }
            )
        }

        if (medicamentToDelete != null) {
            DeleteConfirmationDialog(
                onDismiss = { medicamentToDelete = null },
                onConfirm = {
                    medicamentToDelete?.let { viewModel.supprimer(it) }
                    medicamentToDelete = null
                }
            )
        }
    }
}

@Composable
fun MedicamentGridItem(medicament: Medicament, onDelete: () -> Unit) {
    val displayImageUrl = remember(medicament.nom, medicament.image_url) {
        if (medicament.image_url.isNotEmpty()) {
            medicament.image_url
        } else {
            val name = medicament.nom.lowercase()
            when {
                name.contains("sirop") -> "https://images.unsplash.com/photo-1587854692152-cbe660dbbb88?w=500&q=80"
                name.contains("gelule") || name.contains("capsule") || name.contains("comprim") -> "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=500&q=80"
                name.contains("pommade") || name.contains("creme") -> "https://images.unsplash.com/photo-1555633514-abcee6ad93e1?w=500&q=80"
                name.contains("pansement") || name.contains("soin") -> "https://images.unsplash.com/photo-1576091160550-2173dba999ef?w=500&q=80"
                else -> "https://images.unsplash.com/photo-1585435557343-3b092031a831?w=500&q=80"
            }
        }
    }

    // Logique d'alerte expiration
    val expirationStatus = remember(medicament.date_expiration) {
        getExpirationStatus(medicament.date_expiration)
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box {
            Column {
                AsyncImage(
                    model = displayImageUrl,
                    contentDescription = medicament.nom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = medicament.nom,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = medicament.dosage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Badge Expiration Dynamique
                    val (badgeColor, badgeText, icon) = when (expirationStatus) {
                        ExpirationStatus.EXPIRED -> Triple(Color.Red, "EXPIRÉ", Icons.Default.Warning)
                        ExpirationStatus.SOON -> Triple(Color(0xFFFFA500), "Expire bientôt", Icons.Default.Timer)
                        ExpirationStatus.OK -> Triple(Color(0xFF4CAF50), medicament.date_expiration, Icons.Default.Event)
                    }

                    Surface(
                        color = badgeColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = badgeColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

enum class ExpirationStatus { OK, SOON, EXPIRED }

fun getExpirationStatus(dateStr: String): ExpirationStatus {
    return try {
        val expirationDate = LocalDate.parse(dateStr)
        val today = LocalDate.now()
        val daysUntil = ChronoUnit.DAYS.between(today, expirationDate)
        
        when {
            daysUntil < 0 -> ExpirationStatus.EXPIRED
            daysUntil <= 30 -> ExpirationStatus.SOON
            else -> ExpirationStatus.OK
        }
    } catch (e: DateTimeParseException) {
        ExpirationStatus.OK
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.CloudOff, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) { Text("Réessayer") }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Inventory2,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Aucun médicament", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun DeleteConfirmationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red) },
        title = { Text("Confirmation") },
        text = { Text("Voulez-vous vraiment supprimer ce médicament du catalogue ?") },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                Text("Supprimer", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicamentDialog(onDismiss: () -> Unit, onConfirm: (Medicament) -> Unit) {
    var nom by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var forme by remember { mutableStateOf("") }
    var fabricant by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau Produit", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = nom, onValueChange = { nom = it }, label = { Text("Nom (ex: Doliprane)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = dosage, onValueChange = { dosage = it }, label = { Text("Dosage") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = forme, onValueChange = { forme = it }, label = { Text("Forme") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                }
                OutlinedTextField(value = fabricant, onValueChange = { fabricant = it }, label = { Text("Fabricant") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date expiration (AAAA-MM-JJ)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Lien image (optionnel)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(Medicament(0, nom, dosage, forme, fabricant, date, imageUrl)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Ajouter au catalogue") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Annuler") }
        }
    )
}
