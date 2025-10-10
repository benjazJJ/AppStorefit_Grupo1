package com.example.appstorefit_grupo1.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appstorefit_grupo1.domain.validation.validateEmail
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

//Estructuras de datos para los formularios(pantallas)

data class LoginuiState(

    // variables para guardar los datos de los campos
    val email: String = " ",
    val pass : String = " ",

    // variables manejo de error
    val emailError: String? = null,
    val passError: String? = null,

    // varables del formulario general
    val msgError: String? = null,
    val isSubmitting: Boolean = false, // flag carga
    val canSubmit: Boolean = false, // habilitar boton
    val succes: Boolean = false //resltado ok
)

data class RegisterUiState(

    //variables para guardar los datos de los campos
    val nombre: String = "",
    val correo: String = "",
    val phone: String = "",
    val pass: String = "",
    val confirm: String = "",

    //Variables de manejo de error
    val nombreError: String? = null,
    val correoError: String? = null,
    val phoneError: String? = null,
    val passError: String? = null,
    val confirmError: String? = null,

    //variables del formulario
    val msgError: String? = null,
    val isSubmitting: Boolean = false, // flag carga
    val canSubmit: Boolean = false, // habilitar boton
    val succes: Boolean = false //resltado ok
)

//modelo para los datos del usuario
data class DemoUser(

    val nombre: String,
    val correo: String,
    val phone: String,
    val pass: String,

)

class AuthViewModel: ViewModel(){

    //crear coneccion etatica e datos compartida

    companion object{
        //creo la coleccion
        private val USERS = mutableListOf(
            //Usuario de prueba
            DemoUser("Juan","a@a.cl","12345678","Juan123!")
        )

    }
    private val _login = MutableStateFlow(LoginuiState())
    val login: StateFlow<LoginuiState> = _login

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register

    //manipuladores el login

    fun recomputeLoginCanSubmit(){
        val s = _login.value
        val can = s.emailError != null && s.emailError.isNotBlank() && s.pass.isNotBlank()
        _login.update { it.copy(canSubmit = can) }
    }

    fun onLoginEmailChangue(value: String){
        _login.update { it.copy(email = value, emailError = validateEmail(value)) }
    }

    fun onLoginPassChangue(value: String){
        _login.update { it.copy(pass = value) }
        recomputeLoginCanSubmit()
    }

    fun submitLogin(){
        val s = _login.value
        if(!s.canSubmit || s.isSubmitting) return
        viewModelScope.launch {
            _login.update { it.copy(isSubmitting = true, msgError = null, succes = false) }
            delay(1000)
            //buscamos si el usuario existe
            val user = USERS.firstOrNull{it.correo.equals(s.email, ignoreCase = true)}
            val ok = user != null && user.pass == s.pass

            _login.update { it.copy(
                isSubmitting = false,
                succes = ok,
                msgError = if(!ok) "Credeciales invalidas" else null
            ) }
        }
    }

    fun cleraLoginResult(){
        _login.update { it.copy(succes = false, msgError = null) }
    }


}