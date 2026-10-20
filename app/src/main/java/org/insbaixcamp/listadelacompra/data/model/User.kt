package org.insbaixcamp.listadelacompra.data.model

/**
 * Usuario de la aplicacion. Se guarda en la coleccion "users",
 * usando como id del documento el uid de Firebase Authentication.
 */
data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = ""
)
