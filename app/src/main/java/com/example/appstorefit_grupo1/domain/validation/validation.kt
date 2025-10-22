package com.example.appstorefit_grupo1.domain.validation

import android.util.Patterns


fun validateEmail(email: String): String? {
    val limpio = email.trim()
    if (limpio.isEmpty()) return "Campo email obligatorio"
    val ok = Patterns.EMAIL_ADDRESS.matcher(limpio).matches()
    return if (!ok) "Formato de correo inválido" else null
}

fun emailCanonico(raw: String): String =
    raw.trim().lowercase()


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
fun normalizarTelefono(raw: String): String {
    var s = raw.trim().replace(Regex("\\D+"), "") // solo dígitos
    if (s.startsWith("56") && s.length == 11) s = s.drop(2) // +56 / 56
    if (s.startsWith("0") && s.length == 10) s = s.drop(1)  // 0
    return s
}

fun validateTelefono(telefono: String): String? {
    if (telefono.isBlank()) return "Campo Teléfono Obligatorio"
    val canon = normalizarTelefono(telefono)
    if (canon.length != 9) return "El teléfono debe tener 9 dígitos (Chile)"
    if (!canon.all { it.isDigit() }) return "El teléfono debe contener solo números"
    if (canon.toSet().size == 1) return "El teléfono ingresado no es válido"
    if (canon.first() !in '2'..'9') return "El teléfono ingresado no es válido"
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