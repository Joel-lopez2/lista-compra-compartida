package org.insbaixcamp.listadelacompra.ui.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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
import org.insbaixcamp.listadelacompra.ui.share.ShareDialog
import org.insbaixcamp.listadelacompra.ui.theme.ListColors
import org.insbaixcamp.listadelacompra.ui.theme.ListStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    onBack: () -> Unit,
    viewModel: ListDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val style = ListColors.forId(viewModel.listId)

    var showNewCategory by rememberSaveable { mutableStateOf(false) }
    var showShare by rememberSaveable { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

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
                title = {
                    Text(
                        text = uiState.listName,
                        maxLines = 1,
                        fontWeight = FontWeight.Bold
                    )
                },
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
                        // El propietario comparte; los demas solo ven los miembros
                        TextButton(onClick = { showShare = true }) {
                            Text(
                                text = stringResource(
                                    if (uiState.isOwner) R.string.share else R.string.members
                                ),
                                color = style.onColor
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = style.color,
                    titleContentColor = style.onColor,
                    navigationIconContentColor = style.onColor,
                    actionIconContentColor = style.onColor
                )
            )
        },
        bottomBar = {
            if (!uiState.isLoading && uiState.errorRes == null) {
                AddProductBar(
                    style = style,
                    categories = uiState.categories,
                    onAdd = { name, categoryId -> viewModel.addProduct(name, categoryId) },
                    onNewCategory = { showNewCategory = true }
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
                Column(
                    modifier = contentModifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(uiState.listIcon.ifBlank { "\uD83D\uDED2" }, fontSize = 64.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.detail_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    NewCategoryButton(onClick = { showNewCategory = true })
                }
            }

            else -> {
                LazyColumn(
                    modifier = contentModifier,
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item(key = "new_category") {
                        NewCategoryButton(
                            onClick = { showNewCategory = true },
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                    item(key = "progress") {
                        ProgressHeader(
                            done = uiState.doneCount,
                            total = uiState.totalCount,
                            style = style
                        )
                    }
                    // Productos agrupados por categoria (los completados se quedan en la suya, tachados)
                    uiState.groups.forEach { group ->
                        val category = group.category
                        item(key = "header_${category?.id ?: "none"}") {
                            CategoryHeader(
                                category = category,
                                style = style,
                                onRename = { categoryToEdit = category },
                                onDelete = { categoryToDelete = category }
                            )
                        }
                        items(group.products, key = { it.id }) { product ->
                            ProductRow(
                                product = product,
                                style = style,
                                modifier = Modifier.animateItem(),
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

    if (showShare) {
        ShareDialog(
            listId = viewModel.listId,
            onDismiss = { showShare = false },
            onLeft = {
                showShare = false
                onBack()
            }
        )
    }

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
