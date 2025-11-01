package mx.edu.utng.arg.recicladoloresfirst.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Modelo que representa una recompensa disponible para canje.
 *
 * Ejemplo: "Beca escolar de $500" que cuesta 500 puntos
 */
data class Recompensa(
    @DocumentId
    val id: String = "",
    val titulo: String = "",                 // "Beca Escolar"
    val descripcion: String = "",            // Descripción detallada
    val categoria: CategoriaRecompensa = CategoriaRecompensa.BECA,
    val costoEnPuntos: Int = 0,
    val valorMonetario: Double = 0.0,        // Valor real en pesos
    val imagenUrl: String? = null,
    val cantidadDisponible: Int = 0,         // -1 = ilimitado
    val activa: Boolean = true,
    val requisitos: List<String> = emptyList(), // Ej: "Ser estudiante", "Mayor de edad"
    @ServerTimestamp
    val fechaCreacion: Date? = null,
    val creadoPor: String = "",              // ID del admin que la creó
    val vigenciaInicio: Date? = null,
    val vigenciaFin: Date? = null
) {
    /**
     * Verifica si la recompensa está disponible.
     */
    fun estaDisponible(): Boolean {
        val hayStock = cantidadDisponible == -1 || cantidadDisponible > 0
        val estaActiva = activa
        val estaVigente = if (vigenciaFin != null) {
            Date().before(vigenciaFin)
        } else true

        return hayStock && estaActiva && estaVigente
    }

    /**
     * Reduce la cantidad disponible (después de un canje).
     */
    fun reducirStock(): Recompensa {
        return if (cantidadDisponible > 0) {
            this.copy(cantidadDisponible = cantidadDisponible - 1)
        } else {
            this
        }
    }

    companion object {
        const val COLLECTION_NAME = "rewards"
        const val STOCK_ILIMITADO = -1
    }
}

/**
 * Categorías de recompensas.
 */
enum class CategoriaRecompensa(val display: String, val icono: String) {
    BECA("Becas Educativas", "🎓"),
    APOYO_ECONOMICO("Apoyos Económicos", "💰"),
    DESCUENTO("Descuentos Municipales", "🏛️"),
    SERVICIO("Servicios Públicos", "🚰"),
    CULTURA("Eventos Culturales", "🎭"),
    DEPORTE("Deportes y Recreación", "⚽"),
    SALUD("Servicios de Salud", "🏥");
}