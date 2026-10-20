package org.insbaixcamp.listadelacompra.ui.lists

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.insbaixcamp.listadelacompra.R
import org.insbaixcamp.listadelacompra.data.model.ShoppingList
import org.insbaixcamp.listadelacompra.data.repository.ListRepository
import org.insbaixcamp.listadelacompra.data.repository.ShareRepository

/** Una lista junto con los datos extra que se muestran en su tarjeta. */
data class ListItem(
    val list: ShoppingList,
    val productCount: Int = 0,
    val completedCount: Int = 0,
    val ownerName: String? = null
)

/** Estado de la pantalla "Mis listas". */
data class ListsUiState(
    val isLoading: Boolean = true,
    @StringRes val errorRes: Int? = null,      // error al cargar (ocupa la pantalla)
    val myLists: List<ListItem> = emptyList(),
    val sharedLists: List<ListItem> = emptyList(),
    val userName: String = "",                // nombre del usuario para el saludo
    @StringRes val messageRes: Int? = null     // mensaje puntual (snackbar)
)

@OptIn(ExperimentalCoroutinesApi::class)
class ListsViewModel(
    private val repository: ListRepository = ListRepository(),
    private val shareRepository: ShareRepository = ShareRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListsUiState())
    val uiState: StateFlow<ListsUiState> = _uiState.asStateFlow()

    // Nombres de propietarios ya consultados (uid -> nombre)
    private val ownerNames = MutableStateFlow<Map<String, String>>(emptyMap())
    private val requestedOwners = mutableSetOf<String>()
    private var loadJob: Job? = null

    init {
        load()
        loadUserName()
    }

    /** Lee el nombre del usuario (solo la primera palabra) para el saludo. */
    private fun loadUserName() {
        val uid = repository.currentUid ?: return
        viewModelScope.launch {
            val name = try {
                repository.getUserName(uid)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            if (!name.isNullOrBlank()) {
                _uiState.update { it.copy(userName = name.trim().substringBefore(' ')) }
            }
        }
    }

    /** Empieza (o reinicia) la escucha en tiempo real de las listas. */
    fun load() {
        val uid = repository.currentUid
        if (uid == null) {
            _uiState.update { it.copy(isLoading = false, errorRes = R.string.error_loading_lists) }
            return
        }
        loadJob?.cancel()
        _uiState.update { it.copy(isLoading = true, errorRes = null) }

        loadJob = viewModelScope.launch {
            repository.observeLists(uid)
                .flatMapLatest { lists -> withProductCount(lists) }
                .combine(ownerNames) { items, names ->
                    items.map { it.copy(ownerName = names[it.list.ownerId]) }
                }
                .catch {
                    _uiState.update { s ->
                        s.copy(isLoading = false, errorRes = R.string.error_loading_lists)
                    }
                }
                .collect { items ->
                    // Mas recientes arriba; una lista aun sin fecha confirmada es la mas nueva
                    val sorted = items.sortedByDescending {
                        it.list.createdAt?.toDate()?.time ?: Long.MAX_VALUE
                    }
                    val (mine, shared) = sorted.partition { it.list.ownerId == uid }
                    _uiState.update { s ->
                        s.copy(
                            isLoading = false,
                            errorRes = null,
                            myLists = mine,
                            sharedLists = shared
                        )
                    }
                    loadMissingOwners(shared.map { it.list.ownerId })
                }
        }
    }

    /** Combina cada lista con el contador de sus productos en tiempo real. */
    private fun withProductCount(lists: List<ShoppingList>): Flow<List<ListItem>> {
        if (lists.isEmpty()) return flowOf(emptyList())
        val statsFlows = lists.map { repository.observeProductStats(it.id) }
        return combine(statsFlows) { stats ->
            lists.mapIndexed { index, list ->
                ListItem(list, productCount = stats[index].total, completedCount = stats[index].done)
            }
        }
    }

    private fun loadMissingOwners(ownerIds: List<String>) {
        ownerIds.distinct().filter { requestedOwners.add(it) }.forEach { ownerId ->
            viewModelScope.launch {
                val name = try {
                    repository.getUserName(ownerId)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    null
                }
                if (name != null) {
                    ownerNames.update { it + (ownerId to name) }
                } else {
                    requestedOwners.remove(ownerId) // se volvera a intentar mas tarde
                }
            }
        }
    }

    fun createList(name: String) {
        val clean = name.trim().take(MAX_LIST_NAME_LENGTH)
        if (clean.isBlank()) return
        runAction { repository.createList(clean) }
    }

    fun renameList(listId: String, name: String) {
        val clean = name.trim().take(MAX_LIST_NAME_LENGTH)
        if (clean.isBlank()) return
        runAction { repository.renameList(listId, clean) }
    }

    fun deleteList(listId: String) {
        runAction { repository.deleteList(listId) }
    }

    /** El usuario sale de una lista que le han compartido. */
    fun leaveList(listId: String) {
        val uid = repository.currentUid ?: return
        runAction { shareRepository.removeMember(listId, uid) }
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
