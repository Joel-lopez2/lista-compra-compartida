package org.insbaixcamp.listadelacompra.ui.share

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.insbaixcamp.listadelacompra.R
import org.insbaixcamp.listadelacompra.data.model.User
import org.insbaixcamp.listadelacompra.data.repository.ListRepository
import org.insbaixcamp.listadelacompra.data.repository.ShareRepository

/** Estado del dialogo de compartir. */
data class ShareUiState(
    val isLoading: Boolean = true,
    val listName: String = "",
    val ownerId: String = "",
    val currentUid: String = "",
    val code: String = "",                  // codigo para unirse a la lista
    val members: List<User> = emptyList(),  // el propietario primero
    val isWorking: Boolean = false,         // quitando a alguien o saliendo
    @StringRes val errorRes: Int? = null,
    val left: Boolean = false               // el usuario ya no tiene acceso a la lista
) {
    val isOwner: Boolean
        get() = currentUid.isNotEmpty() && currentUid == ownerId
}

class ShareViewModel(private val listId: String) : ViewModel() {

    private val listRepository = ListRepository()
    private val shareRepository = ShareRepository()
    private val currentUid = listRepository.currentUid.orEmpty()

    private val _uiState = MutableStateFlow(ShareUiState(currentUid = currentUid))
    val uiState: StateFlow<ShareUiState> = _uiState.asStateFlow()

    // Usuarios ya consultados (uid -> usuario) para no repetir lecturas
    private val userCache = mutableMapOf<String, User>()

    // Para crear el codigo de una lista antigua solo una vez
    private var creatingCode = false

    init {
        viewModelScope.launch {
            listRepository.observeList(listId)
                .catch { error ->
                    // Sin permiso = ya no somos miembros de la lista
                    val lostAccess = error is FirebaseFirestoreException &&
                        error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
                    _uiState.update {
                        if (lostAccess) it.copy(isLoading = false, left = true)
                        else it.copy(isLoading = false, errorRes = R.string.error_loading_members)
                    }
                }
                .collect { list ->
                    if (list == null) {
                        // La lista ya no existe
                        _uiState.update { it.copy(isLoading = false, left = true) }
                        return@collect
                    }

                    // Las listas antiguas no tienen codigo: lo crea el propietario al abrir el dialogo
                    if (list.code.isBlank() && list.ownerId == currentUid && !creatingCode) {
                        creatingCode = true
                        runAction { listRepository.createCodeForList(listId) }
                    }

                    // Propietario primero y despues los demas por nombre
                    val users = list.members.map { uid -> loadUser(uid) }
                    val sorted = users.sortedWith(
                        compareByDescending<User> { it.uid == list.ownerId }
                            .thenBy { displayName(it).lowercase() }
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            listName = list.name,
                            ownerId = list.ownerId,
                            code = list.code,
                            members = sorted
                        )
                    }
                }
        }
    }

    /** Lee un usuario de Firestore (o de la memoria si ya lo habiamos leido). */
    private suspend fun loadUser(uid: String): User {
        userCache[uid]?.let { return it }
        val user = try {
            shareRepository.getUser(uid)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
        // Si no se puede leer, se muestra un usuario sin datos
        if (user == null) return User(uid = uid)
        userCache[uid] = user
        return user
    }

    /** El propietario quita a un miembro. */
    fun removeMember(user: User) {
        val state = _uiState.value
        if (!state.isOwner || user.uid == state.ownerId) return
        runAction { shareRepository.removeMember(listId, user.uid) }
    }

    /** Un invitado sale de la lista. El propietario no puede salir (tiene que eliminarla). */
    fun leave() {
        val state = _uiState.value
        if (state.isOwner || currentUid.isEmpty()) return
        runAction {
            shareRepository.removeMember(listId, currentUid)
            _uiState.update { it.copy(left = true) }
        }
    }

    /** Ejecuta una accion de Firestore y muestra un error si falla. */
    private fun runAction(action: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, errorRes = null) }
            try {
                action()
                _uiState.update { it.copy(isWorking = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isWorking = false, errorRes = R.string.error_action_failed) }
            }
        }
    }

    companion object {
        /** Nombre a mostrar de un usuario: su nombre, o su correo si no tiene. */
        fun displayName(user: User): String =
            user.displayName.ifBlank { user.email }
    }
}
