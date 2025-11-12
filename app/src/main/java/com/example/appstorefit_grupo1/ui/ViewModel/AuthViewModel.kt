package com.example.appstorefit_grupo1.ui.ViewModel

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
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

// Jobs globales (debounce)
private var emailCheckJob: Job? = null
private var rutCheckJob: Job? = null
private var phoneCheckJob: Job? = null
private var phonePerfilCheckJob: Job? = null

private var emailPerfilCheckJob: Job? = null


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

data class PerfilUiState(
    val cargando: Boolean = false,
    val modoEdicion: Boolean = false,

    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val fechaNacimiento: String = "",

    val errorNombre: String? = null,
    val errorCorreo : String? = null,
    val errorTelefono: String? = null,
    val errorFechaNacimiento: String? = null,

    val puedeGuardar: Boolean = false,
    val mensaje: String? = null
)

class AuthViewModel(
    private val repository: UserRepository,
    private val appContext: Context
) : ViewModel() {

    // Login/Registro
    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register

    // Perfil
    private val _perfil = MutableStateFlow(PerfilUiState())
    val perfil: StateFlow<PerfilUiState> = _perfil

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
            delay(250)
            val result = repository.login(s.email, s.pass)
            if (result.isSuccess) {
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
        _register.update { it.copy(rut = filtered, rutError = validateRut(filtered)) }

        rutCheckJob?.cancel()
        rutCheckJob = viewModelScope.launch {
            delay(350)
            val current = _register.value.rut
            if (_register.value.rutError != null || current.isBlank()) {
                recomputeRegisterCanSubmit(); return@launch
            }
            val tomado = repository.isRutTaken(current)
            _register.update { it.copy(rutError = if (tomado) "RUT ya registrado" else null) }
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
        _register.update { it.copy(email = value, emailError = validateEmail(value)) }

        emailCheckJob?.cancel()
        emailCheckJob = viewModelScope.launch {
            delay(350)
            val current = _register.value.email
            if (_register.value.emailError != null || current.isBlank()) {
                recomputeRegisterCanSubmit(); return@launch
            }
            val tomado = repository.isEmailTaken(current)
            _register.update { it.copy(emailError = if (tomado) "Correo ya registrado" else null) }
            recomputeRegisterCanSubmit()
        }
        recomputeRegisterCanSubmit()
    }

    fun onPhoneChange(value: String) {
        val digits = value.filter { it.isDigit() }
        _register.update { it.copy(phone = digits, phoneError = validateTelefono(digits)) }

        phoneCheckJob?.cancel()
        phoneCheckJob = viewModelScope.launch {
            delay(350)
            val current = _register.value.phone
            val hayErrorFormato = _register.value.phoneError != null
            if (hayErrorFormato || current.isBlank()) {
                recomputeRegisterCanSubmit(); return@launch
            }
            val tomado = repository.isPhoneTaken(current)
            _register.update { it.copy(phoneError = if (tomado) "Este teléfono ya pertenece a otro usuario." else null) }
            recomputeRegisterCanSubmit()
        }
        recomputeRegisterCanSubmit()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onBirthDateChange(value: String) {
        _register.update {
            it.copy(
                birthDate = value,
                birthDateError = validateBirthDate(value, 15)
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
                if (result.isSuccess) it.copy(isSubmitting = false, success = true, errorMsg = null)
                else it.copy(isSubmitting = false, success = false, errorMsg = result.exceptionOrNull()?.message ?: "Registro inválido")
            }
        }
    }

    fun clearRegisterResult() {
        _register.update { it.copy(success = false, errorMsg = null) }
    }

    // ---------- PERFIL ----------
    fun cargarPerfil() {
        val u = SessionManager.user
        if (u == null) {
            _perfil.update { it.copy(mensaje = "Sesión no disponible.") }
            return
        }
        _perfil.update {
            it.copy(
                cargando = false,
                modoEdicion = false,
                nombre = u.name,
                correo = u.email,
                telefono = u.phone.orEmpty(),
                direccion = u.address,
                fechaNacimiento = u.birthDate,
                errorNombre = null,
                errorCorreo = null,
                errorTelefono = null,
                errorFechaNacimiento = null,
                puedeGuardar = false,
                mensaje = null
            )
        }
    }

    fun alternarModoEdicionPerfil() {
        _perfil.update { it.copy(modoEdicion = !it.modoEdicion, mensaje = null) }
    }

    fun onPerfilNombreChange(value: String) {
        val err = if (value.isBlank()) "El nombre es obligatorio." else null
        _perfil.update { it.copy(nombre = value, errorNombre = err) }
        recomputePerfilPuedeGuardar()
    }


    fun onPerfilEmailChange(value: String) {
        // 1) Validación inmediata de formato
        val errFormato = validateEmail(value)
        _perfil.update { it.copy(correo = value, errorCorreo = errFormato) }
        recomputePerfilPuedeGuardar()

        // 2) Debounce para chequear unicidad solo si el formato es válido
        emailPerfilCheckJob?.cancel()
        emailPerfilCheckJob = viewModelScope.launch {
            delay(350)

            val s = _perfil.value
            val actual = s.correo
            if (s.errorCorreo != null || actual.isBlank()) {
                recomputePerfilPuedeGuardar(); return@launch
            }

            // Excluir el email actual del usuario (permitir no-cambio)
            val emailActual = SessionManager.user?.email.orEmpty()
            val libre = repository.emailDisponible(actual, emailActual)
            _perfil.update { it.copy(errorCorreo = if (!libre) "Correo ya registrado" else null) }
            recomputePerfilPuedeGuardar()
        }
    }

    fun onPerfilTelefonoChange(value: String) {
        val digits = value.filter { it.isDigit() }
        val errFormato = validateTelefono(digits)
        _perfil.update { it.copy(telefono = digits, errorTelefono = errFormato) }
        recomputePerfilPuedeGuardar()

        phonePerfilCheckJob?.cancel()
        phonePerfilCheckJob = viewModelScope.launch {
            delay(350)
            val s = _perfil.value
            val actual = s.telefono
            if (s.errorTelefono != null || actual.isBlank()) {
                recomputePerfilPuedeGuardar(); return@launch
            }
            val rut = SessionManager.user?.rut
            if (rut.isNullOrBlank()) {
                recomputePerfilPuedeGuardar(); return@launch
            }
            val libre = repository.telefonoDisponible(actual, rut)
            _perfil.update { it.copy(errorTelefono = if (!libre) "El teléfono ya está registrado por otro usuario." else null) }
            recomputePerfilPuedeGuardar()
        }
    }

    fun onPerfilDireccionChange(value: String) {
        _perfil.update { it.copy(direccion = value) }
        recomputePerfilPuedeGuardar()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onPerfilFechaChange(value: String) {
        val err = validateBirthDate(value, 15)
        _perfil.update { it.copy(fechaNacimiento = value, errorFechaNacimiento = err) }
        recomputePerfilPuedeGuardar()
    }

    private fun recomputePerfilPuedeGuardar() {
        val s = _perfil.value
        val okNombre = s.errorNombre == null && s.nombre.isNotBlank()
        val okCorreo = s.errorCorreo == null && s.correo.isNotBlank()
        val okTel = s.errorTelefono == null
        val okFecha = s.errorFechaNacimiento == null && s.fechaNacimiento.isNotBlank()
        val okDir = s.direccion.isNotBlank()
        _perfil.update { it.copy(puedeGuardar = okNombre && okTel && okFecha && okDir) }
    }

    fun submitPerfilGuardar() {
        val s = _perfil.value
        if (!s.puedeGuardar || s.cargando) return

        val rut = SessionManager.user?.rut
        if (rut.isNullOrBlank()) {
            _perfil.update { it.copy(mensaje = "Sesión no disponible.") }
            return
        }

        viewModelScope.launch {
            _perfil.update { it.copy(cargando = true, mensaje = null) }

            if (s.telefono.isNotBlank()) {
                val libre = repository.telefonoDisponible(s.telefono, rut)
                if (!libre) {
                    _perfil.update { it.copy(cargando = false, mensaje = "El teléfono ya está registrado por otro usuario.") }
                    return@launch
                }
            }

            val emailActual = SessionManager.user?.email.orEmpty()
            val libreMail = repository.emailDisponible(s.correo, emailActual)
            if (!libreMail) {
                _perfil.update { it.copy(cargando = false, mensaje = "El correo ya está registrado por otro usuario.") }
                return@launch
            }


            val r = repository.actualizarPerfil(
                rut = rut,
                nombre = s.nombre,
                telefono = s.telefono.ifBlank { null },
                direccion = s.direccion,
                fechaNacimiento = s.fechaNacimiento,
                emailNuevo = s.correo
            )

            if (r.isSuccess) {
                SessionManager.persistToStore(appContext)
                _perfil.update { it.copy(cargando = false, modoEdicion = false, mensaje = "Perfil actualizado correctamente.") }
            } else {
                _perfil.update { it.copy(cargando = false, mensaje = r.exceptionOrNull()?.message ?: "No se pudo actualizar el perfil.") }
            }
        }
    }
}
