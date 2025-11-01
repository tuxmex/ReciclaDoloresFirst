package mx.edu.utng.arg.recicladoloresfirst.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mx.edu.utng.arg.recicladoloresfirst.data.model.Entrega
import mx.edu.utng.arg.recicladoloresfirst.data.model.Usuario
import mx.edu.utng.arg.recicladoloresfirst.data.repository.AuthRepository
import mx.edu.utng.arg.recicladoloresfirst.data.repository.EntregaRepository
import mx.edu.utng.arg.recicladoloresfirst.data.repository.UsuarioRepository
import mx.edu.utng.arg.recicladoloresfirst.util.Resource
import javax.inject.Inject

/**
 * ViewModel para la pantalla de inicio.
 *
 * Muestra:
 * - Información del usuario (nombre, puntos)
 * - Últimas entregas
 * - Estadísticas rápidas
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository,
    private val entregaRepository: EntregaRepository
) : ViewModel() {

    /**
     * ID del usuario actual.
     */
    private val userId: String = authRepository.currentUserId ?: ""

    /**
     * Datos del usuario en tiempo real.
     *
     * Se actualiza automáticamente cuando cambian los puntos u otros datos.
     */
    val usuario: StateFlow<Resource<Usuario>> = usuarioRepository
        .obtenerUsuarioFlow(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Loading()
        )

    /**
     * Últimas 5 entregas del usuario.
     */
    val ultimasEntregas: StateFlow<Resource<List<Entrega>>> = entregaRepository
        .obtenerEntregasUsuario(userId)
        .map { resource ->
            when (resource) {
                is Resource.Success -> {
                    // Tomar solo las 5 más recientes
                    Resource.Success(resource.data?.take(5) ?: emptyList())
                }
                is Resource.Error -> resource
                is Resource.Loading -> resource
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Loading()
        )

    /**
     * Estadísticas del usuario (entregas totales, aprobadas, etc.).
     */
    private val _estadisticas = MutableStateFlow<Resource<Map<String, Int>>>(Resource.Loading())
    val estadisticas: StateFlow<Resource<Map<String, Int>>> = _estadisticas.asStateFlow()

    init {
        cargarEstadisticas()
    }

    /**
     * Carga estadísticas del usuario.
     */
    private fun cargarEstadisticas() {
        viewModelScope.launch {
            _estadisticas.value = Resource.Loading()
            _estadisticas.value = entregaRepository.obtenerEstadisticasUsuario(userId)
        }
    }

    /**
     * Recarga todas las datos (pull to refresh).
     */
    fun recargar() {
        cargarEstadisticas()
        // Los flows se recargan automáticamente
    }

    /**
     * Cierra sesión del usuario.
     */
    fun cerrarSesion() {
        authRepository.cerrarSesion()
    }
}