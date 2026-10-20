package org.insbaixcamp.listadelacompra.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Categoria de una lista. Se guarda en lists/{listId}/categories.
 * El nombre es obligatorio.
 */
data class Category(
    @DocumentId val id: String = "",
    val name: String = "",
    val icon: String = ""
)
