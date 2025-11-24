package com.example.appstorefit_grupo1.domain.validation

import junit.framework.TestCase.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

//Importamos
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ValidationTest {
    //Emails
    @Test
    fun validateEmail_email_ok() {
        val error = validateEmail("prueba@prueba.cl")
        //Evaluar los criterios de aceptación
        assertNull(error)
    }

    @Test
    fun validateEmail_vacio_da_error() {
        val error = validateEmail("")
        //Evaluar los criterios de aceptación
        assertEquals("El email no puede estar vacío", error)
    }

    @Test
    fun validateEmail_trim_ok() = assertNull(validateEmail("  USER@Test.CL  "))

    @Test
    fun validateEmail_formato_invalido() =
        assertEquals("Formato de correo inválido", validateEmail("usuario@mal"))

    //EemailCanonico
    @Test fun emailCanonico_normaliza() =
        assertEquals("user@test.cl", emailCanonico("  User@Test.CL  "))

    //Validar Contraseña
    @Test fun validateContrasena_vacia() =
        assertEquals("Campo contraseña Obligatorio", validateContraseña(""))

    @Test fun validateContrasena_sin_mayuscula() =
        assertEquals("Debe contener al menos 1 mayuscula", validateContraseña("abc123$%"))

    @Test fun validateContrasena_sin_minuscula() =
        assertEquals("Debe contener al menos 1 minusccula", validateContraseña("ABC123$%"))

    @Test fun validateContrasena_corta() =
        assertEquals("La contraseña debe tener entre 8 y 12 caracteres", validateContraseña("Aa1$12"))

    @Test fun validateContrasena_larga() =
        assertEquals("La contraseña debe tener entre 8 y 12 caracteres", validateContraseña("Aa1$123456789"))

    @Test fun validateContrasena_sin_numero() =
        assertEquals("Debe tener al menos 1 numero", validateContraseña("Aaaaaaa"))

    @Test fun validateContrasena_valida() =
        assertNull(validateContraseña("Aa1aaaa"))

    // validateConfirmarcontrasena
    @Test fun validateConfir_confirm_vacio() =
        assertEquals("Campo confirmar Obligatorio", validateConfir("Aa1aaaa", ""))

    @Test fun validateConfir_no_coincide() =
        assertEquals("Las contraseñas no coinciden", validateConfir("Aa1aaaa", "Aa1aaab"))

    @Test fun validateConfir_ok() =
        assertNull(validateConfir("Aa1aaaa", "Aa1aaaa"))

    // validateNombre
    @Test fun validateNombre_vacio() =
        assertEquals("Campo Nombre Obligatorio", validateNombre(""))

    @Test fun validateNombre_con_numeros() =
        assertEquals("Debe contener solo letras", validateNombre("Juan123"))

    @Test fun validateNombre_valido() =
        assertNull(validateNombre("Juan Pablo"))


    // normalizarTelefono
    @Test fun normalizarTelefono_con_prefijo() =
        assertEquals("912345678", normalizarTelefono("+56 9 1234 5678"))

    @Test fun normalizarTelefono_con_cero() =
        assertEquals("912345678", normalizarTelefono("09-12345678"))

    @Test fun normalizarTelefono_sin_ruido() =
        assertEquals("212345678", normalizarTelefono("212345678"))

    // validateTelefono
    @Test fun validateTelefono_vacio() =
        assertEquals("Campo Teléfono Obligatorio", validateTelefono(""))

    @Test fun validateTelefono_longitud_incorrecta() =
        assertEquals("El teléfono debe tener 9 digitos (Chile)", validateTelefono("12345"))

    @Test fun validateTelefono_con_letras() =
        assertEquals("El teléfono debe contener solo numeros", validateTelefono("91234abcd"))

    @Test fun validateTelefono_repetido() =
        assertEquals("El telefono ingresado no es valido", validateTelefono("111111111"))

    @Test fun validateTelefono_inicia_con_cero() =
        assertEquals("El telefono ingresado no es valido", validateTelefono("012345678"))

    @Test fun validateTelefono_valido() =
        assertNull(validateTelefono("9 1234 5678"))



    // validateRut
    @Test fun validateRut_formato_invalido() =
        assertEquals("Formato RUT invalido. Usa 12.345.678-5", validateRut("12345678-5"))

    @Test fun validateRut_dv_incorrecto() =
        assertEquals("RUT no válido (digito verificador incorrecto)", validateRut("12.345.678-9"))

    @Test fun validateRut_valido() = assertNull(validateRut("12.345.678-5"))

    @Test fun validateRut_valido_con_k_minuscula() = assertNull(validateRut("12.345.678-k"))



    // validateBirthDate
    @Test fun validateBirthDate_vacia() =
        assertEquals("La fecha de nacimiento es obligatoria.", validateBirthDate(""))

    @Test fun validateBirthDate_formato_invalido() =
        assertEquals("Formato invalido. Usa yyyy-MM-dd.", validateBirthDate("12/01/2000"))

    @Test
    fun validateBirthDate_fecha_no_existente() =
        assertEquals("Formato invalido. Usa yyyy-MM-dd.", validateBirthDate("2024-13-01"))

    @Test
    fun validateBirthDate_futura() =
        assertEquals("La fecha no puede ser futura.", validateBirthDate("2999-01-01"))

    @Test fun validateBirthDate_menor_de_edad() =
        assertEquals("Debes tener al menos 15 años para registrarte.", validateBirthDate("2015-12-31", edadMinima = 15))

    @Test fun validateBirthDate_en_borde_minimo() =
        assertNull(validateBirthDate(java.time.LocalDate.now().minusYears(15).toString(), edadMinima = 15))

    @Test fun validateBirthDate_valida() =
        assertNull(validateBirthDate("1990-05-10"))
}

