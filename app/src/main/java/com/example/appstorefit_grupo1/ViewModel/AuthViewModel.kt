package com.example.appstorefit_grupo1.ViewModel

import androidx.lifecycle.ViewModel                       // Base de ViewModel
import androidx.lifecycle.viewModelScope                  // Scope de corrutinas ligado al VM
import com.example.appstorefit_grupo1.data.repository.UserRepository
import com.example.appstorefit_grupo1.domain.validation.validateConfir
import com.example.appstorefit_grupo1.domain.validation.validateContraseña
import com.example.appstorefit_grupo1.domain.validation.validateEmail
import com.example.appstorefit_grupo1.domain.validation.validateNombre
import com.example.appstorefit_grupo1.domain.validation.validateTelefono
import kotlinx.coroutines.delay                            // Simulamos tareas async (IO/red)
import kotlinx.coroutines.flow.MutableStateFlow            // Estado observable mutable
import kotlinx.coroutines.flow.StateFlow                   // Exposición inmutable
import kotlinx.coroutines.flow.update                      // Helper para actualizar flows
import kotlinx.coroutines.launch                            // Lanzar corrutinas
           // Importamos las funciones de validación

// ----------------- ESTADOS DE UI (observable con StateFlow) -----------------

data class LoginUiState(                                   // Estado de la pantalla Login
    val email: String = "",                                // Campo email
    val pass: String = "",                                 // Campo contraseña (texto)
    val emailError: String? = null,                        // Error de email
    val passError: String? = null,                         // (Opcional) error de pass en login
    val isSubmitting: Boolean = false,                     // Flag de carga
    val canSubmit: Boolean = false,                        // Habilitar botón
    val success: Boolean = false,                          // Resultado OK
    val errorMsg: String? = null                           // Error global (credenciales inválidas)
)

data class RegisterUiState(                                // Estado de la pantalla Registro (<= 5 campos)
    val name: String = "",                                 // 1) Nombre
    val email: String = "",                                // 2) Email
    val phone: String = "",                                // 3) Teléfono
    val pass: String = "",                                 // 4) Contraseña
    val confirm: String = "",                              // 5) Confirmación

    val nameError: String? = null,                         // Errores por campo
    val emailError: String? = null,
    val phoneError: String? = null,
    val passError: String? = null,
    val confirmError: String? = null,

    val isSubmitting: Boolean = false,                     // Flag de carga
    val canSubmit: Boolean = false,                        // Habilitar botón
    val success: Boolean = false,                          // Resultado OK
    val errorMsg: String? = null                           // Error global (ej: duplicado)
)

// ----------------- COLECCIÓN EN MEMORIA (solo para la demo) -----------------

// Modelo mínimo de usuario para la colección
// 1.-borre data class

class AuthViewModel(
    //2.- traigo al repositorio como parametro
    private val repository: UserRepository
) : ViewModel() {                         // ViewModel que maneja Login/Registro

    // Colección **estática** en memoria compartida entre instancias del VM (sin storage persistente)

    //3.-borro la lsita mutable

    // Flujos de estado para observar desde la UI
    private val _login = MutableStateFlow(LoginUiState())   // Estado interno (Login)
    val login: StateFlow<LoginUiState> = _login             // Exposición inmutable

    private val _register = MutableStateFlow(RegisterUiState()) // Estado interno (Registro)
    val register: StateFlow<RegisterUiState> = _register        // Exposición inmutable

    // ----------------- LOGIN: handlers y envío -----------------

    fun onLoginEmailChange(value: String) {                 // Handler cuando cambia el email
        _login.update { it.copy(email = value, emailError = validateEmail(value)) } // Guardamos + validamos
        recomputeLoginCanSubmit()                           // Recalculamos habilitado
    }

    fun onLoginPassChange(value: String) {                  // Handler cuando cambia la contraseña
        _login.update { it.copy(pass = value) }             // Guardamos (sin validar fuerza aquí)
        recomputeLoginCanSubmit()                           // Recalculamos habilitado
    }

    private fun recomputeLoginCanSubmit() {                 // Regla para habilitar botón "Entrar"
        val s = _login.value                                // Tomamos el estado actual
        val can = s.emailError == null &&                   // Email válido
                s.email.isNotBlank() &&                   // Email no vacío
                s.pass.isNotBlank()                       // Password no vacía
        _login.update { it.copy(canSubmit = can) }          // Actualizamos el flag
    }

    fun submitLogin() {                                     // Acción de login (simulación async)
        val s = _login.value                                // Snapshot del estado
        if (!s.canSubmit || s.isSubmitting) return          // Si no se puede o ya está cargando, salimos
        viewModelScope.launch {                             // Lanzamos corrutina
            _login.update { it.copy(isSubmitting = true, errorMsg = null, success = false) } // Seteamos loading
            delay(500)                                      // Simulamos tiempo de verificación

            // Buscamos en la **colección en memoria** un usuario con ese email
            //borro y llamo al repositorio
            val result= repository.login(s.email,s.pass)
            _login.update {
                if(result.isSuccess){
                    it.copy(isSubmitting = false, success = true, errorMsg = null)
                }else{
                    it.copy(isSubmitting = false, success = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "Error de autenticacion")
                }
            }
        }
    }

    fun clearLoginResult() {                                // Limpia banderas tras navegar
        _login.update { it.copy(success = false, errorMsg = null) }
    }

    // ----------------- REGISTRO: handlers y envío -----------------

    fun onNameChange(value: String) {                       // Handler del nombre
        val filtered = value.filter { it.isLetter() || it.isWhitespace() } // Filtramos números/símbolos (solo letras/espacios)
        _register.update {                                  // Guardamos + validamos
            it.copy(name = filtered, nameError = validateNombre(filtered))
        }
        recomputeRegisterCanSubmit()                        // Recalculamos habilitado
    }

    fun onRegisterEmailChange(value: String) {              // Handler del email
        _register.update { it.copy(email = value, emailError = validateEmail(value)) } // Guardamos + validamos
        recomputeRegisterCanSubmit()
    }

    fun onPhoneChange(value: String) {                      // Handler del teléfono
        val digitsOnly = value.filter { it.isDigit() }      // Dejamos solo dígitos
        _register.update {                                  // Guardamos + validamos
            it.copy(phone = digitsOnly, phoneError = validateTelefono(digitsOnly))
        }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterPassChange(value: String) {               // Handler de la contraseña
        _register.update { it.copy(pass = value, passError = validateContraseña(value)) } // Validamos seguridad
        // Revalidamos confirmación con la nueva contraseña
        _register.update { it.copy(confirmError = validateConfir(it.pass, it.confirm)) }
        recomputeRegisterCanSubmit()
    }

    fun onConfirmChange(value: String) {                    // Handler de confirmación
        _register.update { it.copy(confirm = value, confirmError = validateConfir(it.pass, value)) } // Guardamos + validamos
        recomputeRegisterCanSubmit()
    }

    private fun recomputeRegisterCanSubmit() {              // Habilitar "Registrar" si todo OK
        val s = _register.value                              // Tomamos el estado actual
        val noErrors = listOf(s.nameError, s.emailError, s.phoneError, s.passError, s.confirmError).all { it == null } // Sin errores
        val filled = s.name.isNotBlank() && s.email.isNotBlank() && s.phone.isNotBlank() && s.pass.isNotBlank() && s.confirm.isNotBlank() // Todo lleno
        _register.update { it.copy(canSubmit = noErrors && filled) } // Actualizamos flag
    }

    fun submitRegister() {                                  // Acción de registro (simulación async)
        val s = _register.value                              // Snapshot del estado
        if (!s.canSubmit || s.isSubmitting) return          // Evitamos reentradas
        viewModelScope.launch {                             // Corrutina
            _register.update { it.copy(isSubmitting = true, errorMsg = null, success = false) } // Loading
            delay(700)                                      // Simulamos IO

            // ¿Existe ya un usuario con el mismo email en la **colección**?
           //Borro y llamo al repositorio
            val result = repository.register(
                name = s.name,
                email = s.email,
                phone = s.phone,
                pass = s.pass
            )

            _register.update {
                if(result.isSuccess){
                    it.copy(isSubmitting = false, success = true, errorMsg = null)
                }else{
                    it.copy(isSubmitting = false, success = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "Registro invalido")
                }

            }
        }
    }

    fun clearRegisterResult() {                             // Limpia banderas tras navegar
        _register.update { it.copy(success = false, errorMsg = null) }
    }
}