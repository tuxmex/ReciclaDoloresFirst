package mx.edu.utng.arg.recicladoloresfirst.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Botón personalizado de la app.
 *
 * Encapsula el estilo común de todos los botones
 * para mantener consistencia visual.
 *
 * Ejemplo de uso:
 * ```
 * ReciclaButton(
 *     text = "Iniciar Sesión",
 *     onClick = { viewModel.login() },
 *     isLoading = isLoading
 * )
 * ```
 */
@Composable
fun ReciclaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    colors: ButtonColors = ButtonDefaults.buttonColors()
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !isLoading,
        colors = colors,
        shape = MaterialTheme.shapes.medium
    ) {
        if (isLoading) {
            // Mostrar indicador de carga
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            // Mostrar texto del botón
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

/**
 * Botón secundario (outlined).
 */
@Composable
fun ReciclaOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}