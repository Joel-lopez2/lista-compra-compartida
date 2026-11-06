package org.insbaixcamp.listadelacompra.ui.share

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import org.insbaixcamp.listadelacompra.R
import org.insbaixcamp.listadelacompra.data.model.User
import org.insbaixcamp.listadelacompra.ui.components.ConfirmDialog

/**
 * Dialogo de compartir y miembros de una lista.
 * - Propietario: ve el codigo para compartir la lista y puede quitar miembros.
 * - Invitado: ve los miembros y puede salir de la lista.
 *
 * onLeft se llama cuando el usuario ya no tiene acceso a la lista
 * (ha salido, le han quitado o la lista se ha eliminado).
 */
@Composable
fun ShareDialog(
    listId: String,
    onDismiss: () -> Unit,
    onLeft: () -> Unit
) {
    val viewModel: ShareViewModel = viewModel(
        key = "share_$listId",
        factory = viewModelFactory { initializer { ShareViewModel(listId) } }
    )
    val uiState by viewModel.uiState.collectAsState()

    var memberToRemove by remember { mutableStateOf<User?>(null) }
    var showLeaveConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.left) {
        if (uiState.left) onLeft()
    }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (uiState.isOwner) R.string.share_list else R.string.members_title
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Solo el propietario ve el codigo para compartir la lista
                if (uiState.isOwner && uiState.code.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.share_code_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = uiState.code,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 6.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = stringResource(R.string.share_code_help),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { copyCode(context, uiState.code) },
                            modifier = Modifier.weight(1f)
                        ) { Text(stringResource(R.string.copy_code)) }
                        Button(
                            onClick = { sendCode(context, uiState.listName, uiState.code) },
                            modifier = Modifier.weight(1f)
                        ) { Text(stringResource(R.string.send_code)) }
                    }
                    HorizontalDivider()
                }

                uiState.errorRes?.let {
                    Text(
                        text = stringResource(it),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = stringResource(R.string.members_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                } else {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 240.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        uiState.members.forEach { member ->
                            MemberRow(
                                member = member,
                                isOwnerOfList = member.uid == uiState.ownerId,
                                isMe = member.uid == uiState.currentUid,
                                canRemove = uiState.isOwner && member.uid != uiState.ownerId,
                                enabled = !uiState.isWorking,
                                onRemove = { memberToRemove = member }
                            )
                        }
                    }
                }

                if (!uiState.isOwner && !uiState.isLoading) {
                    TextButton(
                        onClick = { showLeaveConfirm = true },
                        enabled = !uiState.isWorking
                    ) {
                        Text(
                            text = stringResource(R.string.leave_list),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        }
    )

    memberToRemove?.let { member ->
        ConfirmDialog(
            title = stringResource(R.string.remove_member_title),
            message = stringResource(
                R.string.remove_member_message,
                ShareViewModel.displayName(member),
                uiState.listName
            ),
            confirmLabel = stringResource(R.string.remove),
            onConfirm = {
                memberToRemove = null
                viewModel.removeMember(member)
            },
            onDismiss = { memberToRemove = null }
        )
    }

    if (showLeaveConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.leave_list),
            message = stringResource(R.string.leave_list_message, uiState.listName),
            confirmLabel = stringResource(R.string.leave),
            onConfirm = {
                showLeaveConfirm = false
                viewModel.leave()
            },
            onDismiss = { showLeaveConfirm = false }
        )
    }
}

@Composable
private fun MemberRow(
    member: User,
    isOwnerOfList: Boolean,
    isMe: Boolean,
    canRemove: Boolean,
    enabled: Boolean,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(vertical = 6.dp)) {
            val name = ShareViewModel.displayName(member).ifBlank { stringResource(R.string.unknown_user) }
            val suffix = buildString {
                if (isOwnerOfList) append(" · ").append(stringResource(R.string.owner_tag))
                if (isMe) append(" · ").append(stringResource(R.string.you_tag))
            }
            Text(text = name + suffix, style = MaterialTheme.typography.bodyLarge)
            if (member.displayName.isNotBlank() && member.email.isNotBlank()) {
                Text(
                    text = member.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (canRemove) {
            TextButton(onClick = onRemove, enabled = enabled) {
                Text(
                    text = stringResource(R.string.remove),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/** Copia el codigo al portapapeles. */
private fun copyCode(context: Context, code: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("codigo", code))
    Toast.makeText(context, R.string.code_copied, Toast.LENGTH_SHORT).show()
}

/** Abre el menu de Android para enviar el codigo por WhatsApp, correo, etc. */
private fun sendCode(context: Context, listName: String, code: String) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_code_text, listName, code))
    context.startActivity(Intent.createChooser(intent, null))
}
