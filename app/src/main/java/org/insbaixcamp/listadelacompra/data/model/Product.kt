package org.insbaixcamp.listadelacompra.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Producto de una lista. Se guarda en lists/{listId}/products.
 * El nombre es obligatorio.
 *
 * - categoryId: vacio ("") significa "sin categoria".
 * - completed: true = comprado (se muestra tachado, pero no se elimina).
 */
data class Product(
    @DocumentId val id: String = "",
    val name: String = "",
    val categoryId: String = "",
    val completed: Boolean = false,
    val quantity: String = "",
    val tags: List<String> = emptyList(),
    val createdBy: String = ""
)
