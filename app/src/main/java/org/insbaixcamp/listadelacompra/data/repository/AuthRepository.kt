package org.insbaixcamp.listadelacompra.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.insbaixcamp.listadelacompra.data.model.User

/**
 * Se encarga de la autenticacion (Firebase Authentication) y de guardar
 * el perfil del usuario en Firestore (coleccion "users").
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /** Usuario con la sesion iniciada, o null si no hay sesion. */
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Crea la cuenta y guarda el documento users/{uid}.
     * Si falla el guardado en Firestore se elimina la cuenta recien creada
     * para no dejar usuarios "a medias", y se relanza el error.
     */
    suspend fun register(name: String, email: String, password: String) {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw IllegalStateException("Usuario nulo tras el registro")

        val user = User(uid = firebaseUser.uid, email = email, displayName = name)
        try {
            db.collection("users").document(firebaseUser.uid).set(user).await()
        } catch (e: Exception) {
            firebaseUser.delete().await()
            throw e
        }
    }

    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    fun logout() {
        auth.signOut()
    }
}
