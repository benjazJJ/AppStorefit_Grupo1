package com.example.appstorefit_grupo1.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appstorefit_grupo1.data.repository.UserRepository
import com.example.appstorefit_grupo1.domain.validation.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import com.example.appstorefit_grupo1.session.SessionManager
private var emailCheckJob: Job? = null
private var rutCheckJob: Job? = null
private var phoneCheckJob: Job? = null

data class LoginUiState(
    val email: String = "",
    val pass: String = "",
    val emailError: String? = null,
    val passError: String? = null,
    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null,
    val birthDate: String = "",
    val birthDateError: String? = null
)

data class RegisterUiState(
    val rut: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val pass: String = "",
    val confirm: String = "",
    val birthDate: String = "",

    val rutError: String? = null,
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val addressError: String? = null,
    val passError: String? = null,
    val confirmError: String? = null,
    val birthDateError: String? = null,

    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null
)

class AuthViewModel(
    private val repository: UserRepository,
    private val appContext: android.content.Context
) : ViewModel() {

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register

    // ---------- LOGIN ----------
    fun onLoginEmailChange(value: String) {
        _login.update { it.copy(email = value, emailError = validateEmail(value)) }
        recomputeLoginCanSubmit()
    }

    fun onLoginPassChange(value: String) {
        _login.update { it.copy(pass = value) }
        recomputeLoginCanSubmit()
    }

    private fun recomputeLoginCanSubmit() {
        val s = _login.value
        _login.update {
            it.copy(
                canSubmit = s.emailError == null &&
                        s.email.isNotBlank() &&
                        s.pass.isNotBlank()
            )
        }
    }

    fun submitLogin() {
        val s = _login.value
        if (!s.canSubmit || s.isSubmitting) return

        viewModelScope.launch {
            _login.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }
            //delay para más detalle UI
            delay(250)

            val result = repository.login(s.email, s.pass)
            if (result.isSuccess) {
                //Persistir la sesión en DataStore (el repo ya setea SessionManager.user y roleId)
                SessionManager.persistToStore(appContext)

                _login.update { it.copy(isSubmitting = false, success = true, errorMsg = null) }
            } else {
                _login.update {
                    it.copy(
                        isSubmitting = false,
                        success = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "Error de autenticación"
                    )
                }
            }
        }
    }

    fun clearLoginResult() {
        _login.update { it.copy(success = false, errorMsg = null) }
    }

    // ---------- REGISTRO ----------
    fun onRutChange(value: String) {
        val filtered = value.trim()

        // 1) Validación de formato inmediata (tu validateRut actual)
        _register.update { it.copy(rut = filtered, rutError = validateRut(filtered)) }

        // 2) Debounce para chequear unicidad en BD solo si no hay error de formato
        rutCheckJob?.cancel()
        rutCheckJob = viewModelScope.launch {
            delay(350)
            val current = _register.value.rut
            if (_register.value.rutError != null || current.isBlank()) {
                recomputeRegisterCanSubmit()
                return@launch
            }

            val tomado = repository.isRutTaken(current)
            _register.update {
                it.copy(
                    rutError = if (tomado) "RUT ya registrado" else null
                )
            }
            recomputeRegisterCanSubmit()
        }

        recomputeRegisterCanSubmit()
    }

    fun onNameChange(value: String) {
        val filtered = value.filter { it.isLetter() || it.isWhitespace() }
        _register.update { it.copy(name = filtered, nameError = validateNombre(filtered)) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterEmailChange(value: String) {
        // 1) Validación de formato inmediata
        _register.update { it.copy(email = value, emailError = validateEmail(value)) }

        // 2) Debounce para chequear unicidad en BD solo si no hay error de formato
        emailCheckJob?.cancel()
        emailCheckJob = viewModelScope.launch {
            delay(350)
            val current = _register.value.email
            // si el formato es inválido o está vacío, no consultamos BD
            if (_register.value.emailError != null || current.isBlank()) {
                recomputeRegisterCanSubmit()
                return@launch
            }

            val tomado = repository.isEmailTaken(current)
            _register.update {
                it.copy(
                    emailError = if (tomado) "Correo ya registrado" else null
                )
            }
            recomputeRegisterCanSubmit()
        }

        recomputeRegisterCanSubmit()
    }

    fun onPhoneChange(value: String) {
        // 1) dejar solo dígitos
        val digits = value.filter { it.isDigit() }

        // 2) validación inmediata (formato/longitud) usando validateTelefono
        _register.update { it.copy(phone = digits, phoneError = validateTelefono(digits)) }

        // 3) debounce para chequear unicidad SOLO si no hay error de formato
        phoneCheckJob?.cancel()
        phoneCheckJob = viewModelScope.launch {
            delay(350)
            val current = _register.value.phone
            val hayErrorFormato = _register.value.phoneError != null

            if (hayErrorFormato || current.isBlank()) {
                recomputeRegisterCanSubmit()
                return@launch
            }

            val tomado = repository.isPhoneTaken(current)
            _register.update {
                it.copy(
                    phoneError = if (tomado) "Este teléfono ya pertenece a otro usuario." else null
                )
            }
            recomputeRegisterCanSubmit()
        }
        recomputeRegisterCanSubmit()
    }

    fun onBirthDateChange(value: String) {
        _register.update {
            it.copy(
                birthDate = value,
                birthDateError = validateBirthDate(value)
            )
        }
        recomputeRegisterCanSubmit()
    }

    fun onAddressChange(value: String) {
        _register.update { it.copy(address = value, addressError = null) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterPassChange(value: String) {
        _register.update { it.copy(pass = value, passError = validateContraseña(value)) }
        _register.update { it.copy(confirmError = validateConfir(it.pass, it.confirm)) }
        recomputeRegisterCanSubmit()
    }

    fun onConfirmChange(value: String) {
        _register.update { it.copy(confirm = value, confirmError = validateConfir(it.pass, value)) }
        recomputeRegisterCanSubmit()
    }

    private fun recomputeRegisterCanSubmit() {
        val s = _register.value
        val noErrors = listOf(
            s.rutError, s.nameError, s.emailError, s.phoneError,
            s.addressError, s.passError, s.confirmError, s.birthDateError
        ).all { it == null }

        val filled = s.rut.isNotBlank() && s.name.isNotBlank() && s.email.isNotBlank() &&
                s.phone.isNotBlank() && s.address.isNotBlank() &&
                s.pass.isNotBlank() && s.confirm.isNotBlank() &&
                s.birthDate.isNotBlank()

        _register.update { it.copy(canSubmit = noErrors && filled) }
    }

    fun submitRegister() {
        val s = _register.value
        if (!s.canSubmit || s.isSubmitting) return

        viewModelScope.launch {
            _register.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }
            delay(350)

            val result = repository.register(
                rut = s.rut,
                name = s.name,
                email = s.email,
                address = s.address,
                phone = s.phone,
                pass = s.pass,
                birthDate = s.birthDate
            )

            _register.update {
                if (result.isSuccess) {
                    it.copy(isSubmitting = false, success = true, errorMsg = null)
                } else {
                    it.copy(
                        isSubmitting = false,
                        success = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "Registro inválido"
                    )
                }
            }
        }
    }

    fun clearRegisterResult(){
        _register.update { it.copy(success = false, errorMsg = null) }
    }
}
