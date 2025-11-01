package mx.edu.utng.arg.recicladoloresfirst.ui.screens.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.arg.recicladoloresfirst.data.model.Usuario
import mx.edu.utng.arg.recicladoloresfirst.data.repository.AuthRepository
import mx.edu.utng.arg.recicladoloresfirst.util.Resource
import javax.inject.Inject

/**
 * ViewModel para la pantalla de Login.
 *
 * Responsabilidades:
 * - Validar campos de email y contraseña
 * - Iniciar sesión con email/password
 * - Iniciar sesión con Google
 * - Recuperar contraseña
 * - Mantener estado de la UI
 *
 * Los ViewModel sobreviven a cambios de configuración (rotación de pantalla).
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // ========== ESTADO DE LA UI ==========

    /**
     * Email ingresado por el usuario.
     */
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    /**
     * Contraseña ingresada por el usuario.
     */
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    /**
     * Si la contraseña es visible o está oculta.
     */
    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible: StateFlow<Boolean> = _passwordVisible.asStateFlow()

    /**
     * Estado del proceso de login.
     */
    private val _loginState = MutableStateFlow<Resource<Usuario>?>(null)
    val loginState: StateFlow<Resource<Usuario>?> = _loginState.asStateFlow()

    /**
     * Mensaje de error de validación.
     */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ========== ACCIONES DEL USUARIO ==========

    /**
     * Actualiza el email.
     */
    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        _errorMessage.value = null // Limpiar error al escribir
    }

    /**
     * Actualiza la contraseña.
     */
    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        _errorMessage.value = null
    }

    /**
     * Alterna visibilidad de la contraseña.
     */
    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }

    /**
     * Valida los campos antes de iniciar sesión.
     *
     * @return true si los campos son válidos
     */
    private fun validarCampos(): Boolean {
        when {
            _email.value.isBlank() -> {
                _errorMessage.value = "Ingresa tu correo electrónico"
                return false
            }
            !Patterns.EMAIL_ADDRESS.matcher(_email.value).matches() -> {
                _errorMessage.value = "Correo electrónico inválido"
                return false
            }
            _password.value.isBlank() -> {
                _errorMessage.value = "Ingresa tu contraseña"
                return false
            }
            _password.value.length < 6 -> {
                _errorMessage.value = "La contraseña debe tener al menos 6 caracteres"
                return false
            }
        }
        return true
    }

    /**
     * Inicia sesión con email y contraseña.
     */
    fun iniciarSesion() {
        // Validar campos
        if (!validarCampos()) return

        viewModelScope.launch {
            _loginState.value = Resource.Loading()

            val result = authRepository.iniciarSesion(
                email = _email.value.trim(),
                password = _password.value
            )

            _loginState.value = result
        }
    }

    /**
     * Inicia sesión con Google.
     *
     * @param idToken Token obtenido de Google Sign-In
     */
    fun iniciarSesionConGoogle(idToken: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()

            val result = authRepository.iniciarSesionConGoogle(idToken)

            _loginState.value = result
        }
    }

    /**
     * Envía email para recuperar contraseña.
     */
    fun recuperarPassword() {
        if (_email.value.isBlank()) {
            _errorMessage.value = "Ingresa tu correo para recuperar la contraseña"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(_email.value).matches()) {
            _errorMessage.value = "Correo electrónico inválido"
            return
        }

        viewModelScope.launch {
            when (val result = authRepository.enviarEmailRecuperacion(_email.value.trim())) {
                is Resource.Success -> {
                    _errorMessage.value = "Se envió un correo para restablecer tu contraseña"
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message
                }
                else -> {}
            }
        }
    }

    /**
     * Limpia el estado de login (después de navegar).
     */
    fun limpiarEstado() {
        _loginState.value = null
        _errorMessage.value = null
    }

    /**
     * Limpia todos los campos.
     */
    fun limpiarCampos() {
        _email.value = ""
        _password.value = ""
        _passwordVisible.value = false
        _errorMessage.value = null
    }

}

