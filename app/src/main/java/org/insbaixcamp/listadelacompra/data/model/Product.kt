package org.insbaixcamp.listadelacompra.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Producto de una lista. Se guarda en lists/{listId}/products.
 * El nombre es obligatorio.
 *
 * - categoryId: vacio ("") significa "sin categoria".
 * - completed: true = comprado (se muestra tachado, pero no se elimina).
 * - createdAt: para mostrar los productos en el orden en que se anadieron.
 */
data class Product(
    @DocumentId val id: String = "",
    val name: String = "",
    val categoryId: String = "",
    val completed: Boolean = false,
    val quantity: String = "",
    val tags: List<String> = emptyList(),
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
