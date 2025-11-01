package mx.edu.utng.arg.recicladoloresfirst.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import mx.edu.utng.arg.recicladoloresfirst.data.model.Entrega
import mx.edu.utng.arg.recicladoloresfirst.data.model.EstadoEntrega
import mx.edu.utng.arg.recicladoloresfirst.ui.components.*
import mx.edu.utng.arg.recicladoloresfirst.util.Resource
import mx.edu.utng.arg.recicladoloresfirst.util.formatPoints
import mx.edu.utng.arg.recicladoloresfirst.util.toRelativeTime
import mx.edu.utng.reciclaDH.ui.components.ErrorMessage
import kotlin.text.get

/**
 * Pantalla principal de la app.
 *
 * Muestra:
 * - Puntos del usuario
 * - Estadísticas rápidas
 * - Últimas entregas
 * - Accesos rápidos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPerfil: () -> Unit,
    onNavigateToEntregas: () -> Unit,
    onNavigateToRecompensas: () -> Unit,
    onNavigateToNuevaEntrega: () -> Unit,
    onNavigateToHistorial: () -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val usuarioState by viewModel.usuario.collectAsState()
    val entregasState by viewModel.ultimasEntregas.collectAsState()
    val estadisticasState by viewModel.estadisticas.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ReciclaD Hidalgo") },
                actions = {
                    IconButton(onClick = onNavigateToPerfil) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil")
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToNuevaEntrega,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nueva Entrega") }
            )
        }
    ) { paddingValues ->

        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = {
                isRefreshing = true
                viewModel.recargar()
                isRefreshing = false
            },
            modifier = Modifier.padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tarjeta de puntos
                item {
                    TarjetaPuntos(usuarioState)
                }

                // Estadísticas rápidas
                item {
                    EstadisticasRapidas(estadisticasState)
                }

                // Accesos rápidos
                item {
                    AccesosRapidos(
                        onNavigateToRecompensas = onNavigateToRecompensas,
                        onNavigateToHistorial = onNavigateToHistorial,
                        onNavigateToEntregas = onNavigateToEntregas
                    )
                }

                // Últimas entregas
                item {
                    Text(
                        text = "Últimas Entregas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                when (entregasState) {
                    is Resource.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    is Resource.Success -> {
                        val entregas = (entregasState as Resource.Success).data ?: emptyList()
                        if (entregas.isEmpty()) {
                            item {
                                EmptyStateMessage(
                                    message = "No tienes entregas aún",
                                    actionText = "Crear primera entrega",
                                    onAction = onNavigateToNuevaEntrega
                                )
                            }
                        } else {
                            items(entregas) { entrega ->
                                TarjetaEntrega(entrega)
                            }
                        }
                    }
                    is Resource.Error -> {
                        item {
                            ErrorMessage(
                                message = (entregasState as Resource.Error).message
                                    ?: "Error al cargar entregas",
                                onRetry = { viewModel.recargar() }
                            )
                        }
                    }
                    null -> {}
                }
            }
        }
    }

    // Diálogo de confirmación de cierre de sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Estás seguro que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cerrarSesion()
                        onLogout()
                    }
                ) {
                    Text("Sí, cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Tarjeta que muestra los puntos del usuario.
 */
@Composable
private fun TarjetaPuntos(usuarioState: Resource<mx.edu.utng.arg.recicladoloresfirst.data.model.Usuario>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (usuarioState) {
                is Resource.Loading -> {
                    CircularProgressIndicator()
                }
                is Resource.Success -> {
                    val usuario = usuarioState.data
                    Text(
                        text = "¡Hola, ${usuario?.nombre?.split(" ")?.first() ?: "Usuario"}!",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = (usuario?.puntos ?: 0).formatPoints(),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Puntos acumulados",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                is Resource.Error -> {
                    Text(
                        text = "Error al cargar puntos",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                null -> {}
            }
        }
    }
}

/**
 * Estadísticas rápidas en tarjetas pequeñas.
 */
@Composable
private fun EstadisticasRapidas(
    estadisticasState: Resource<Map<String, Int>>
) {
    when (estadisticasState) {
        is Resource.Success -> {
            val stats = estadisticasState.data ?: emptyMap()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total entregas
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    valor = stats["totalEntregas"] ?: 0,
                    titulo = "Entregas",
                    icono = Icons.Default.Recycling
                )

                // Aprobadas
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    valor = stats["aprobadas"] ?: 0,
                    titulo = "Aprobadas",
                    icono = Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.tertiary
                )

                // Pendientes
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    valor = stats["pendientes"] ?: 0,
                    titulo = "Pendientes",
                    icono = Icons.Default.HourglassEmpty
                )
            }
        }
        else -> {}
    }
}

/**
 * Mini tarjeta de estadística.
 */
@Composable
private fun MiniStatCard(
    valor: Int,
    titulo: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondary
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = color
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = valor.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = titulo,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * Accesos rápidos a secciones principales.
 */
@Composable
private fun AccesosRapidos(
    onNavigateToRecompensas: () -> Unit,
    onNavigateToHistorial: () -> Unit,
    onNavigateToEntregas: () -> Unit
) {
    Column {
        Text(
            text = "Accesos Rápidos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccesoRapidoCard(
                modifier = Modifier.weight(1f),
                titulo = "Recompensas",
                icono = Icons.Default.CardGiftcard,
                onClick = onNavigateToRecompensas
            )

            AccesoRapidoCard(
                modifier = Modifier.weight(1f),
                titulo = "Historial",
                icono = Icons.Default.History,
                onClick = onNavigateToHistorial
            )

            AccesoRapidoCard(
                modifier = Modifier.weight(1f),
                titulo = "Mis Entregas",
                icono = Icons.Default.List,
                onClick = onNavigateToEntregas
            )
        }
    }
}

/**
 * Tarjeta de acceso rápido.
 */
@Composable
private fun AccesoRapidoCard(
    titulo: String,
    icono: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Tarjeta que muestra una entrega.
 */
@Composable
private fun TarjetaEntrega(entrega: Entrega) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono del material
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = entrega.tipoMaterial.getIcono(),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entrega.tipoMaterial.display,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${entrega.pesoKg} kg • ${entrega.puntosGenerados} pts",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = entrega.fechaEntrega?.toRelativeTime() ?: "Ahora",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Badge de estado
            Surface(
                shape = MaterialTheme.shapes.small,
                color = entrega.estado.getColor().copy(alpha = 0.2f)
            ) {
                Text(
                    text = when (entrega.estado) {
                        EstadoEntrega.PENDIENTE -> "Pendiente"
                        EstadoEntrega.APROBADA -> "Aprobada"
                        EstadoEntrega.RECHAZADA -> "Rechazada"
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = entrega.estado.getColor()
                )
            }
        }
    }
}

/**
 * Mensaje cuando no hay datos.
 */
@Composable
private fun EmptyStateMessage(
    message: String,
    actionText: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Recycling,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onAction) {
                Text(actionText)
            }
        }
    }
}
