package org.insbaixcamp.listadelacompra.ui.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.insbaixcamp.listadelacompra.R
import org.insbaixcamp.listadelacompra.data.model.ShoppingList
import org.insbaixcamp.listadelacompra.ui.components.ConfirmDialog
import org.insbaixcamp.listadelacompra.ui.share.ShareDialog
import org.insbaixcamp.listadelacompra.ui.theme.ListColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    onLogout: () -> Unit,
    onOpenList: (ShoppingList) -> Unit,
    viewModel: ListsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var showJoinDialog by rememberSaveable { mutableStateOf(false) }
    var listToRename by remember { mutableStateOf<ShoppingList?>(null) }
    var listToDelete by remember { mutableStateOf<ShoppingList?>(null) }
    var listToShare by remember { mutableStateOf<ShoppingList?>(null) }
    var listToLeave by remember { mutableStateOf<ShoppingList?>(null) }

    // Mensajes puntuales (por ejemplo, "no se ha podido completar la accion")
    val message = uiState.messageRes?.let { stringResource(it) }
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.messageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (uiState.userName.isNotBlank()) {
                                stringResource(R.string.greeting, uiState.userName)
                            } else {
                                stringResource(R.string.greeting_no_name)
                            },
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = stringResource(R.string.lists_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { showJoinDialog = true }) {
                        Text(stringResource(R.string.join_button))
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_logout),
                            contentDescription = stringResource(R.string.logout)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (!uiState.isLoading && uiState.errorRes == null) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateDialog = true },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_add),
                            contentDescription = null
                        )
                    },
                    text = { Text(stringResource(R.string.new_list)) },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when {
            uiState.isLoading -> {
                Box(contentModifier, contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorRes != null -> {
                CenteredMessage(
                    emoji = "\uD83D\uDE15",
                    text = stringResource(uiState.errorRes!!),
                    modifier = contentModifier
                ) {
                    Button(onClick = { viewModel.load() }) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }

            uiState.myLists.isEmpty() && uiState.sharedLists.isEmpty() -> {
                CenteredMessage(
                    emoji = "\uD83D\uDECD\uFE0F",
                    text = stringResource(R.string.lists_empty),
                    modifier = contentModifier
                )
            }

            else -> {
                LazyColumn(
                    modifier = contentModifier,
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (uiState.myLists.isNotEmpty()) {
                        item(key = "header_mine") { SectionHeader(stringResource(R.string.section_my_lists)) }
                        items(uiState.myLists, key = { it.list.id }) { item ->
                            ListCard(
                                item = item,
                                isOwner = true,
                                onClick = { onOpenList(item.list) },
                                onShare = { listToShare = item.list },
                                onRename = { listToRename = item.list },
                                onDelete = { listToDelete = item.list },
                                onLeave = {}
                            )
                        }
                    }
                    if (uiState.sharedLists.isNotEmpty()) {
                        item(key = "header_shared") { SectionHeader(stringResource(R.string.section_shared)) }
                        items(uiState.sharedLists, key = { it.list.id }) { item ->
                            ListCard(
                                item = item,
                                isOwner = false,
                                onClick = { onOpenList(item.list) },
                                onShare = { listToShare = item.list },
                                onRename = {},
                                onDelete = {},
                                onLeave = { listToLeave = item.list }
                            )
                        }
                    }
                }
            }
        }
    }

    // ---- Dialogos ----

    if (showCreateDialog) {
        ListDialog(
            title = stringResource(R.string.new_list),
            initialName = "",
            initialIcon = "",
            confirmLabel = stringResource(R.string.create),
            onConfirm = { name, icon ->
                showCreateDialog = false
                viewModel.createList(name, icon)
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    if (showJoinDialog) {
        JoinListDialog(
            onConfirm = { code ->
                showJoinDialog = false
                viewModel.joinList(code)
            },
            onDismiss = { showJoinDialog = false }
        )
    }

    listToRename?.let { list ->
        ListDialog(
            title = stringResource(R.string.edit_list),
            initialName = list.name,
            initialIcon = list.icon,
            confirmLabel = stringResource(R.string.save),
            onConfirm = { name, icon ->
                listToRename = null
                viewModel.updateList(list.id, name, icon)
            },
            onDismiss = { listToRename = null }
        )
    }

    listToDelete?.let { list ->
        DeleteListDialog(
            listName = list.name,
            onConfirm = {
                listToDelete = null
                viewModel.deleteList(list.id)
            },
            onDismiss = { listToDelete = null }
        )
    }

    listToShare?.let { list ->
        ShareDialog(
            listId = list.id,
            onDismiss = { listToShare = null },
            onLeft = { listToShare = null }
        )
    }

    listToLeave?.let { list ->
        ConfirmDialog(
            title = stringResource(R.string.leave_list),
            message = stringResource(R.string.leave_list_message, list.name),
            confirmLabel = stringResource(R.string.leave),
            onConfirm = {
                listToLeave = null
                viewModel.leaveList(list.id)
            },
            onDismiss = { listToLeave = null }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.logout)) },
            text = { Text(stringResource(R.string.logout_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) { Text(stringResource(R.string.accept)) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 12.dp, bottom = 2.dp, start = 4.dp)
    )
}

@Composable
private fun CenteredMessage(
    emoji: String,
    text: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, fontSize = 64.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (action != null) {
            Box(Modifier.padding(top = 16.dp)) { action() }
        }
    }
}
