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
 */
class ListRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val lists = db.collection("lists")

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

    /** Crea una lista: el usuario actual es el propietario y el primer miembro. */
    suspend fun createList(name: String) {
        val uid = currentUid ?: throw IllegalStateException("No hay sesion iniciada")
        val data = hashMapOf(
            "name" to name,
            "ownerId" to uid,
            "members" to listOf(uid),
            "createdAt" to FieldValue.serverTimestamp()
        )
        lists.add(data).await()
    }

    suspend fun renameList(listId: String, name: String) {
        lists.document(listId).update("name", name).await()
    }

    /**
     * Elimina una lista junto con todas sus categorias y productos.
     * Firestore no borra las subcolecciones solo, asi que se borran aqui con
     * batches (maximo 500 operaciones cada uno). La lista va en el ultimo.
     */
    suspend fun deleteList(listId: String) {
        val listRef = lists.document(listId)
        val categories = listRef.collection("categories").get().await()
        val products = listRef.collection("products").get().await()

        val refs = categories.documents.map { it.reference } +
            products.documents.map { it.reference } +
            listRef

        refs.chunked(BATCH_SIZE).forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { batch.delete(it) }
            batch.commit().await()
        }
    }

    private companion object {
        const val BATCH_SIZE = 400
    }
}
