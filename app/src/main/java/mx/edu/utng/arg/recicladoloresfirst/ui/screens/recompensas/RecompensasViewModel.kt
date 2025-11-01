package mx.edu.utng.arg.recicladoloresfirst.ui.screens.recompensas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mx.edu.utng.arg.recicladoloresfirst.data.model.Recompensa
import mx.edu.utng.arg.recicladoloresfirst.data.repository.AuthRepository
import mx.edu.utng.arg.recicladoloresfirst.data.repository.RecompensaRepository
import mx.edu.utng.arg.recicladoloresfirst.data.repository.UsuarioRepository
import mx.edu.utng.arg.recicladoloresfirst.util.Resource
import javax.inject.Inject

@HiltViewModel
class RecompensasViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val recompensaRepository: RecompensaRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val userId = authRepository.currentUserId ?: ""

    /**
     * Lista de recompensas disponibles.
     */
    val recompensas: StateFlow<Resource<List<Recompensa>>> = recompensaRepository
        .obtenerRecompensasActivas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Loading()
        )

    /**
     * Puntos del usuario actual.
     */
    val puntosUsuario: StateFlow<Int> = usuarioRepository
        .obtenerUsuarioFlow(userId)
        .map { resource ->
            when (resource) {
                is Resource.Success -> resource.data?.puntos ?: 0
                else -> 0
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun recargar() {
        // Los flows se recargan automáticamente
        viewModelScope.launch {
            // Forzar recarga si es necesario
        }
    }
}