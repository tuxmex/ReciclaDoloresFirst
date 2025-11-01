package mx.edu.utng.arg.recicladoloresfirst.data.model

import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Modelo que representa una entrega de material reciclable.
 *
 * Ejemplo: Doña María lleva 3 kg de PET → se crea esta entrega
 */
data class Entrega(
    @DocumentId
    val id: String = "",
    val usuarioId: String = "",              // ID del usuario que entrega
    val usuarioNombre: String = "",          // Nombre (para mostrar sin consultar usuario)
    val tipoMaterial: TipoMaterial = TipoMaterial.PET,
    val pesoKg: Double = 0.0,
    val puntosGenerados: Int = 0,
    val fotoUrl: String? = null,             // URL de la foto en Storage
    val estado: EstadoEntrega = EstadoEntrega.PENDIENTE,
    val ubicacion: GeoPoint? = null,         // Ubicación GPS (opcional)
    val comentarios: String = "",
    @ServerTimestamp
    val fechaEntrega: Date? = null,
    val validadoPor: String? = null,         // ID del operador que validó
    val fechaValidacion: Date? = null,
    val motivoRechazo: String? = null        // Si fue rechazada, por qué
) {
    /**
     * Calcula puntos basados en tipo de material y peso.
     */
    fun calcularPuntos(): Int {
        return (pesoKg * tipoMaterial.puntosPorKg).toInt()
    }

    companion object {
        const val COLLECTION_NAME = "deliveries"
    }
}

/**
 * Tipos de material reciclable con sus puntos.
 */
enum class TipoMaterial(val display: String, val puntosPorKg: Double) {
    PET("PET (Botellas plásticas)", 10.0),
    PLASTICO("Plástico general", 8.0),
    VIDRIO("Vidrio", 5.0),
    PAPEL("Papel y cartón", 3.0),
    METAL("Metal (latas, aluminio)", 15.0),
    ELECTRONICO("Electrónico", 20.0),
    ORGANICO("Orgánico (compostable)", 2.0);

    /**
     * Obtiene el icono de emoji para mostrar en UI.
     */
    fun getIcono(): String {
        return when (this) {
            PET -> "♻️"
            PLASTICO -> "🥤"
            VIDRIO -> "🍾"
            PAPEL -> "📄"
            METAL -> "🥫"
            ELECTRONICO -> "📱"
            ORGANICO -> "🌱"
        }
    }

}

/**

Estados posibles de una entrega. */
enum class EstadoEntrega(val display: String) {
    PENDIENTE("Pendiente de validación"), APROBADA("Aprobada"), RECHAZADA(
        "Rechazada"
    );

    fun getColor(): Color {
        return when (this) {
            PENDIENTE -> Color(0xFFFFA726) // Naranja
            APROBADA -> androidx.compose.ui.graphics.Color(0xFF66BB6A) // Verde
            RECHAZADA -> androidx.compose.ui.graphics.Color(0xFFEF5350) // Rojo
        }
    }
}

