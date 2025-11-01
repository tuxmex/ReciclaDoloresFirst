package mx.edu.utng.arg.recicladoloresfirst.ui.screens.entrega

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import mx.edu.utng.arg.recicladoloresfirst.data.model.TipoMaterial
import mx.edu.utng.arg.recicladoloresfirst.ui.components.*
import mx.edu.utng.arg.recicladoloresfirst.util.Resource
import mx.edu.utng.arg.recicladoloresfirst.util.formatPoints
import kotlin.compareTo

/**
 * Pantalla para registrar una nueva entrega de material reciclable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntregaScreen(
    onNavigateBack: () -> Unit,
    viewModel: EntregaViewModel = hiltViewModel()
) {
    val tipoMaterial by viewModel.tipoMaterial.collectAsState()
    val pesoText by viewModel.pesoText.collectAsState()
    val imagenUri by viewModel.imagenUri.collectAsState()
    val comentarios by viewModel.comentarios.collectAsState()
    val puntosEstimados by viewModel.puntosEstimados.collectAsState()
    val crearState by viewModel.crearState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showTipoMaterialDialog by remember { mutableStateOf(false) }

// Launcher para seleccionar imagen de galería
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onImagenChange(uri)
    }

// Manejar resultado de creación
    LaunchedEffect(crearState) {
        when (crearState) {
            is Resource.Success -> {
                viewModel.limpiarFormulario()
                onNavigateBack()
            }
            else -> {}
        }
    }

// Diálogo de carga
    if (crearState is Resource.Loading) {
        LoadingDialog(message = "Registrando entrega...")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Entrega") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Instrucciones
            Card(
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
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Registra tu material reciclable y acumula puntos. Un operador validará tu entrega.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Selector de tipo de material
            Text(
                text = "Tipo de Material",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedCard(
                onClick = { showTipoMaterialDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tipoMaterial.getIcono(),
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tipoMaterial.display,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${tipoMaterial.puntosPorKg} puntos por kg",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }
            }

            // Campo de peso
            Text(
                text = "Peso",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            ReciclaTextField(
                value = pesoText,
                onValueChange = { viewModel.onPesoChange(it) },
                label = "Peso en kilogramos",
                keyboardType = KeyboardType.Decimal,
                placeholder = "Ej: 2.5",
                leadingIcon = {
                    Icon(Icons.Default.Scale, contentDescription = null)
                },
                trailingIcon = {
                    Text(
                        text = "kg",
                        modifier = Modifier.padding(end = 12.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                isError = errorMessage != null && pesoText.isNotBlank(),
                errorMessage = errorMessage
            )

            // Puntos estimados
            if (!puntosEstimados.equals(0)) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
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
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(32.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Puntos estimados",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = puntosEstimados.formatPoints(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }

            // Foto
            Text(
                text = "Fotografía (Opcional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (imagenUri != null) {
                // Mostrar imagen seleccionada
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box {
                        Image(
                            painter = rememberAsyncImagePainter(imagenUri),
                            contentDescription = "Foto de la entrega",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )

                        // Botón para eliminar imagen
                        IconButton(
                            onClick = { viewModel.onImagenChange(null) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Eliminar foto",
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // Botón para seleccionar imagen
                OutlinedCard(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Toca para agregar foto",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Comentarios
            Text(
                text = "Comentarios (Opcional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = comentarios,
                onValueChange = { viewModel.onComentariosChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text("Agrega información adicional sobre tu entrega...") },
                maxLines = 5,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón de enviar
            ReciclaButton(
                text = "Registrar Entrega",
                onClick = { viewModel.crearEntrega() },
                isLoading = crearState is Resource.Loading,
                enabled = pesoText.isNotBlank()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    // Diálogo de selección de material
    if (showTipoMaterialDialog) {
        AlertDialog(
            onDismissRequest = { showTipoMaterialDialog = false },
            title = { Text("Selecciona el tipo de material") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TipoMaterial.values().forEach { tipo ->
                        Card(
                            onClick = {
                                viewModel.onTipoMaterialChange(tipo)
                                showTipoMaterialDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tipo.getIcono(),
                                    style = MaterialTheme.typography.headlineSmall
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tipo.display,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${tipo.puntosPorKg} pts/kg",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (tipo == tipoMaterial) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTipoMaterialDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
