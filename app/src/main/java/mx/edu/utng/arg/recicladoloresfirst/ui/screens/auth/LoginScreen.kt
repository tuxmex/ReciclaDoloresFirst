package mx.edu.utng.arg.recicladoloresfirst.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
 * Pantalla de inicio de sesión.
 *
 * Permite al usuario:
 * - Iniciar sesión con email/contraseña
 * - Iniciar sesión con Google
 * - Recuperar contraseña
 * - Navegar a registro
 */

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    // Recolectar estados del ViewModel
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val passwordVisible by viewModel.passwordVisible.collectAsState()
    val loginState by viewModel.loginState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Manejar resultado del login
    LaunchedEffect(loginState) {
        when (loginState) {
            is Resource.Success -> {
                viewModel.limpiarEstado()
                onNavigateToHome()
            }
            else -> {}
        }
    }

    // Mostrar diálogo de carga
    if (loginState is Resource.Loading) {
        LoadingDialog(message = "Iniciando sesión...")
    }

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo o título
        Text(
            text = "♻️ ReciclaD Hidalgo",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Recicla y gana recompensas",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Campo de email
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

        // Campo de contraseña
        ReciclaTextField(
            value = password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = "Contraseña",
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
                        contentDescription = if (passwordVisible) {
                            "Ocultar contraseña"
                        } else {
                            "Mostrar contraseña"
                        }
                    )
                }
            }
        )

        // Mensaje de error
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Recuperar contraseña
        TextButton(
            onClick = { viewModel.recuperarPassword() },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("¿Olvidaste tu contraseña?")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de login
        ReciclaButton(
            text = "Iniciar Sesión",
            onClick = { viewModel.iniciarSesion() },
            isLoading = loginState is Resource.Loading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "  o  ",
                style = MaterialTheme.typography.bodySmall
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de Google (implementación completa requiere configuración adicional)
        ReciclaOutlinedButton(
            text = "Continuar con Google",
            onClick = { /* TODO: Implementar Google Sign-In */ }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Link a registro
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("¿No tienes cuenta?")
            TextButton(onClick = onNavigateToRegister) {
                Text("Regístrate")
            }
        }
    }
}