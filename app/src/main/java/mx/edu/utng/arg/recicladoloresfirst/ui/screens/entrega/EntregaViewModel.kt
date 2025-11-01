package mx.edu.utng.arg.recicladoloresfirst.ui.screens.entrega

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.arg.recicladoloresfirst.data.model.Entrega
import mx.edu.utng.arg.recicladoloresfirst.data.model.TipoMaterial
import mx.edu.utng.arg.recicladoloresfirst.data.repository.AuthRepository
import mx.edu.utng.arg.recicladoloresfirst.data.repository.EntregaRepository
import mx.edu.utng.arg.recicladoloresfirst.data.repository.UsuarioRepository
import mx.edu.utng.arg.recicladoloresfirst.util.Resource
import javax.inject.Inject

/**
 * ViewModel para registrar una nueva entrega de material reciclable.
 *
 * Flujo:
 * 1. Usuario selecciona tipo de material
 * 2. Ingresa peso en kg
 * 3. Toma/selecciona foto (opcional)
 * 4. Agrega comentarios (opcional)
 * 5. Envía entrega
 */
@HiltViewModel
class EntregaViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val entregaRepository: EntregaRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    // ========== ESTADO DEL FORMULARIO ==========

    /**
     * Tipo de material seleccionado.
     */
    private val _tipoMaterial = MutableStateFlow(TipoMaterial.PET)
    val tipoMaterial: StateFlow<TipoMaterial> = _tipoMaterial.asStateFlow()

    /**
     * Peso en kg (como String para el TextField).
     */
    private val _pesoText = MutableStateFlow("")
    val pesoText: StateFlow<String> = _pesoText.asStateFlow()

    /**
     * URI de la imagen seleccionada.
     */
    private val _imagenUri = MutableStateFlow<Uri?>(null)
    val imagenUri: StateFlow<Uri?> = _imagenUri.asStateFlow()

    /**
     * Comentarios adicionales.
     */
    private val _comentarios = MutableStateFlow("")
    val comentarios: StateFlow<String> = _comentarios.asStateFlow()

    /**
     * Puntos que se generarán (calculados automáticamente).
     */
    private val _puntosEstimados = MutableStateFlow(0)
    val puntosEstimados: StateFlow<Int> = _puntosEstimados.asStateFlow()

    /**
     * Estado del proceso de creación.
     */
    private val _crearState = MutableStateFlow<Resource<String>?>(null)
    val crearState: StateFlow<Resource<String>?> = _crearState.asStateFlow()

    /**
     * Mensaje de error de validación.
     */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ========== ID Y NOMBRE DEL USUARIO ==========

    private val userId = authRepository.currentUserId ?: ""
    private var nombreUsuario = ""

    init {
        cargarNombreUsuario()
    }

    /**
     * Carga el nombre del usuario para incluirlo en la entrega.
     */
    private fun cargarNombreUsuario() {
        viewModelScope.launch {
            when (val result = usuarioRepository.obtenerUsuario(userId)) {
                is Resource.Success -> {
                    nombreUsuario = result.data?.nombre ?: ""
                }
                else -> {}
            }
        }
    }

    // ========== ACCIONES DEL USUARIO ==========

    /**
     * Actualiza el tipo de material.
     */
    fun onTipoMaterialChange(tipo: TipoMaterial) {
        _tipoMaterial.value = tipo
        calcularPuntosEstimados()
    }

    /**
     * Actualiza el peso.
     */
    fun onPesoChange(nuevoPeso: String) {
        // Solo permitir números y un punto decimal
        val filtrado = nuevoPeso.filter { it.isDigit() || it == '.' }

        // Evitar múltiples puntos decimales
        if (filtrado.count { it == '.' } <= 1) {
            _pesoText.value = filtrado
            calcularPuntosEstimados()
            _errorMessage.value = null
        }
    }

    /**
     * Actualiza la URI de la imagen.
     */
    fun onImagenChange(uri: Uri?) {
        _imagenUri.value = uri
    }

    /**
     * Actualiza los comentarios.
     */
    fun onComentariosChange(nuevosComentarios: String) {
        _comentarios.value = nuevosComentarios
    }

    /**
     * Calcula puntos estimados basados en peso y tipo de material.
     */
    private fun calcularPuntosEstimados() {
        val peso = _pesoText.value.toDoubleOrNull() ?: 0.0
        val puntosPorKg = _tipoMaterial.value.puntosPorKg
        _puntosEstimados.value = (peso * puntosPorKg).toInt()
    }

    /**
     * Valida el formulario antes de enviar.
     */
    private fun validarFormulario(): Boolean {
        val peso = _pesoText.value.toDoubleOrNull()

        when {
            peso == null || peso <= 0 -> {
                _errorMessage.value = "Ingresa un peso válido"
                return false
            }
            peso < 0.1 -> {
                _errorMessage.value = "El peso mínimo es 0.1 kg"
                return false
            }
            peso > 1000 -> {
                _errorMessage.value = "El peso máximo es 1000 kg"
                return false
            }
        }

        return true
    }

    /**
     * Crea y envía la entrega.
     */
    fun crearEntrega() {
        if (!validarFormulario()) return

        viewModelScope.launch {
            _crearState.value = Resource.Loading()

            val peso = _pesoText.value.toDouble()

            val entrega = Entrega(
                usuarioId = userId,
                usuarioNombre = nombreUsuario,
                tipoMaterial = _tipoMaterial.value,
                pesoKg = peso,
                comentarios = _comentarios.value.trim()
            )

            val result = entregaRepository.crearEntrega(
                entrega = entrega,
                imagenUri = _imagenUri.value
            )

            _crearState.value = result
        }
    }

    /**
     * Limpia el formulario después de crear exitosamente.
     */
    fun limpiarFormulario() {
        _tipoMaterial.value = TipoMaterial.PET
        _pesoText.value = ""
        _imagenUri.value = null
        _comentarios.value = ""
        _puntosEstimados.value = 0
        _crearState.value = null
        _errorMessage.value = null
    }
}