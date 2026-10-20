package org.insbaixcamp.listadelacompra.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.insbaixcamp.listadelacompra.R
import org.insbaixcamp.listadelacompra.data.model.Category
import org.insbaixcamp.listadelacompra.data.model.Product
import org.insbaixcamp.listadelacompra.ui.components.ConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    onBack: () -> Unit,
    viewModel: ListDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showNewCategory by rememberSaveable { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var topMenuExpanded by remember { mutableStateOf(false) }

    // Si el propietario elimina la lista mientras la tenemos abierta, volvemos atras
    LaunchedEffect(uiState.listDeleted) {
        if (uiState.listDeleted) onBack()
    }

    // Mensajes puntuales
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
                title = { Text(uiState.listName, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (!uiState.isLoading && uiState.errorRes == null) {
                        Box {
                            IconButton(onClick = { topMenuExpanded = true }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_more_vert),
                                    contentDescription = stringResource(R.string.more_options)
                                )
                            }
                            DropdownMenu(
                                expanded = topMenuExpanded,
                                onDismissRequest = { topMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.new_category)) },
                                    onClick = {
                                        topMenuExpanded = false
                                        showNewCategory = true
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (!uiState.isLoading && uiState.errorRes == null) {
                AddProductBar(onAdd = { viewModel.addProduct(it) })
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
                Column(
                    modifier = contentModifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(uiState.errorRes!!),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Box(Modifier.padding(top = 16.dp)) {
                        Button(onClick = { viewModel.load() }) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }

            uiState.isEmpty -> {
                Box(contentModifier.padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.detail_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = contentModifier,
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Productos agrupados por categoria (los completados se quedan en la suya, tachados)
                    uiState.groups.forEach { group ->
                        val category = group.category
                        item(key = "header_${category?.id ?: "none"}") {
                            CategoryHeader(
                                category = category,
                                onRename = { categoryToEdit = category },
                                onDelete = { categoryToDelete = category }
                            )
                        }
                        items(group.products, key = { it.id }) { product ->
                            ProductRow(
                                product = product,
                                onToggle = { viewModel.toggleCompleted(product) },
                                onClick = { productToEdit = product }
                            )
                        }
                    }
                }
            }
        }
    }

    // ---- Dialogos ----

    if (showNewCategory) {
        CategoryDialog(
            title = stringResource(R.string.new_category),
            initialName = "",
            initialIcon = "",
            confirmLabel = stringResource(R.string.create),
            onConfirm = { name, icon ->
                showNewCategory = false
                viewModel.createCategory(name, icon)
            },
            onDismiss = { showNewCategory = false }
        )
    }

    categoryToEdit?.let { category ->
        CategoryDialog(
            title = stringResource(R.string.rename_category),
            initialName = category.name,
            initialIcon = category.icon,
            confirmLabel = stringResource(R.string.save),
            onConfirm = { name, icon ->
                categoryToEdit = null
                viewModel.updateCategory(category, name, icon)
            },
            onDismiss = { categoryToEdit = null }
        )
    }

    categoryToDelete?.let { category ->
        ConfirmDialog(
            title = stringResource(R.string.delete_category_title),
            message = stringResource(R.string.delete_category_message, category.name),
            confirmLabel = stringResource(R.string.delete),
            onConfirm = {
                categoryToDelete = null
                viewModel.deleteCategory(category)
            },
            onDismiss = { categoryToDelete = null }
        )
    }

    productToEdit?.let { product ->
        ProductDialog(
            product = product,
            categories = uiState.categories,
            onSave = { name, quantity, categoryId ->
                productToEdit = null
                viewModel.updateProduct(product, name, quantity, categoryId)
            },
            onDelete = {
                productToEdit = null
                productToDelete = product
            },
            onDismiss = { productToEdit = null }
        )
    }

    productToDelete?.let { product ->
        ConfirmDialog(
            title = stringResource(R.string.delete_product_title),
            message = stringResource(R.string.delete_product_message, product.name),
            confirmLabel = stringResource(R.string.delete),
            onConfirm = {
                productToDelete = null
                viewModel.deleteProduct(product)
            },
            onDismiss = { productToDelete = null }
        )
    }
}

/** Barra inferior para anadir productos rapidamente. */
@Composable
private fun AddProductBar(onAdd: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }

    fun submit() {
        if (text.isNotBlank()) {
            onAdd(text)
            text = ""
        }
    }

    Surface(tonalElevation = 3.dp, color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= 40) text = it },
                placeholder = { Text(stringResource(R.string.add_product_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { submit() }, enabled = text.isNotBlank()) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.add_product)
                )
            }
        }
    }
}

/** Cabecera de una categoria. category = null es "Sin categoria" (sin menu). */
@Composable
private fun CategoryHeader(
    category: Category?,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val title = if (category == null) {
        stringResource(R.string.uncategorized)
    } else {
        listOf(category.icon, category.name).filter { it.isNotBlank() }.joinToString(" ")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp)
        )
        if (category != null) {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert),
                        contentDescription = stringResource(R.string.more_options),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.rename)) },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete)) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductRow(
    product: Product,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    val done = product.completed
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = done, onCheckedChange = { onToggle() })
        Column(modifier = Modifier
            .weight(1f)
            .padding(end = 8.dp)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (done) TextDecoration.LineThrough else null,
                color = if (done) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            )
            if (product.quantity.isNotBlank()) {
                Text(
                    text = product.quantity,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
