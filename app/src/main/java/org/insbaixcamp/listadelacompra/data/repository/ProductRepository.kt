package org.insbaixcamp.listadelacompra.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.insbaixcamp.listadelacompra.data.model.Product

/**
 * Productos de una lista: lists/{listId}/products.
 */
class ProductRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun products(listId: String) =
        db.collection("lists").document(listId).collection("products")

    /** Escucha en tiempo real los productos de la lista. */
    fun observeProducts(listId: String): Flow<List<Product>> = callbackFlow {
        val registration = products(listId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(snapshot.toObjects(Product::class.java))
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun addProduct(listId: String, name: String, categoryId: String = "") {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("No hay sesion iniciada")
        val data = hashMapOf(
            "name" to name,
            "categoryId" to categoryId,
            "completed" to false,
            "quantity" to "",
            "tags" to emptyList<String>(),
            "createdBy" to uid,
            "createdAt" to FieldValue.serverTimestamp()
        )
        products(listId).add(data).await()
    }

    suspend fun updateProduct(
        listId: String,
        productId: String,
        name: String,
        quantity: String,
        categoryId: String
    ) {
        products(listId).document(productId)
            .update("name", name, "quantity", quantity, "categoryId", categoryId)
            .await()
    }

    /** Marca o desmarca un producto como completado (no se elimina). */
    suspend fun setCompleted(listId: String, productId: String, completed: Boolean) {
        products(listId).document(productId).update("completed", completed).await()
    }

    suspend fun deleteProduct(listId: String, productId: String) {
        products(listId).document(productId).delete().await()
    }
}
