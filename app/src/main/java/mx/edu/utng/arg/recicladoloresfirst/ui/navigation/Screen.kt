package mx.edu.utng.arg.recicladoloresfirst.ui.navigation

/**
 * Sealed class que define todas las rutas de navegación.
 *
 * Usar sealed class en vez de Strings previene errores de tipeo
 * y facilita el refactoring.
 */
sealed class Screen(val route: String) {

    // Autenticación
    object Login : Screen("login")
    object Register : Screen("register")

    // Principal
    object Home : Screen("home")
    object Perfil : Screen("perfil")

    // Entregas
    object NuevaEntrega : Screen("nueva_entrega")
    object MisEntregas : Screen("mis_entregas")
    object DetalleEntrega : Screen("detalle_entrega/{entregaId}") {
        fun createRoute(entregaId: String) = "detalle_entrega/$entregaId"
    }

    // Recompensas
    object Recompensas : Screen("recompensas")
    object DetalleRecompensa : Screen("detalle_recompensa/{recompensaId}") {
        fun createRoute(recompensaId: String) = "detalle_recompensa/$recompensaId"
    }

    // Canjes
    object MisCanjes : Screen("mis_canjes")
    object Historial : Screen("historial")

    // Admin/Operador
    object ValidarEntregas : Screen("validar_entregas")
    object GestionRecompensas : Screen("gestion_recompensas")
    object GestionCanjes : Screen("gestion_canjes")
}