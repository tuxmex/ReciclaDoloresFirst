package mx.edu.utng.arg.recicladoloresfirst.util

/**
 * Clase sellada que representa el estado de una operación.
 *
 * ¿Por qué usar esto?
 * Cuando cargamos datos de Firebase, pueden pasar 3 cosas:
 * 1. Está cargando (Loading)
 * 2. Tuvo éxito (Success)
 * 3. Falló (Error)
 *
 * Esta clase nos permite manejar los 3 casos de forma elegante.
 *
 * Ejemplo de uso en ViewModel:
 * ```
 * private val _usuario = MutableStateFlow<Resource<Usuario>>(Resource.Loading())
 * val usuario: StateFlow<Resource<Usuario>> = _usuario.asStateFlow()
 *
 * fun cargarUsuario() {
 *     viewModelScope.launch {
 *         _usuario.value = Resource.Loading()
 *         try {
 *             val data = repository.obtenerUsuario()
 *             _usuario.value = Resource.Success(data)
 *         } catch (e: Exception) {
 *             _usuario.value = Resource.Error(e.message ?: "Error desconocido")
 *         }
 *     }
 * }
 * ```
 *
 * Ejemplo de uso en Composable:
 * ```
 * val usuarioState by viewModel.usuario.collectAsState()
 *
 * when (usuarioState) {
 *     is Resource.Loading -> CircularProgressIndicator()
 *     is Resource.Success -> Text("Hola ${usuarioState.data?.nombre}")
 *     is Resource.Error -> Text("Error: ${usuarioState.message}")
 * }
 * ```
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    /**
     * Estado de carga - muestra indicador de progreso.
     */
    class Loading<T>(data: T? = null) : Resource<T>(data)

    /**
     * Estado exitoso - muestra los datos.
     */
    class Success<T>(data: T) : Resource<T>(data)

    /**
     * Estado de error - muestra mensaje de error.
     */
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}