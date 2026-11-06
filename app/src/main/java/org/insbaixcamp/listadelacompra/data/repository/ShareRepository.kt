package org.insbaixcamp.listadelacompra.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import org.insbaixcamp.listadelacompra.data.model.User

/** Resultado de intentar unirse a una lista con un codigo. */
enum class JoinResult { JOINED, ALREADY_MEMBER, NOT_FOUND }

/**
 * Compartir listas: unirse con un codigo y anadir o quitar miembros
 * (campo "members" de la lista).
 */
class ShareRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val lists = db.collection("lists")
    private val users = db.collection("users")

    /** Datos de un usuario por su uid, o null si no existe. */
    suspend fun getUser(uid: String): User? {
        val doc = users.document(uid).get().await()
        if (!doc.exists()) return null
        return doc.toObject(User::class.java)?.copy(uid = doc.id)
    }

    /** El usuario actual se une a la lista que tiene ese codigo. */
    suspend fun joinWithCode(code: String): JoinResult {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("No hay sesion iniciada")

        // 1. Buscar el codigo
        val codeDoc = db.collection("codes").document(code).get().await()
        val listId = codeDoc.getString("listId")
        if (!codeDoc.exists() || listId == null) return JoinResult.NOT_FOUND

        // 2. Si podemos leer la lista es que ya somos miembros (si no, Firestore da error de permisos)
        val alreadyMember = try {
            lists.document(listId).get().await()
            true
        } catch (e: FirebaseFirestoreException) {
            false
        }
        if (alreadyMember) return JoinResult.ALREADY_MEMBER

        // 3. Anadirnos como miembros
        addMember(listId, uid)
        return JoinResult.JOINED
    }

    /** Da acceso a la lista (arrayUnion evita duplicados). */
    suspend fun addMember(listId: String, uid: String) {
        lists.document(listId).update("members", FieldValue.arrayUnion(uid)).await()
    }

    /** Quita el acceso a la lista. Sirve para que el propietario quite a alguien o para salir uno mismo. */
    suspend fun removeMember(listId: String, uid: String) {
        lists.document(listId).update("members", FieldValue.arrayRemove(uid)).await()
    }
}
