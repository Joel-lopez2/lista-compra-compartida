package org.insbaixcamp.listadelacompra.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.insbaixcamp.listadelacompra.R

/** Emojis entre los que se puede elegir para una lista o una categoria. */
val ICONS = listOf(
    "🛒", "🍎", "🥦", "🥕", "🍌", "🍇",
    "🥛", "🧀", "🥚", "🍞", "🥩", "🐟",
    "🍝", "🍕", "🍫", "☕", "🍺", "🧁",
    "🧴", "🧻", "💊", "🏠", "🐶", "🎉"
)

/**
 * Selector de icono: una opcion "Sin icono" y una cuadricula de emojis.
 * selected = "" significa que no hay icono.
 */
@Composable
fun IconPicker(
    selected: String,
    onSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.icon_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Opcion "Sin icono"
        val noIconSelected = selected.isEmpty()
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(
                    if (noIconSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .clickable { onSelected("") }
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(stringResource(R.string.no_icon), style = MaterialTheme.typography.labelLarge)
        }

        // Emojis, 6 por fila
        ICONS.chunked(6).forEach { rowIcons ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                rowIcons.forEach { icon ->
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (icon == selected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { onSelected(icon) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(icon, fontSize = 22.sp)
                    }
                }
            }
        }
    }
}
