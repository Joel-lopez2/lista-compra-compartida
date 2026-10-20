package org.insbaixcamp.listadelacompra.ui.share

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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
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
 * - Propietario: puede invitar por correo y quitar miembros.
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

    var email by rememberSaveable { mutableStateOf("") }
    var memberToRemove by remember { mutableStateOf<User?>(null) }
    var showLeaveConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.left) {
        if (uiState.left) onLeft()
    }

    // Al compartir con exito se vacia el campo de correo
    LaunchedEffect(uiState.sharedWith) {
        if (uiState.sharedWith != null) email = ""
    }

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
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (uiState.isOwner) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; viewModel.clearFeedback() },
                        label = { Text(stringResource(R.string.share_email_label)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { viewModel.share(email) }),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.share(email) },
                        enabled = email.isNotBlank() && !uiState.isWorking,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.share_button)) }

                    uiState.errorRes?.let {
                        Text(
                            text = stringResource(it),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    uiState.sharedWith?.let {
                        Text(
                            text = stringResource(R.string.share_success, it),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    HorizontalDivider()
                } else {
                    uiState.errorRes?.let {
                        Text(
                            text = stringResource(it),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
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
