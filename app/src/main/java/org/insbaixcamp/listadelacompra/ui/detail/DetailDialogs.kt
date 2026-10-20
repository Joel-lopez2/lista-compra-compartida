package org.insbaixcamp.listadelacompra.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import org.insbaixcamp.listadelacompra.R
import org.insbaixcamp.listadelacompra.data.model.Category
import org.insbaixcamp.listadelacompra.data.model.Product
import java.text.Normalizer

private const val MAX_NAME_LENGTH = 40
private const val MAX_QUANTITY_LENGTH = 20
private const val MAX_ICON_LENGTH = 4

/** Dialogo para crear una categoria o renombrarla (el nombre es obligatorio, el icono no). */
@Composable
fun CategoryDialog(
    title: String,
    initialName: String,
    initialIcon: String,
    confirmLabel: String,
    onConfirm: (name: String, icon: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var icon by rememberSaveable { mutableStateOf(initialIcon) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= MAX_NAME_LENGTH) name = it },
                    label = { Text(stringResource(R.string.category_name_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = icon,
                    onValueChange = { if (it.length <= MAX_ICON_LENGTH) icon = it },
                    label = { Text(stringResource(R.string.category_icon_label)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, icon) },
                enabled = name.isNotBlank()
            ) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

/**
 * Dialogo para editar un producto: nombre (obligatorio), cantidad (opcional)
 * y categoria (campo desplegable con buscador). Incluye el boton de eliminar.
 */
@Composable
fun ProductDialog(
    product: Product,
    categories: List<Category>,
    onSave: (name: String, quantity: String, categoryId: String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf(product.name) }
    var quantity by rememberSaveable { mutableStateOf(product.quantity) }
    var categoryId by rememberSaveable { mutableStateOf(product.categoryId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_product)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= MAX_NAME_LENGTH) name = it },
                    label = { Text(stringResource(R.string.product_name_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { if (it.length <= MAX_QUANTITY_LENGTH) quantity = it },
                    label = { Text(stringResource(R.string.product_quantity_label)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                CategoryPicker(
                    categories = categories,
                    selectedId = categoryId,
                    onSelected = { categoryId = it }
                )
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text(
                        text = stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                TextButton(
                    onClick = { onSave(name, quantity, categoryId) },
                    enabled = name.isNotBlank()
                ) { Text(stringResource(R.string.save)) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

/** Texto de una categoria: icono (si lo tiene) + nombre. */
private fun categoryLabel(category: Category): String =
    listOf(category.icon, category.name).filter { it.isNotBlank() }.joinToString(" ")

/** Pasa a minusculas y quita tildes para que "platano" encuentre "Plátano". */
private fun normalize(text: String): String =
    Normalizer.normalize(text, Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        .lowercase()

/**
 * Campo desplegable de categoria: al pulsarlo se abre una ventana con un
 * buscador y la lista de categorias, mas comoda cuando hay muchas.
 */
@Composable
private fun CategoryPicker(
    categories: List<Category>,
    selectedId: String,
    onSelected: (String) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val selected = categories.firstOrNull { it.id == selectedId }
    val uncategorized = stringResource(R.string.uncategorized)

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected?.let { categoryLabel(it) } ?: uncategorized,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(stringResource(R.string.category_label)) },
            trailingIcon = { Text("\u25BE") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        // Capa transparente: un campo de texto de solo lectura no recibe el clic por si mismo
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showPicker = true }
        )
    }

    if (showPicker) {
        CategoryPickerDialog(
            categories = categories,
            selectedId = selectedId,
            onSelected = {
                showPicker = false
                onSelected(it)
            },
            onDismiss = { showPicker = false }
        )
    }
}

@Composable
private fun CategoryPickerDialog(
    categories: List<Category>,
    selectedId: String,
    onSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val uncategorized = stringResource(R.string.uncategorized)

    // Opciones: "Sin categoria" (id vacio) y las categorias, filtradas por el buscador
    val options = buildList {
        add("" to uncategorized)
        categories.forEach { add(it.id to categoryLabel(it)) }
    }.filter { (_, label) -> normalize(label).contains(normalize(query.trim())) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pick_category_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(stringResource(R.string.search_category_hint)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (options.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_results),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                        items(options, key = { it.first }) { (id, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelected(id) }
                                    .padding(horizontal = 4.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (id == selectedId) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (id == selectedId) {
                                    Text("\u2713", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
