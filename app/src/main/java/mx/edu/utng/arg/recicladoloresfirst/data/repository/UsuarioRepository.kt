package mx.edu.utng.arg.recicladoloresfirst.data.repository

import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import mx.edu.utng.arg.recicladoloresfirst.data.model.Usuario
import mx.edu.utng.arg.recicladoloresfirst.util.Resource
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para operaciones relacionadas con usuarios.
 *
 * Responsabilidades:
 * - Obtener datos de usuario
 * - Actualizar perfil
 * - Subir foto de perfil
 * - Administrar puntos
 */
@Singleton
class UsuarioRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    /**
     * Obtiene un usuario por su ID.
     *
     * @param usuarioId ID del usuario
     * @return Resource<Usuario>
     */
    suspend fun obtenerUsuario(usuarioId: String): Resource<Usuario> {
        return try {
            val doc = firestore.collection(Usuario.COLLECTION_NAME)
                .document(usuarioId)
                .get()
                .await()

            val usuario = doc.toObject(Usuario::class.java)
                ?: return Resource.Error("Usuario no encontrado")

            Resource.Success(usuario)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener usuario")
        }
    }

    /**
     * Obtiene un Flow que emite cambios en tiempo real del usuario.
     *
     * Esto es útil para que la UI se actualice automáticamente
     * cuando cambian los puntos u otros datos del usuario.
     *
     * Ejemplo de uso en ViewModel:
     * ```
     * val usuario: StateFlow<Resource<Usuario>> = repository
     *     .obtenerUsuarioFlow(userId)
     *     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Resource.Loading())
     * ```
     */
    fun obtenerUsuarioFlow(usuarioId: String): Flow<Resource<Usuario>> = callbackFlow {
        // Enviar estado inicial
        trySend(Resource.Loading())

        // Listener para cambios en tiempo real
        val listener: ListenerRegistration = firestore
            .collection(Usuario.COLLECTION_NAME)
            .document(usuarioId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error desconocido"))
                    return@addSnapshotListener
                }

                val usuario = snapshot?.toObject(Usuario::class.java)
                if (usuario != null) {
                    trySend(Resource.Success(usuario))
                } else {
                    trySend(Resource.Error("Usuario no encontrado"))
                }
            }

        // Cancelar listener cuando se cancele el Flow
        awaitClose { listener.remove() }
    }

    /**
     * Actualiza el perfil del usuario.
     *
     * @param usuarioId ID del usuario
     * @param updates Mapa con los campos a actualizar
     *
     * Ejemplo:
     * ```
     * repository.actualizarPerfil(
     *     userId,
     *     mapOf(
     *         "nombre" to "Juan Pérez",
     *         "telefono" to "4771234567"
     *     )
     * )
     * ```
     */
    suspend fun actualizarPerfil(
        usuarioId: String,
        updates: Map<String, Any>
    ): Resource<Unit> {
        return try {
            firestore.collection(
                Usuario.COLLECTION_NAME)
                .document(usuarioId)
                .update(updates)
                .await()

            Resource.Success(Unit)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al actualizar perfil")
        }
    }

    /**
     * Sube una foto de perfil al Storage y actualiza la URL en Firestore.
     *
     * Flujo:
     * 1. Subir imagen a Firebase Storage
     * 2. Obtener URL de descarga
     * 3. Actualizar campo fotoPerfilUrl en Firestore
     *
     * @param usuarioId ID del usuario
     * @param imageFile Archivo de imagen
     *
     * @return Resource<String> con la URL de la imagen o error
     */
    suspend fun subirFotoPerfil(
        usuarioId: String,
        imageFile: File
    ): Resource<String> {
        return try {
            // Referencia en Storage: profiles/{usuarioId}/perfil.jpg
            val storageRef = storage.reference
                .child("profiles")
                .child(usuarioId)
                .child("perfil.jpg")

            // Subir archivo
            storageRef.putFile(Uri.fromFile(imageFile)).await()

            // Obtener URL de descarga
            val downloadUrl = storageRef.downloadUrl.await().toString()

            // Actualizar Firestore
            firestore.collection(Usuario.COLLECTION_NAME)
                .document(usuarioId)
                .update("fotoPerfilUrl", downloadUrl)
                .await()

            Resource.Success(downloadUrl)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al subir foto de perfil")
        }
    }

    /**
     * Suma puntos al usuario.
     *
     * Usa FieldValue.increment() para evitar condiciones de carrera.
     *
     * @param usuarioId ID del usuario
     * @param puntos Cantidad de puntos a sumar
     */
    suspend fun sumarPuntos(
        usuarioId: String,
        puntos: Int
    ): Resource<Unit> {
        return try {
            firestore.collection(Usuario.COLLECTION_NAME)
                .document(usuarioId)
                .update("puntos", FieldValue.increment(puntos.toLong()))
                .await()

            Resource.Success(Unit)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al sumar puntos")
        }
    }

    /**
     * Resta puntos al usuario (para canjes).
     *
     * IMPORTANTE: No verifica si el usuario tiene suficientes puntos.
     * Esta verificación debe hacerse ANTES de llamar este método.
     *
     * @param usuarioId ID del usuario
     * @param puntos Cantidad de puntos a restar
     */
    suspend fun restarPuntos(
        usuarioId: String,
        puntos: Int
    ): Resource<Unit> {
        return try {
            firestore.collection(Usuario.COLLECTION_NAME)
                .document(usuarioId)
                .update("puntos", FieldValue.increment(-puntos.toLong()))
                .await()

            Resource.Success(Unit)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al restar puntos")
        }
    }

    /**
     * Obtiene el ranking de usuarios por puntos (top 10).
     *
     * Útil para mostrar una tabla de líderes.
     */
    suspend fun obtenerRankingUsuarios(limite: Int = 10): Resource<List<Usuario>> {
        return try {
            val snapshot = firestore.collection(Usuario.COLLECTION_NAME)
                .whereEqualTo("rol", Usuario.ROL_CIUDADANO) // Solo ciudadanos
                .whereEqualTo("activo", true)
                .orderBy("puntos", Query.Direction.DESCENDING)
                .limit(limite.toLong())
                .get()
                .await()

            val usuarios = snapshot.toObjects(Usuario::class.java)
            Resource.Success(usuarios)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener ranking")
        }
    }

    /**
     * Busca usuarios por nombre (para admins).
     *
     * Nota: Firestore no soporta búsquedas tipo LIKE nativas.
     * Esta implementación busca coincidencias exactas al inicio del nombre.
     * Para búsquedas más avanzadas, considera usar Algolia o ElasticSearch.
     */
    suspend fun buscarUsuarios(query: String): Resource<List<Usuario>> {
        return try {
            val snapshot = firestore.collection(Usuario.COLLECTION_NAME)
                .orderBy("nombre")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(20)
                .get()
                .await()

            val usuarios = snapshot.toObjects(Usuario::class.java)
            Resource.Success(usuarios)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al buscar usuarios")
        }
    }

}
