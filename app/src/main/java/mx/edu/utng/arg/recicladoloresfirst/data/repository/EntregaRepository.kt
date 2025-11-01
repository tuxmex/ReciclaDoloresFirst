package mx.edu.utng.arg.recicladoloresfirst.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import mx.edu.utng.arg.recicladoloresfirst.data.model.Entrega
import mx.edu.utng.arg.recicladoloresfirst.data.model.EstadoEntrega
import mx.edu.utng.arg.recicladoloresfirst.util.Resource
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar entregas de material reciclable.
 *
 * Flujo completo de una entrega:
 * 1. Usuario crea entrega (estado: PENDIENTE) → crearEntrega()
 * 2. Operador la revisa y valida → validarEntrega()
 * 3. Si es aprobada, se suman puntos al usuario
 * 4. Si es rechazada, no se suman puntos
 */
@Singleton
class EntregaRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val usuarioRepository: UsuarioRepository
) {

    /**
     * Crea una nueva entrega de material reciclable.
     *
     * Flujo:
     * 1. Subir foto a Storage (si existe)
     * 2. Calcular puntos según peso y tipo de material
     * 3. Crear documento en Firestore con estado PENDIENTE
     *
     * @param entrega Datos de la entrega
     * @param imagenUri URI de la foto (opcional)
     *
     * @return Resource<String> con ID de la entrega creada
     */
    suspend fun crearEntrega(
        entrega: Entrega,
        imagenUri: Uri?
    ): Resource<String> {
        return try {
            // Paso 1: Subir imagen si existe
            val fotoUrl = if (imagenUri != null) {
                subirImagenEntrega(imagenUri)
            } else null

            // Paso 2: Calcular puntos
            val puntosGenerados = entrega.calcularPuntos()

            // Paso 3: Crear documento
            val entregaConDatos = entrega.copy(
                fotoUrl = fotoUrl,
                puntosGenerados = puntosGenerados,
                estado = EstadoEntrega.PENDIENTE
            )

            val docRef = firestore.collection(Entrega.COLLECTION_NAME)
                .add(entregaConDatos)
                .await()

            Resource.Success(docRef.id)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al crear entrega")
        }
    }

    /**
     * Sube imagen de una entrega a Storage.
     *
     * @param imageUri URI de la imagen
     * @return URL de descarga de la imagen
     */
    private suspend fun subirImagenEntrega(imageUri: Uri): String {
        val timestamp = System.currentTimeMillis()
        val filename = "delivery_${timestamp}.jpg"

        val storageRef = storage.reference
            .child("deliveries")
            .child(filename)

        storageRef.putFile(imageUri).await()
        return storageRef.downloadUrl.await().toString()
    }

    /**
     * Obtiene entregas del usuario en tiempo real.
     *
     * @param usuarioId ID del usuario
     * @return Flow con lista de entregas ordenadas por fecha (más recientes primero)
     */
    fun obtenerEntregasUsuario(usuarioId: String): Flow<Resource<List<Entrega>>> = callbackFlow {
        trySend(Resource.Loading())

        val listener = firestore.collection(Entrega.COLLECTION_NAME)
            .whereEqualTo("usuarioId", usuarioId)
            .orderBy("fechaEntrega", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error desconocido"))
                    return@addSnapshotListener
                }

                val entregas = snapshot?.toObjects(Entrega::class.java) ?: emptyList()
                trySend(Resource.Success(entregas))
            }

        awaitClose { listener.remove() }
    }

    /**
     * Obtiene todas las entregas pendientes de validación.
     *
     * Usado por operadores para revisar entregas.
     */
    fun obtenerEntregasPendientes(): Flow<Resource<List<Entrega>>> = callbackFlow {
        trySend(Resource.Loading())

        val listener = firestore.collection(Entrega.COLLECTION_NAME)
            .whereEqualTo("estado", EstadoEntrega.PENDIENTE.name)
            .orderBy("fechaEntrega", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error desconocido"))
                    return@addSnapshotListener
                }

                val entregas = snapshot?.toObjects(Entrega::class.java) ?: emptyList()
                trySend(Resource.Success(entregas))
            }

        awaitClose { listener.remove() }
    }

    /**
     * Obtiene una entrega específica por su ID.
     */
    suspend fun obtenerEntrega(entregaId: String): Resource<Entrega> {
        return try {
            val doc = firestore.collection(Entrega.COLLECTION_NAME)
                .document(entregaId)
                .get()
                .await()

            val entrega = doc.toObject(Entrega::class.java)
                ?: return Resource.Error("Entrega no encontrada")

            Resource.Success(entrega)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener entrega")
        }
    }

    /**
     * Valida una entrega (aprobar o rechazar).
     *
     * Este método debe ser llamado solo por operadores o admins.
     *
     * Flujo si se APRUEBA:
     * 1. Actualizar estado de entrega a APROBADA
     * 2. Sumar puntos al usuario
     * 3. Registrar quién validó y cuándo
     *
     * Flujo si se RECHAZA:
     * 1. Actualizar estado a RECHAZADA
     * 2. Registrar motivo de rechazo
     * 3. NO sumar puntos
     *
     * @param entregaId ID de la entrega
     * @param aprobada true para aprobar, false para rechazar
     * @param operadorId ID del operador que valida
     * @param motivoRechazo Motivo si es rechazada (opcional)
     */
    suspend fun validarEntrega(
        entregaId: String,
        aprobada: Boolean,
        operadorId: String,
        motivoRechazo: String? = null
    ): Resource<Unit> {
        return try {
            // Obtener entrega actual
            val entregaDoc = firestore.collection(Entrega.COLLECTION_NAME)
                .document(entregaId)
                .get()
                .await()

            val entrega = entregaDoc.toObject(Entrega::class.java)
                ?: return Resource.Error("Entrega no encontrada")

            // Verificar que esté pendiente
            if (entrega.estado != EstadoEntrega.PENDIENTE) {
                return Resource.Error("Esta entrega ya fue validada")
            }

            // Preparar actualización
            val updates = mutableMapOf<String, Any>(
                "estado" to if (aprobada) EstadoEntrega.APROBADA.name else EstadoEntrega.RECHAZADA.name,
                "validadoPor" to operadorId,
                "fechaValidacion" to Date()
            )

            if (!aprobada && motivoRechazo != null) {
                updates["motivoRechazo"] = motivoRechazo
            }

            // Actualizar entrega
            firestore.collection(Entrega.COLLECTION_NAME)
                .document(entregaId)
                .update(updates)
                .await()

            // Si fue aprobada, sumar puntos al usuario
            if (aprobada) {
                usuarioRepository.sumarPuntos(
                    entrega.usuarioId,
                    entrega.puntosGenerados
                )
            }

            Resource.Success(Unit)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al validar entrega")
        }
    }

    /**
     * Obtiene estadísticas de entregas del usuario.
     *
     * @return Map con estadísticas: totalEntregas, aprobadas, rechazadas, pendientes, totalPuntos
     */
    suspend fun obtenerEstadisticasUsuario(usuarioId: String): Resource<Map<String, Int>> {
        return try {
            val snapshot = firestore.collection(Entrega.COLLECTION_NAME)
                .whereEqualTo("usuarioId", usuarioId)
                .get()
                .await()

            val entregas = snapshot.toObjects(Entrega::class.java)

            val stats = mapOf(
                "totalEntregas" to entregas.size,
                "aprobadas" to entregas.count { it.estado == EstadoEntrega.APROBADA },
                "rechazadas" to entregas.count { it.estado == EstadoEntrega.RECHAZADA },
                "pendientes" to entregas.count { it.estado == EstadoEntrega.PENDIENTE },
                "totalPuntos" to entregas
                    .filter { it.estado == EstadoEntrega.APROBADA }
                    .sumOf { it.puntosGenerados }
            )

            Resource.Success(stats)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener estadísticas")
        }
    }

    /**
     * Elimina una entrega (solo si está pendiente).
     *
     * Útil si el usuario se equivocó al registrar.
     */
    suspend fun eliminarEntrega(entregaId: String, usuarioId: String): Resource<Unit> {
        return try {
            val doc = firestore.collection(Entrega.COLLECTION_NAME)
                .document(entregaId)
                .get()
                .await()

            val entrega = doc.toObject(Entrega::class.java)
                ?: return Resource.Error("Entrega no encontrada")

            // Verificar que sea del usuario
            if (entrega.usuarioId != usuarioId) {
                return Resource.Error("No tienes permiso para eliminar esta entrega")
            }

            // Solo se puede eliminar si está pendiente
            if (entrega.estado != EstadoEntrega.PENDIENTE) {
                return Resource.Error("Solo puedes eliminar entregas pendientes")
            }

            // Eliminar documento
            firestore.collection(Entrega.COLLECTION_NAME)
                .document(entregaId)
                .delete()
                .await()

            // Eliminar imagen si existe
            entrega.fotoUrl?.let { url ->
                try {
                    storage.getReferenceFromUrl(url).delete().await()
                } catch (e: Exception) {
                    // No es crítico si falla
                }
            }

            Resource.Success(Unit)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al eliminar entrega")
        }
    }
}