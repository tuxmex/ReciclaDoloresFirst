package mx.edu.utng.arg.recicladoloresfirst.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import mx.edu.utng.arg.recicladoloresfirst.data.model.Usuario
import mx.edu.utng.arg.recicladoloresfirst.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio que maneja la autenticación de usuarios.
 *
 * Este repositorio es como "el portero" de la app:
 * - Deja entrar a usuarios registrados (login)
 * - Registra nuevos usuarios (registro)
 * - Verifica credenciales
 * - Maneja cierre de sesión
 *
 * @Inject: Hilt automáticamente pasa FirebaseAuth y Firestore
 * @Singleton: Solo se crea una instancia de este repositorio
 */
@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    /**
     * Usuario actual autenticado (null si no hay sesión).
     */
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * ID del usuario actual.
     */
    val currentUserId: String?
        get() = auth.currentUser?.uid

    /**
     * Verifica si hay un usuario autenticado.
     */
    fun isUserAuthenticated(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Registra un nuevo usuario con email y contraseña.
     *
     * Flujo:
     * 1. Crear cuenta en Firebase Auth
     * 2. Crear documento de usuario en Firestore
     * 3. Retornar resultado
     *
     * @param email Correo electrónico
     * @param password Contraseña (mínimo 6 caracteres)
     * @param nombre Nombre completo
     * @param telefono Teléfono de contacto
     *
     * @return Resource<Usuario> con el usuario creado o error
     */
    suspend fun registrarUsuario(
        email: String,
        password: String,
        nombre: String,
        telefono: String
    ): Resource<Usuario> {
        return try {
            // Paso 1: Crear usuario en Firebase Auth
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return Resource.Error("Error al crear usuario")

            // Paso 2: Crear perfil de usuario en Firestore
            val usuario = Usuario(
                id = firebaseUser.uid,
                email = email,
                nombre = nombre,
                telefono = telefono,
                puntos = 0,
                rol = Usuario.ROL_CIUDADANO,
                activo = true
            )

            // Guardar en Firestore
            firestore.collection(Usuario.COLLECTION_NAME)
                .document(firebaseUser.uid)
                .set(usuario)
                .await()

            Resource.Success(usuario)

        } catch (e: Exception) {
            Resource.Error(
                message = when {
                    e.message?.contains("email address is already") == true ->
                        "Este correo ya está registrado"
                    e.message?.contains("network") == true ->
                        "Error de conexión. Verifica tu internet"
                    e.message?.contains("password") == true ->
                        "La contraseña debe tener al menos 6 caracteres"
                    else -> e.message ?: "Error desconocido al registrar"
                }
            )
        }
    }

    /**
     * Inicia sesión con email y contraseña.
     *
     * @param email Correo electrónico
     * @param password Contraseña
     *
     * @return Resource<Usuario> con datos del usuario o error
     */
    suspend fun iniciarSesion(
        email: String,
        password: String
    ): Resource<Usuario> {
        return try {
            // Autenticar con Firebase Auth
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return Resource.Error("Error al iniciar sesión")

            // Obtener datos del usuario desde Firestore
            val usuarioDoc = firestore.collection(Usuario.COLLECTION_NAME)
                .document(firebaseUser.uid)
                .get()
                .await()

            val usuario = usuarioDoc.toObject(Usuario::class.java)
                ?: return Resource.Error("Usuario no encontrado en la base de datos")

            // Verificar si el usuario está activo
            if (!usuario.activo) {
                auth.signOut() // Cerrar sesión inmediatamente
                return Resource.Error("Tu cuenta ha sido desactivada. Contacta al administrador")
            }

            Resource.Success(usuario)

        } catch (e: Exception) {
            Resource.Error(
                message = when {
                    e.message?.contains("no user record") == true ||
                            e.message?.contains("password is invalid") == true ->
                        "Correo o contraseña incorrectos"
                    e.message?.contains("network") == true ->
                        "Error de conexión. Verifica tu internet"
                    else -> e.message ?: "Error desconocido al iniciar sesión"
                }
            )
        }
    }

    /**
     * Inicia sesión con Google.
     *
     * @param idToken Token de Google Sign-In
     *
     * @return Resource<Usuario> con datos del usuario
     */
    suspend fun iniciarSesionConGoogle(idToken: String): Resource<Usuario> {
        return try {
            // Crear credencial de Google
            val credential = GoogleAuthProvider.getCredential(idToken, null)

            // Autenticar con Firebase
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user
                ?: return Resource.Error("Error al iniciar sesión con Google")

            // Verificar si el usuario ya existe en Firestore
            val usuarioDoc = firestore.collection(Usuario.COLLECTION_NAME)
                .document(firebaseUser.uid)
                .get()
                .await()

            val usuario = if (usuarioDoc.exists()) {
                // Usuario existente
                usuarioDoc.toObject(Usuario::class.java)!!
            } else {
                // Nuevo usuario - crear perfil
                val nuevoUsuario = Usuario(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    nombre = firebaseUser.displayName ?: "",
                    telefono = firebaseUser.phoneNumber ?: "",
                    puntos = 0,
                    rol = Usuario.ROL_CIUDADANO,
                    activo = true,
                    fotoPerfilUrl = firebaseUser.photoUrl?.toString()
                )

                firestore.collection(Usuario.COLLECTION_NAME)
                    .document(firebaseUser.uid)
                    .set(nuevoUsuario)
                    .await()

                nuevoUsuario
            }

            Resource.Success(usuario)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al iniciar sesión con Google")
        }
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    fun cerrarSesion() {
        auth.signOut()
    }

    /**
     * Envía email para restablecer contraseña.
     *
     * @param email Correo electrónico
     *
     * @return Resource<Unit> éxito o error
     */
    suspend fun enviarEmailRecuperacion(email: String): Resource<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(
                message = when {
                    e.message?.contains("no user record") == true ->
                        "No existe una cuenta con este correo"
                    else -> e.message ?: "Error al enviar email de recuperación"
                }
            )
        }
    }

    /**
     * Actualiza la contraseña del usuario actual.
     *
     * @param newPassword Nueva contraseña
     */
    suspend fun actualizarPassword(newPassword: String): Resource<Unit> {
        return try {
            val user = auth.currentUser
                ?: return Resource.Error("No hay usuario autenticado")

            user.updatePassword(newPassword).await()
            Resource.Success(Unit)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al actualizar contraseña")
        }
    }
}