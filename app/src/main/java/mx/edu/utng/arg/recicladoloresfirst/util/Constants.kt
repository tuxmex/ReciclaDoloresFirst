package mx.edu.utng.arg.recicladoloresfirst.util

/**
 * Constantes usadas en toda la aplicación.
 *
 * Ventaja: Si necesitas cambiar un valor, solo lo cambias aquí.
 */
object Constants {

    // Firebase Collections
    const val USERS_COLLECTION = "users"
    const val DELIVERIES_COLLECTION = "deliveries"
    const val REWARDS_COLLECTION = "rewards"
    const val REDEMPTIONS_COLLECTION = "redemptions"

    // Firebase Storage
    const val STORAGE_DELIVERIES_PATH = "deliveries"
    const val STORAGE_PROFILES_PATH = "profiles"
    const val STORAGE_REWARDS_PATH = "rewards"
    const val STORAGE_RECEIPTS_PATH = "receipts"

    // Puntos y conversiones
    const val PUNTOS_POR_PESO_DEFAULT = 10 // 10 puntos por kg
    const val MIN_PESO_KG = 0.1
    const val MAX_PESO_KG = 1000.0

    // Límites de la app
    const val MAX_IMAGE_SIZE_MB = 5
    const val MAX_IMAGE_DIMENSION = 1920

    // Roles de usuario
    const val ROL_CIUDADANO = "ciudadano"
    const val ROL_OPERADOR = "operador"
    const val ROL_ADMIN = "admin"

    // Preferencias / SharedPreferences keys (si usáramos)
    const val PREF_USER_ID = "user_id"
    const val PREF_IS_FIRST_TIME = "is_first_time"

    // Navigation
    const val NAV_ARG_USER_ID = "userId"
    const val NAV_ARG_DELIVERY_ID = "deliveryId"
    const val NAV_ARG_REWARD_ID = "rewardId"

    // Validaciones
    const val MIN_PASSWORD_LENGTH = 6
    const val PHONE_LENGTH = 10

    // Mensajes comunes
    const val ERROR_NETWORK = "Error de conexión. Verifica tu internet."
    const val ERROR_UNKNOWN = "Ocurrió un error inesperado."
    const val SUCCESS_SAVE = "Guardado exitosamente."
    const val SUCCESS_DELETE = "Eliminado exitosamente."

    // Tiempos (milisegundos)
    const val SPLASH_DELAY = 2000L
    const val DEBOUNCE_DELAY = 500L
    const val TIMEOUT_NETWORK = 30000L
}