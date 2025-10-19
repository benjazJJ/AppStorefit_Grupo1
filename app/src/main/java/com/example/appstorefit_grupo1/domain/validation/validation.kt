package com.example.appstorefit_grupo1.domain.validation

import android.util.Patterns

//validacion de correo: FORMATO, NO VACIO

fun validateEmail(email: String): String?{

    if(email.isBlank()) return "Campo Email Obligatorio"
    val ok = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    return if(!ok) "Formato de correo invalido" else null
}

//Validacion de Contraseña

fun validateContraseña(contraseña: String): String?{
    if (contraseña.isBlank()) return "Campo contraseña Obligatorio"
    if (!contraseña.any { it.isUpperCase() }) return "Debe contener al menos 1 mayuscula"
    if (!contraseña.any { it.isLowerCase() }) return "Debe contener al menos 1 minusccula"
    if (contraseña.length !in 8..12) return "La contraseña debe tener entre 8 y 12 caracteres"
    if (!contraseña.any { it.isLetterOrDigit() }) return "Debet tener al menos 1 simbolo"
    if(!contraseña.any { it.isDigit() }) return "Debe tener al menos 1 numero"
    return null
}

//validar contrsaeñas iguales confirmar contraseña
fun validateConfir(contraseña: String,confirm: String): String?{
    if (confirm.isBlank()) return "Campo confirmar Obligatorio"
    return if (contraseña != confirm) "Las contraseñas no coinsiden" else null
}

// validar nombre:
fun validateNombre(nombre: String): String?{
    if (nombre.isBlank()) return "Campo Nombre Obligatorio"
    val regex = Regex("^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$")
    return if (!regex.matches(nombre)) "Debe contener solo letras" else null
}

//validar telefono
fun validateTelefono(telefono: String): String?{
    if (telefono.isBlank()) return "Campo Telefono Obligatorio"
    if (!telefono.all { it.isDigit() }) return "Deben ser solo Numeros"
    if (telefono.length !in 8..9) return "El telefono debe tener entre 8 y 9 caracteres"
    return null
}


fun validateRut(input: String): String? {
    val raw = input.lowercase().replace(".", "").replace("-", "").trim()
    if (raw.length < 2) return "RUT incompleto"

    val cuerpo = raw.dropLast(1)
    val dvIngresado = raw.last()

    if (!cuerpo.all { it.isDigit() }) return "Formato inválido"
    val dvCalculado = calcularDv(cuerpo.toLong())

    return if (dvCalculado == dvIngresado) null else "RUT inválido"
}

private fun calcularDv(num: Long): Char {
    var suma = 0
    var multiplicador = 2
    var n = num
    while (n > 0) {
        val dig = (n % 10).toInt()
        suma += dig * multiplicador
        multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        n /= 10
    }
    val resto = 11 - (suma % 11)
    return when (resto) {
        11 -> '0'
        10 -> 'k'
        else -> ('0'.code + resto).toChar()
    }
}


