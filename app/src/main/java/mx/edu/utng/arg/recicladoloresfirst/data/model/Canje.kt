package mx.edu.utng.arg.recicladoloresfirst.data.model

import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Modelo que representa un canje de puntos por recompensa.
 *
 * Flujo: Usuario solicita canje → Admin lo revisa → Aprueba/Rechaza → Entrega
 */
data class Canje(
    @DocumentId
    val id: String = "",
    val usuarioId: String = "",
    val usuarioNombre: String = "",
    val recompensaId: String = "",
    val recompensaTitulo: String = "",       // Para mostrar sin consultar
    val puntosUtilizados: Int = 0,
    val estado: EstadoCanje = EstadoCanje.SOLICITADO,
    @ServerTimestamp
    val fechaSolicitud: Date? = null,
    val revisadoPor: String? = null,         // ID del admin
    val fechaRevision: Date? = null,
    val motivoRechazo: String? = null,
    val fechaEntrega: Date? = null,
    val comentariosUsuario: String = "",     // Comentarios del ciudadano
    val comentariosAdmin: String = "",       // Comentarios del administrador
    val comprobanteUrl: String? = null       // URL de comprobante de entrega
) {
    /**
     * Verifica si el canje está en un estado final.
     */
    fun estaFinalizado(): Boolean {
        return estado in listOf(
            EstadoCanje.ENTREGADO,
            EstadoCanje.RECHAZADO,
            EstadoCanje.CANCELADO
        )
    }

    /**
     * Verifica si se pueden devolver los puntos al usuario.
     */
    fun permiteDevolucionPuntos(): Boolean {
        return estado in listOf(
            EstadoCanje.RECHAZADO,
            EstadoCanje.CANCELADO
        )
    }

    companion object {
        const val COLLECTION_NAME = "redemptions"
    }
}

/**
 * Estados del proceso de canje.
 */
enum class EstadoCanje(val display: String) {
    SOLICITADO("Solicitado - En revisión"),
    APROBADO("Aprobado - Pendiente de entrega"),
    EN_PROCESO("En proceso de entrega"),
    ENTREGADO("Entregado"),
    RECHAZADO("Rechazado"),
    CANCELADO("Cancelado por usuario");

    fun getColor(): Color {
        return when (this) {
            SOLICITADO -> Color(0xFFFFA726) // Naranja
            APROBADO -> Color(0xFF42A5F5) // Azul
            EN_PROCESO -> Color(0xFF66BB6A) // Verde claro
            ENTREGADO -> Color(0xFF4CAF50) // Verde
            RECHAZADO, CANCELADO -> Color(0xFFEF5350) // Rojo
        }
    }

    fun getIcono(): String {
        return when (this) {
            SOLICITADO -> "⏳"
            APROBADO -> "✅"
            EN_PROCESO -> "🚚"
            ENTREGADO -> "🎉"
            RECHAZADO -> "❌"
            CANCELADO -> "🚫"
        }
    }
}