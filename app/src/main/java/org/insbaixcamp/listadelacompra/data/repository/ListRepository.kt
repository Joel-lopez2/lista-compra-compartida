package org.insbaixcamp.listadelacompra.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.insbaixcamp.listadelacompra.data.model.ProductStats
import org.insbaixcamp.listadelacompra.data.model.ShoppingList

/**
 * Acceso a las listas de la compra (coleccion "lists" de Firestore).
 *
 * Cada lista tiene un codigo de 6 caracteres. Para poder buscar una lista por
 * su codigo sin ser miembro, se guarda tambien en la coleccion "codes":
 *   codes/{codigo} = { listId, ownerId }
 */
class ListRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val lists = db.collection("lists")
    private val codes = db.collection("codes")

    /** uid del usuario con la sesion iniciada. */
    val currentUid: String?
        get() = auth.currentUser?.uid

    /**
     * Escucha en tiempo real todas las listas a las que tiene acceso el usuario
     * (las suyas y las que le han compartido).
     */
    fun observeLists(uid: String): Flow<List<ShoppingList>> = callbackFlow {
        val registration = lists
            .whereArrayContains("members", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(ShoppingList::class.java))
                }
            }
        awaitClose { registration.remove() }
    }

    /** Escucha una lista concreta. Emite null si la lista no existe (por ejemplo, si se ha eliminado). */
    fun observeList(listId: String): Flow<ShoppingList?> = callbackFlow {
        val registration = lists.document(listId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(ShoppingList::class.java))
            }
        awaitClose { registration.remove() }
    }

    /** Escucha en tiempo real cuantos productos tiene una lista y cuantos estan completados. */
    fun observeProductStats(listId: String): Flow<ProductStats> = callbackFlow {
        val registration = lists.document(listId).collection("products")
            .addSnapshotListener { snapshot, error ->
                // Si falla, mostramos 0 en lugar de romper toda la pantalla
                if (error != null || snapshot == null) {
                    trySend(ProductStats())
                } else {
                    val done = snapshot.documents.count { it.getBoolean("completed") == true }
                    trySend(ProductStats(total = snapshot.size(), done = done))
                }
            }
        awaitClose { registration.remove() }
    }

    /** Nombre del usuario (o su correo si no tiene nombre), o null si no existe. */
    suspend fun getUserName(uid: String): String? {
        val doc = db.collection("users").document(uid).get().await()
        if (!doc.exists()) return null
        val name = doc.getString("displayName").orEmpty()
        return name.ifBlank { doc.getString("email") }
    }

    /** Crea una lista (con su codigo) y el usuario actual es el propietario y el primer miembro. */
    suspend fun createList(name: String, icon: String) {
        val uid = currentUid ?: throw IllegalStateException("No hay sesion iniciada")
        val code = newUniqueCode()
        val listRef = lists.document()   // id nuevo, todavia sin guardar

        // La lista y su codigo se guardan a la vez (o las dos cosas o ninguna)
        val batch = db.batch()
        batch.set(
            listRef,
            hashMapOf(
                "name" to name,
                "icon" to icon,
                "code" to code,
                "ownerId" to uid,
                "members" to listOf(uid),
                "createdAt" to FieldValue.serverTimestamp()
            )
        )
        batch.set(codes.document(code), hashMapOf("listId" to listRef.id, "ownerId" to uid))
        batch.commit().await()
    }

    /** Cambia el nombre y el icono de una lista. */
    suspend fun updateList(listId: String, name: String, icon: String) {
        lists.document(listId).update("name", name, "icon", icon).await()
    }

    /** Las listas creadas antes de existir los codigos no tienen uno: se les crea aqui. */
    suspend fun createCodeForList(listId: String) {
        val uid = currentUid ?: throw IllegalStateException("No hay sesion iniciada")
        val code = newUniqueCode()
        val batch = db.batch()
        batch.set(codes.document(code), hashMapOf("listId" to listId, "ownerId" to uid))
        batch.update(lists.document(listId), "code", code)
        batch.commit().await()
    }

    /**
     * Elimina una lista junto con todas sus categorias, productos y su codigo.
     * Firestore no borra las subcolecciones solo, asi que se borran aqui con
     * batches (maximo 500 operaciones cada uno). La lista va en el ultimo.
     */
    suspend fun deleteList(listId: String) {
        val listRef = lists.document(listId)
        val code = listRef.get().await().getString("code").orEmpty()
        val categories = listRef.collection("categories").get().await()
        val products = listRef.collection("products").get().await()

        val refs = categories.documents.map { it.reference } +
            products.documents.map { it.reference } +
            (if (code.isNotBlank()) listOf(codes.document(code)) else emptyList()) +
            listRef

        refs.chunked(BATCH_SIZE).forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { batch.delete(it) }
            batch.commit().await()
        }
    }

    /** Genera un codigo que no use ninguna otra lista. */
    private suspend fun newUniqueCode(): String {
        repeat(10) {
            val code = generateCode()
            if (!codes.document(code).get().await().exists()) return code
        }
        throw IllegalStateException("No se ha podido generar un codigo")
    }

    /** Codigo de 6 caracteres en mayusculas, sin la letra O ni el numero 0 (se confunden). */
    private fun generateCode(): String {
        return (1..CODE_LENGTH).map { CODE_CHARS.random() }.joinToString("")
    }

    private companion object {
        const val BATCH_SIZE = 400
        const val CODE_LENGTH = 6
        const val CODE_CHARS = "ABCDEFGHIJKLMNPQRSTUVWXYZ123456789"
    }
}
