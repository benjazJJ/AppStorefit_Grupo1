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


fun validateRut(rut: String): String? {
    val trimmed = rut.trim()

    val regex = Regex("""^\d{1,2}\.\d{3}\.\d{3}-[\dkK]$""")
    if (!regex.matches(trimmed)) {
        return "Formato RUT inválido. Usa 12.345.678-5"
    }

    val parts = trimmed.replace(".", "").split("-")
    val cuerpo = parts[0]                 // solo dígitos
    val dvIngresado = parts[1].uppercase() // "0".."9" o "K"

    // Valida DV (módulo 11)
    return if (calculardv(cuerpo, dvIngresado)) null
    else "RUT no válido (dígito verificador incorrecto)"
}

private fun calculardv(numero: String, dvEsperado: String): Boolean {
    var suma = 0
    var multiplicador = 2
    for (c in numero.reversed()) {
        suma += (c - '0') * multiplicador
        multiplicador++
        if (multiplicador > 7) multiplicador = 2
    }
    val resto = 11 - (suma % 11)
    val dvCalculado = when (resto) {
        11 -> "0"
        10 -> "K"
        else -> resto.toString()
    }
    return dvCalculado == dvEsperado
}


