package org.insbaixcamp.listadelacompra.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** Color que identifica visualmente a una lista. */
data class ListStyle(val color: Color) {
    /** Color del texto o los iconos que van encima de [color] (oscuro o blanco segun el brillo). */
    val onColor: Color
        get() = if (color.luminance() > 0.45f) Color(0xFF2A211B) else Color.White
}

/**
 * Cada lista tiene un color que se calcula a partir de su id,
 * asi todos los miembros la ven igual sin guardar nada extra en Firestore.
 */
object ListColors {

    private val colors = listOf(
        Color(0xFFFF6B57), // coral
        Color(0xFFFF9F1C), // naranja
        Color(0xFFFFC233), // amarillo
        Color(0xFF2EC4B6), // turquesa
        Color(0xFF4D8DFF), // azul
        Color(0xFF8B5CF6), // violeta
        Color(0xFFF472B6), // rosa
        Color(0xFF84CC16)  // lima
    )

    fun forId(listId: String): ListStyle {
        val index = (listId.hashCode() and Int.MAX_VALUE) % colors.size
        return ListStyle(colors[index])
    }
}
