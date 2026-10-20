package org.insbaixcamp.listadelacompra.ui.auth

import android.util.Patterns
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.insbaixcamp.listadelacompra.R
import org.insbaixcamp.listadelacompra.data.repository.AuthRepository

/** Estado de las pantallas de login y registro. */
data class AuthUiState(
    val isLoading: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val isSuccess: Boolean = false
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        val cleanEmail = email.trim()
        val error = when {
            cleanEmail.isBlank() || password.isBlank() -> R.string.error_empty_fields
            !isValidEmail(cleanEmail) -> R.string.error_invalid_email
            else -> null
        }
        if (error != null) {
            showError(error)
            return
        }
        execute { repository.login(cleanEmail, password) }
    }

    fun register(name: String, email: String, password: String, repeatPassword: String) {
        val cleanName = name.trim()
        val cleanEmail = email.trim()
        val error = when {
            cleanName.isBlank() || cleanEmail.isBlank() ||
                password.isBlank() || repeatPassword.isBlank() -> R.string.error_empty_fields
            !isValidEmail(cleanEmail) -> R.string.error_invalid_email
            password.length < MIN_PASSWORD_LENGTH -> R.string.error_short_password
            password != repeatPassword -> R.string.error_password_mismatch
            else -> null
        }
        if (error != null) {
            showError(error)
            return
        }
        execute { repository.register(cleanName, cleanEmail, password) }
    }

    /** Quita el mensaje de error (por ejemplo cuando el usuario vuelve a escribir). */
    fun clearError() {
        if (_uiState.value.errorRes != null) {
            _uiState.value = _uiState.value.copy(errorRes = null)
        }
    }

    private fun showError(@StringRes errorRes: Int) {
        _uiState.value = AuthUiState(errorRes = errorRes)
    }

    private fun execute(action: suspend () -> Unit) {
        if (_uiState.value.isLoading) return // evita pulsar dos veces
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            try {
                action()
                _uiState.value = AuthUiState(isSuccess = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = AuthUiState(errorRes = mapError(e))
            }
        }
    }

    /** Traduce las excepciones de Firebase a mensajes propios en espanol. */
    @StringRes
    private fun mapError(e: Exception): Int = when (e) {
        is FirebaseAuthWeakPasswordException -> R.string.error_short_password
        is FirebaseAuthUserCollisionException -> R.string.error_email_in_use
        is FirebaseAuthInvalidUserException,
        is FirebaseAuthInvalidCredentialsException -> R.string.error_invalid_credentials
        is FirebaseNetworkException -> R.string.error_network
        else -> R.string.error_generic
    }

    private fun isValidEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}
