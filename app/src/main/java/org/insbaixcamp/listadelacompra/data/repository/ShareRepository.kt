package org.insbaixcamp.listadelacompra.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.insbaixcamp.listadelacompra.data.model.User

/**
 * Compartir listas: anadir y quitar miembros (campo "members" de la lista)
 * y buscar usuarios por correo en la coleccion "users".
 */
class ShareRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val lists = db.collection("lists")
    private val users = db.collection("users")

    /** Busca un usuario por su correo (guardado siempre en minusculas). Null si no esta registrado. */
    suspend fun findUserByEmail(email: String): User? {
        val snapshot = users.whereEqualTo("email", email).limit(1).get().await()
        val doc = snapshot.documents.firstOrNull() ?: return null
        return doc.toObject(User::class.java)?.copy(uid = doc.id)
    }

    /** Datos de un usuario por su uid, o null si no existe. */
    suspend fun getUser(uid: String): User? {
        val doc = users.document(uid).get().await()
        if (!doc.exists()) return null
        return doc.toObject(User::class.java)?.copy(uid = doc.id)
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
