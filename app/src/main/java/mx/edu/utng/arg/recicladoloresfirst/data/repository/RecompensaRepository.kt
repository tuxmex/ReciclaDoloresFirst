package mx.edu.utng.arg.recicladoloresfirst.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import mx.edu.utng.arg.recicladoloresfirst.data.model.Recompensa
import mx.edu.utng.arg.recicladoloresfirst.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar recompensas (becas, apoyos).
 *
 * Las recompensas son creadas por administradores municipales.
 */
@Singleton
class RecompensaRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    /**
     * Obtiene todas las recompensas activas en tiempo real.
     *
     * @return Flow con lista de recompensas ordenadas por puntos (menor a mayor)
     */
    fun obtenerRecompensasActivas(): Flow<Resource<List<Recompensa>>> = callbackFlow {
        trySend(Resource.Loading())

        val listener = firestore.collection(Recompensa.COLLECTION_NAME)
            .whereEqualTo("activa", true)
            .orderBy("costoEnPuntos", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error desconocido"))
                    return@addSnapshotListener
                }

                val recompensas = snapshot?.toObjects(Recompensa::class.java)
                    ?.filter { it.estaDisponible() } ?: emptyList()

                trySend(Resource.Success(recompensas))
            }

        awaitClose { listener.remove() }
    }

    /**
     * Obtiene una recompensa específica por ID.
     */
    suspend fun obtenerRecompensa(recompensaId: String): Resource<Recompensa> {
        return try {
            val doc = firestore.collection(Recompensa.COLLECTION_NAME)
                .document(recompensaId)
                .get()
                .await()

            val recompensa = doc.toObject(Recompensa::class.java)
                ?: return Resource.Error("Recompensa no encontrada")

            Resource.Success(recompensa)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener recompensa")
        }
    }

    /**
     * Crea una nueva recompensa (solo admins).
     *
     * @param recompensa Datos de la recompensa
     * @param imagenUri URI de imagen (opcional)
     * @param adminId ID del admin que la crea
     *
     * @return Resource<String> con ID de la recompensa creada
     */
    suspend fun crearRecompensa(
        recompensa: Recompensa,
        imagenUri: Uri?,
        adminId: String
    ): Resource<String> {
        return try {
            // Subir imagen si existe
            val imagenUrl = if (imagenUri != null) {
                subirImagenRecompensa(imagenUri)
            } else null

            val recompensaConDatos = recompensa.copy(
                imagenUrl = imagenUrl,
                creadoPor = adminId
            )

            val docRef = firestore.collection(Recompensa.COLLECTION_NAME)
                .add(recompensaConDatos)
                .await()

            Resource.Success(docRef.id)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al crear recompensa")
        }
    }

    /**
     * Sube imagen de recompensa a Storage.
     */
    private suspend fun subirImagenRecompensa(imageUri: Uri): String {
        val timestamp = System.currentTimeMillis()
        val filename = "reward_${timestamp}.jpg"

        val storageRef = storage.reference
            .child("rewards")
            .child(filename)

        storageRef.putFile(imageUri).await()
        return storageRef.downloadUrl.await().toString()
    }

    /**
     * Actualiza una recompensa existente.
     */
    suspend fun actualizarRecompensa(
        recompensaId: String,
        updates: Map<String, Any>
    ): Resource<Unit> {
        return try {
            firestore.collection(Recompensa.COLLECTION_NAME)
                .document(recompensaId)
                .update(updates)
                .await()

            Resource.Success(Unit)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al actualizar recompensa")
        }
    }

    /**
     * Reduce el stock de una recompensa después de un canje.
     *
     * Usa una transacción para evitar condiciones de carrera.
     */
    suspend fun reducirStock(recompensaId: String): Resource<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val docRef = firestore.collection(Recompensa.COLLECTION_NAME)
                    .document(recompensaId)

                val snapshot = transaction.get(docRef)
                val recompensa = snapshot.toObject(Recompensa::class.java)
                    ?: throw Exception("Recompensa no encontrada")

                // Verificar stock
                if (recompensa.cantidadDisponible == 0) {
                    throw Exception("Recompensa agotada")
                }

                // No reducir si es ilimitado
                if (recompensa.cantidadDisponible != Recompensa.STOCK_ILIMITADO) {
                    transaction.update(
                        docRef,
                        "cantidadDisponible",
                        recompensa.cantidadDisponible - 1
                    )
                }
            }.await()

            Resource.Success(Unit)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al reducir stock")
        }
    }

    /**
     * Aumenta el stock de una recompensa (al cancelar un canje).
     */
    suspend fun aumentarStock(recompensaId: String): Resource<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val docRef = firestore.collection(Recompensa.COLLECTION_NAME)
                    .document(recompensaId)

                val snapshot = transaction.get(docRef)
                val recompensa = snapshot.toObject(Recompensa::class.java)
                    ?: throw Exception("Recompensa no encontrada")

                // No aumentar si es ilimitado
                if (recompensa.cantidadDisponible != Recompensa.STOCK_ILIMITADO) {
                    transaction.update(
                        docRef,
                        "cantidadDisponible",
                        recompensa.cantidadDisponible + 1
                    )
                }
            }.await()

            Resource.Success(Unit)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al aumentar stock")
        }
    }

    /**
     * Desactiva una recompensa (soft delete).
     */
    suspend fun desactivarRecompensa(recompensaId: String): Resource<Unit> {
        return actualizarRecompensa(recompensaId, mapOf("activa" to false))
    }

    /**
     * Activa una recompensa desactivada.
     */
    suspend fun activarRecompensa(recompensaId: String): Resource<Unit> {
        return actualizarRecompensa(recompensaId, mapOf("activa" to true))
    }
}