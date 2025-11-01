package mx.edu.utng.arg.recicladoloresfirst.ui.screens.recompensas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import mx.edu.utng.arg.recicladoloresfirst.data.model.Recompensa
import mx.edu.utng.arg.recicladoloresfirst.ui.components.*
import mx.edu.utng.arg.recicladoloresfirst.util.Resource
import mx.edu.utng.arg.recicladoloresfirst.util.formatPoints
import mx.edu.utng.arg.recicladoloresfirst.util.toMoney
import mx.edu.utng.reciclaDH.ui.components.ErrorScreen

/**
 * Pantalla que muestra las recompensas disponibles para canje.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecompensasScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetalle: (String) -> Unit,
    viewModel: RecompensasViewModel = hiltViewModel()
) {
    val recompensasState by viewModel.recompensas.collectAsState()
    val puntosUsuario by viewModel.puntosUsuario.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recompensas Disponibles") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tarjeta de puntos disponibles
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Tus puntos disponibles",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = puntosUsuario.formatPoints(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Lista de recompensas
            when (recompensasState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is Resource.Success -> {
                    val recompensas = (recompensasState as Resource.Success).data ?: emptyList()

                    if (recompensas.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CardGiftcard,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "No hay recompensas disponibles",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(items= recompensas) { recompensa ->
                                TarjetaRecompensa(
                                    recompensa = recompensa,
                                    puntosUsuario = puntosUsuario,
                                    onClick = { onNavigateToDetalle(recompensa.id) }
                                )
                            }
                        }
                    }
                }

                is Resource.Error -> {
                    ErrorScreen(
                        message = (recompensasState as Resource.Error).message
                            ?: "Error al cargar recompensas",
                        onRetry = { viewModel.recargar() }
                    )
                }

                null -> {}
            }
        }
    }
}

/**
 * Tarjeta de recompensa individual.
 */
@Composable
private fun TarjetaRecompensa(
    recompensa: Recompensa,
    puntosUsuario: Int,
    onClick: () -> Unit
) {
    val puedeCanCanjear = puntosUsuario >= recompensa.costoEnPuntos

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Imagen de la recompensa
            if (recompensa.imagenUrl != null) {
                AsyncImage(
                    model = recompensa.imagenUrl,
                    contentDescription = recompensa.titulo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder si no hay imagen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = recompensa.categoria.icono,
                        style = MaterialTheme.typography.displayLarge
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Categoría
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "${recompensa.categoria.icono} ${recompensa.categoria.display}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Título
                Text(
                    text = recompensa.titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Descripción
                Text(
                    text = recompensa.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Valor y costo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Valor: ${recompensa.valorMonetario.toMoney()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = recompensa.costoEnPuntos.formatPoints(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (puedeCanCanjear) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }

                    // Badge de disponibilidad
                    if (recompensa.cantidadDisponible > 0) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = if (recompensa.cantidadDisponible <= 5) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.tertiaryContainer
                            }
                        ) {
                            Text(
                                text = "Quedan ${recompensa.cantidadDisponible}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                // Indicador si no tiene suficientes puntos
                if (!puedeCanCanjear) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "Te faltan ${(recompensa.costoEnPuntos - puntosUsuario).formatPoints()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}