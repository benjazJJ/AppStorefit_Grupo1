package com.example.appstorefit_grupo1.ViewModel

import androidx.lifecycle.ViewModel

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
    val isSubmitting: Boolean? = false, // flag carga
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
    val isSubmitting: Boolean? = false, // flag carga
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



}