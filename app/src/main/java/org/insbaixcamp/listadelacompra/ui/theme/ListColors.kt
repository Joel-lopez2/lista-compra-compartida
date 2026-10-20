package org.insbaixcamp.listadelacompra.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** Color y emoji que identifican visualmente a una lista. */
data class ListStyle(
    val color: Color,
    val emoji: String
) {
    /** Color del texto o los iconos que van encima de [color] (oscuro o blanco segun el brillo). */
    val onColor: Color
        get() = if (color.luminance() > 0.45f) Color(0xFF2A211B) else Color.White
}

/**
 * Cada lista tiene un estilo propio que se calcula a partir de su id,
 * asi todos los miembros la ven igual sin guardar nada extra en Firestore.
 */
object ListColors {

    private val styles = listOf(
        ListStyle(Color(0xFFFF6B57), "🛒"), // coral    - carrito
        ListStyle(Color(0xFFFF9F1C), "🥐"), // naranja  - cruasan
        ListStyle(Color(0xFFFFC233), "🧀"), // amarillo - queso
        ListStyle(Color(0xFF2EC4B6), "🥑"), // turquesa - aguacate
        ListStyle(Color(0xFF4D8DFF), "🥛"), // azul     - leche
        ListStyle(Color(0xFF8B5CF6), "🍇"), // violeta  - uvas
        ListStyle(Color(0xFFF472B6), "🍓"), // rosa     - fresa
        ListStyle(Color(0xFF84CC16), "🥦")  // lima     - brocoli
    )

    fun forId(listId: String): ListStyle =
        styles[(listId.hashCode() and Int.MAX_VALUE) % styles.size]
}
