package org.insbaixcamp.listadelacompra.ui.detail

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.insbaixcamp.listadelacompra.R
import org.insbaixcamp.listadelacompra.data.model.Category
import org.insbaixcamp.listadelacompra.data.model.Product
import org.insbaixcamp.listadelacompra.data.model.ShoppingList
import org.insbaixcamp.listadelacompra.data.repository.CategoryRepository
import org.insbaixcamp.listadelacompra.data.repository.ListRepository
import org.insbaixcamp.listadelacompra.data.repository.ProductRepository
import org.insbaixcamp.listadelacompra.navigation.Routes
import org.insbaixcamp.listadelacompra.util.AutoCategorizer

/** Una categoria con sus productos (pendientes y completados). category = null significa "Sin categoria". */
data class CategoryGroup(
    val category: Category?,
    val products: List<Product>
)

/** Estado de la pantalla de detalle de una lista. */
data class DetailUiState(
    val isLoading: Boolean = true,
    @StringRes val errorRes: Int? = null,
    val listName: String = "",
    val listIcon: String = "",
    val isOwner: Boolean = false,
    val categories: List<Category> = emptyList(),
    val groups: List<CategoryGroup> = emptyList(),
    val listDeleted: Boolean = false,
    @StringRes val messageRes: Int? = null
) {
    val isEmpty: Boolean
        get() = categories.isEmpty() && groups.isEmpty()

    /** Productos totales y completados, para la barra de progreso. */
    val totalCount: Int
        get() = groups.sumOf { it.products.size }
    val doneCount: Int
        get() = groups.sumOf { group -> group.products.count { it.completed } }
}

/**
 * ViewModel del detalle. El id y el nombre de la lista llegan por los
 * argumentos de la ruta (SavedStateHandle).
 */
class ListDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    val listId: String = savedStateHandle.get<String>(Routes.DETAIL_ARG_ID).orEmpty()
    private val initialName: String = savedStateHandle.get<String>(Routes.DETAIL_ARG_NAME).orEmpty()

    private val listRepository = ListRepository()
    private val categoryRepository = CategoryRepository()
    private val productRepository = ProductRepository()

    private val _uiState = MutableStateFlow(DetailUiState(listName = initialName))
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    /** Empieza (o reinicia) la escucha en tiempo real de la lista, sus categorias y sus productos. */
    fun load() {
        loadJob?.cancel()
        _uiState.update { it.copy(isLoading = true, errorRes = null) }

        loadJob = viewModelScope.launch {
            combine(
                listRepository.observeList(listId),
                categoryRepository.observeCategories(listId),
                productRepository.observeProducts(listId)
            ) { list, categories, products ->
                buildState(list, categories, products)
            }
                .catch { error ->
                    // Si el propietario nos quita de la lista, ya no hay permiso: volvemos atras
                    val lostAccess = error is FirebaseFirestoreException &&
                        error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
                    _uiState.update { s ->
                        if (lostAccess) s.copy(isLoading = false, listDeleted = true)
                        else s.copy(isLoading = false, errorRes = R.string.error_loading_list)
                    }
                }
                .collect { newState ->
                    _uiState.update { old -> newState.copy(messageRes = old.messageRes) }
                }
        }
    }

    private fun buildState(
        list: ShoppingList?,
        categories: List<Category>,
        products: List<Product>
    ): DetailUiState {
        // La lista ya no existe (la ha eliminado su propietario)
        if (list == null) {
            return DetailUiState(isLoading = false, listName = initialName, listDeleted = true)
        }

        val sortedCategories = categories.sortedBy { it.name.lowercase() }
        // Los productos completados se quedan en su categoria, pero bajan al final de ella.
        // Dentro de cada grupo (pendientes / completados) se mantiene el orden de creacion.
        val sortedProducts = products.sortedWith(
            compareBy<Product>({ it.completed }, { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE })
        )

        val knownIds = categories.map { it.id }.toSet()
        val groups = sortedCategories.map { category ->
            CategoryGroup(category, sortedProducts.filter { it.categoryId == category.id })
        }.toMutableList()

        // Sin categoria: los que no tienen o los de una categoria que ya no existe
        val uncategorized = sortedProducts.filter { it.categoryId.isBlank() || it.categoryId !in knownIds }
        if (uncategorized.isNotEmpty()) {
            groups.add(CategoryGroup(null, uncategorized))
        }

        return DetailUiState(
            isLoading = false,
            listName = list.name,
            listIcon = list.icon,
            isOwner = list.ownerId == listRepository.currentUid,
            categories = sortedCategories,
            groups = groups
        )
    }

    // ---- Productos ----

    /**
     * Anade un producto. Si el usuario no ha elegido categoria, se clasifica solo
     * (extra: autoclasificacion): "leche" -> "Lácteos". La categoria se reutiliza si ya
     * existe en la lista y, si no, se crea. Si no se reconoce el producto, queda "Sin categoria".
     */
    fun addProduct(name: String, categoryId: String) {
        val clean = name.trim()
        if (clean.isBlank()) return
        runAction {
            val finalCategoryId = categoryId.ifBlank { autoCategoryId(clean) }
            productRepository.addProduct(listId, clean, finalCategoryId)
        }
    }

    // Categorias creadas automaticamente en esta sesion (nombre normalizado -> id).
    // Evita duplicarlas si se anaden varios productos seguidos antes de que Firestore responda.
    private val autoCreated = mutableMapOf<String, String>()
    private val autoMutex = Mutex()

    /** Devuelve el id de la categoria sugerida para el producto ("" si no se reconoce). */
    private suspend fun autoCategoryId(productName: String): String {
        val suggestion = AutoCategorizer.suggest(productName) ?: return ""
        val key = AutoCategorizer.normalize(suggestion.name)
        return autoMutex.withLock {
            val existing = _uiState.value.categories
                .firstOrNull { AutoCategorizer.normalize(it.name) == key }
            existing?.id
                ?: autoCreated[key]
                ?: categoryRepository.createCategory(listId, suggestion.name, suggestion.icon)
                    .also { autoCreated[key] = it }
        }
    }

    fun toggleCompleted(product: Product) {
        runAction { productRepository.setCompleted(listId, product.id, !product.completed) }
    }

    fun updateProduct(product: Product, name: String, quantity: String, categoryId: String) {
        val clean = name.trim()
        if (clean.isBlank()) return
        runAction {
            productRepository.updateProduct(listId, product.id, clean, quantity.trim(), categoryId)
        }
    }

    fun deleteProduct(product: Product) {
        runAction { productRepository.deleteProduct(listId, product.id) }
    }

    // ---- Categorias ----

    fun createCategory(name: String, icon: String) {
        val clean = name.trim()
        if (clean.isBlank()) return
        runAction { categoryRepository.createCategory(listId, clean, icon.trim()) }
    }

    fun updateCategory(category: Category, name: String, icon: String) {
        val clean = name.trim()
        if (clean.isBlank()) return
        runAction { categoryRepository.updateCategory(listId, category.id, clean, icon.trim()) }
    }

    fun deleteCategory(category: Category) {
        runAction { categoryRepository.deleteCategory(listId, category.id) }
    }

    fun messageShown() {
        _uiState.update { it.copy(messageRes = null) }
    }

    private fun runAction(action: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(messageRes = R.string.error_action_failed) }
            }
        }
    }
}
