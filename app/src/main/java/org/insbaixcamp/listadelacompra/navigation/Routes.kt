package org.insbaixcamp.listadelacompra.navigation

import android.net.Uri

/** Rutas de navegacion de la aplicacion. */
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val LISTS = "lists"

    const val DETAIL_ARG_ID = "listId"
    const val DETAIL_ARG_NAME = "name"
    const val DETAIL = "detail/{$DETAIL_ARG_ID}?$DETAIL_ARG_NAME={$DETAIL_ARG_NAME}"

    /** Ruta concreta al detalle de una lista (el nombre se codifica por si lleva simbolos). */
    fun detail(listId: String, listName: String) =
        "detail/$listId?$DETAIL_ARG_NAME=${Uri.encode(listName)}"
}
