package mx.edu.utng.arg.recicladoloresfirst.data.model
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Modelo que representa un usuario de la aplicación.
 *
 * @property id ID único del usuario (generado por Firebase Auth)
 * @property email Correo electrónico
 * @property nombre Nombre completo
 * @property telefono Teléfono de contacto
 * @property direccion Dirección del usuario
 * @property puntos Puntos acumulados por reciclaje
 * @property rol Rol del usuario: "ciudadano", "operador", "admin"
 * @property activo Si el usuario está activo en el sistema
 * @property fechaRegistro Fecha de creación de la cuenta
 * @property fotoPerfilUrl URL de la foto de perfil (opcional)
 */
data class Usuario(
    @DocumentId
    val id: String = "",
    val email: String = "",
    val nombre: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val puntos: Int = 0,
    val rol: String = "ciudadano", // "ciudadano", "operador", "admin"
    val activo: Boolean = true,
    @ServerTimestamp
    val fechaRegistro: Date? = null,
    val fotoPerfilUrl: String? = null
) {
    /**
     * Valida si el usuario tiene suficientes puntos para un canje.
     */
    fun tienePuntosSuficientes(puntosRequeridos: Int): Boolean {
        return puntos >= puntosRequeridos
    }

    /**
     * Verifica si el usuario es administrador o operador.
     */
    fun esPersonalAutorizado(): Boolean {
        return rol in listOf("operador", "admin")
    }

    companion object {
        // Colección en Firestore
        const val COLLECTION_NAME = "users"

        // Roles disponibles
        const val ROL_CIUDADANO = "ciudadano"
        const val ROL_OPERADOR = "operador"
        const val ROL_ADMIN = "admin"
    }
}