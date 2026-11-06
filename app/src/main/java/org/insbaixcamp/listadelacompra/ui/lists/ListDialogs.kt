package org.insbaixcamp.listadelacompra.ui.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import org.insbaixcamp.listadelacompra.R
import org.insbaixcamp.listadelacompra.ui.components.IconPicker

/** Longitud maxima del nombre de una lista. */
const val MAX_LIST_NAME_LENGTH = 40

/** Longitud del codigo para unirse a una lista. */
const val CODE_LENGTH = 6

/**
 * Dialogo para crear o editar una lista.
 * El nombre es obligatorio; el icono es opcional ("" = sin icono).
 */
@Composable
fun ListDialog(
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
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= MAX_LIST_NAME_LENGTH) name = it },
                    label = { Text(stringResource(R.string.list_name_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                IconPicker(selected = icon, onSelected = { icon = it })
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

/** Dialogo para unirse a una lista escribiendo el codigo de 6 caracteres. */
@Composable
fun JoinListDialog(
    onConfirm: (code: String) -> Unit,
    onDismiss: () -> Unit
) {
    var code by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.join_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.join_help),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { text ->
                        // Solo letras y numeros, en mayusculas, sin la O ni el 0
                        code = text.uppercase()
                            .filter { it.isLetterOrDigit() && it != 'O' && it != '0' }
                            .take(CODE_LENGTH)
                    },
                    label = { Text(stringResource(R.string.join_code_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(code) },
                enabled = code.length == CODE_LENGTH
            ) { Text(stringResource(R.string.join_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

/** Confirmacion antes de eliminar una lista. */
@Composable
fun DeleteListDialog(
    listName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_list_title)) },
        text = { Text(stringResource(R.string.delete_list_message, listName)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.delete)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
