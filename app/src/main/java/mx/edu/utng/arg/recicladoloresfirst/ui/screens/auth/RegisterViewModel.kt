package mx.edu.utng.arg.recicladoloresfirst.ui.screens.auth

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
import mx.edu.utng.arg.recicladoloresfirst.util.isValidEmail
import mx.edu.utng.arg.recicladoloresfirst.util.isValidPhone
import mx.edu.utng.arg.recicladoloresfirst.util.toTitleCase
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Estados del formulario
    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _telefono = MutableStateFlow("")
    val telefono: StateFlow<String> = _telefono.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible: StateFlow<Boolean> = _passwordVisible.asStateFlow()

    private val _registerState = MutableStateFlow<Resource<Usuario>?>(null)
    val registerState: StateFlow<Resource<Usuario>?> = _registerState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Acciones
    fun onNombreChange(newNombre: String) {
        _nombre.value = newNombre
        _errorMessage.value = null
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        _errorMessage.value = null
    }

    fun onTelefonoChange(newTelefono: String) {
        // Solo permitir números
        val filtrado = newTelefono.filter { it.isDigit() }.take(10)
        _telefono.value = filtrado
        _errorMessage.value = null
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        _errorMessage.value = null
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
        _errorMessage.value = null
    }

    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }

    /**
     * Valida el formulario de registro.
     */
    private fun validarFormulario(): Boolean {
        when {
            _nombre.value.isBlank() -> {
                _errorMessage.value = "Ingresa tu nombre completo"
                return false
            }
            _nombre.value.length < 3 -> {
                _errorMessage.value = "El nombre debe tener al menos 3 caracteres"
                return false
            }
            _email.value.isBlank() -> {
                _errorMessage.value = "Ingresa tu correo electrónico"
                return false
            }
            !_email.value.isValidEmail() -> {
                _errorMessage.value = "Correo electrónico inválido"
                return false
            }
            _telefono.value.isBlank() -> {
                _errorMessage.value = "Ingresa tu teléfono"
                return false
            }
            !_telefono.value.isValidPhone() -> {
                _errorMessage.value = "El teléfono debe tener 10 dígitos"
                return false
            }
            _password.value.isBlank() -> {
                _errorMessage.value = "Ingresa una contraseña"
                return false
            }
            _password.value.length < 6 -> {
                _errorMessage.value = "La contraseña debe tener al menos 6 caracteres"
                return false
            }
            _confirmPassword.value != _password.value -> {
                _errorMessage.value = "Las contraseñas no coinciden"
                return false
            }
        }
        return true
    }

    /**
     * Registra un nuevo usuario.
     */
    fun registrar() {
        if (!validarFormulario()) return

        viewModelScope.launch {
            _registerState.value = Resource.Loading()

            val result = authRepository.registrarUsuario(
                email = _email.value.trim(),
                password = _password.value,
                nombre = _nombre.value.trim().toTitleCase(),
                telefono = _telefono.value
            )

            _registerState.value = result
        }
    }

    fun limpiarEstado() {
        _registerState.value = null
        _errorMessage.value = null
    }
}
