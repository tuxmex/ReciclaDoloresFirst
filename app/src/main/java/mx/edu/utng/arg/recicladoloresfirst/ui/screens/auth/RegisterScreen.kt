package mx.edu.utng.arg.recicladoloresfirst.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import mx.edu.utng.arg.recicladoloresfirst.ui.components.*
import mx.edu.utng.arg.recicladoloresfirst.util.Resource

/**
 * Pantalla de registro de nuevos usuarios.
 */
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val nombre by viewModel.nombre.collectAsState()
    val email by viewModel.email.collectAsState()
    val telefono by viewModel.telefono.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val passwordVisible by viewModel.passwordVisible.collectAsState()
    val registerState by viewModel.registerState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Manejar resultado del registro
    LaunchedEffect(registerState) {
        when (registerState) {
            is Resource.Success -> {
                viewModel.limpiarEstado()
                onNavigateToHome()
            }
            else -> {}
        }
    }

    // Diálogo de carga
    if (registerState is Resource.Loading) {
        LoadingDialog(message = "Creando cuenta...")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Título
        Text(
            text = "Crear Cuenta",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Únete y comienza a reciclar",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Campo: Nombre completo
        ReciclaTextField(
            value = nombre,
            onValueChange = { viewModel.onNombreChange(it) },
            label = "Nombre completo",
            keyboardType = KeyboardType.Text,
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo: Email
        ReciclaTextField(
            value = email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = "Correo electrónico",
            keyboardType = KeyboardType.Email,
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo: Teléfono
        ReciclaTextField(
            value = telefono,
            onValueChange = { viewModel.onTelefonoChange(it) },
            label = "Teléfono (10 dígitos)",
            keyboardType = KeyboardType.Phone,
            leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = null)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo: Contraseña
        ReciclaTextField(
            value = password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = "Contraseña (mínimo 6 caracteres)",
            keyboardType = KeyboardType.Password,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null)
            },
            trailingIcon = {
                IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Default.Visibility
                        } else {
                            Icons.Default.VisibilityOff
                        },
                        contentDescription = null
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo: Confirmar contraseña
        ReciclaTextField(
            value = confirmPassword,
            onValueChange = { viewModel.onConfirmPasswordChange(it) },
            label = "Confirmar contraseña",
            keyboardType = KeyboardType.Password,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null)
            }
        )

        // Mensaje de error
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de registro
        ReciclaButton(
            text = "Registrarse",
            onClick = { viewModel.registrar() },
            isLoading = registerState is Resource.Loading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Link a login
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("¿Ya tienes cuenta?")
            TextButton(onClick = onNavigateToLogin) {
                Text("Inicia sesión")
            }
        }
    }
}
