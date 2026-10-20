package org.insbaixcamp.listadelacompra.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Lista de la compra. Se guarda en la coleccion "lists".
 *
 * - ownerId: uid del usuario que la ha creado (unico que puede borrarla).
 * - members: uids de todos los usuarios con acceso (incluye al propietario).
 *   Asi una lista se comparte anadiendo el uid de otra persona.
 */
data class ShoppingList(
    @DocumentId val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val members: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)
