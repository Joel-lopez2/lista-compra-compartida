package org.insbaixcamp.listadelacompra.ui.share

import android.util.Patterns
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
    val members: List<User> = emptyList(),  // el propietario primero
    val isWorking: Boolean = false,         // buscando, anadiendo o quitando
    @StringRes val errorRes: Int? = null,
    val sharedWith: String? = null,         // nombre de la persona con la que se acaba de compartir
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

    init {
        viewModelScope.launch {
            listRepository.observeList(listId)
                .catch { error ->
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
                            members = sorted
                        )
                    }
                }
        }
    }

    private suspend fun loadUser(uid: String): User {
        userCache[uid]?.let { return it }
        val user = try {
            shareRepository.getUser(uid)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
        // Si no se puede leer, se muestra un usuario sin datos (y se volvera a intentar luego)
        if (user == null) return User(uid = uid)
        userCache[uid] = user
        return user
    }

    /** Comparte la lista con la persona que tenga ese correo. */
    fun share(emailInput: String) {
        val state = _uiState.value
        if (!state.isOwner || state.isWorking) return

        val email = emailInput.trim().lowercase()
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorRes = R.string.error_share_invalid_email, sharedWith = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, errorRes = null, sharedWith = null) }
            try {
                val user = shareRepository.findUserByEmail(email)
                val error = when {
                    user == null -> R.string.error_user_not_found
                    user.uid == currentUid -> R.string.error_share_self
                    state.members.any { it.uid == user.uid } -> R.string.error_already_member
                    else -> null
                }
                if (error != null || user == null) {
                    _uiState.update { it.copy(isWorking = false, errorRes = error) }
                } else {
                    shareRepository.addMember(listId, user.uid)
                    userCache[user.uid] = user
                    _uiState.update { it.copy(isWorking = false, sharedWith = displayName(user)) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isWorking = false, errorRes = R.string.error_action_failed) }
            }
        }
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

    /** Borra el error y el aviso de exito (por ejemplo cuando el usuario vuelve a escribir). */
    fun clearFeedback() {
        _uiState.update {
            if (it.errorRes != null || it.sharedWith != null) it.copy(errorRes = null, sharedWith = null) else it
        }
    }

    private fun runAction(action: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, errorRes = null, sharedWith = null) }
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
