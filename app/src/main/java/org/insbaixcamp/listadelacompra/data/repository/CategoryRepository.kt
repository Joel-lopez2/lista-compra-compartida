package org.insbaixcamp.listadelacompra.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.insbaixcamp.listadelacompra.data.model.Category

/**
 * Categorias de una lista: lists/{listId}/categories.
 */
class CategoryRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun categories(listId: String) =
        db.collection("lists").document(listId).collection("categories")

    /** Escucha en tiempo real las categorias de la lista. */
    fun observeCategories(listId: String): Flow<List<Category>> = callbackFlow {
        val registration = categories(listId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(snapshot.toObjects(Category::class.java))
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun createCategory(listId: String, name: String, icon: String) {
        categories(listId).add(mapOf("name" to name, "icon" to icon)).await()
    }

    suspend fun updateCategory(listId: String, categoryId: String, name: String, icon: String) {
        categories(listId).document(categoryId).update("name", name, "icon", icon).await()
    }

    /**
     * Elimina la categoria. Sus productos no se borran: pasan a "Sin categoria"
     * (categoryId = ""). Se hace antes de borrar la categoria.
     */
    suspend fun deleteCategory(listId: String, categoryId: String) {
        val products = db.collection("lists").document(listId).collection("products")
            .whereEqualTo("categoryId", categoryId)
            .get().await()

        products.documents.chunked(BATCH_SIZE).forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { batch.update(it.reference, "categoryId", "") }
            batch.commit().await()
        }
        categories(listId).document(categoryId).delete().await()
    }

    private companion object {
        const val BATCH_SIZE = 400
    }
}
