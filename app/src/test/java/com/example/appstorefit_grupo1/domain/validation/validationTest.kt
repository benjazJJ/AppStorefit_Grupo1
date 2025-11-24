package com.example.appstorefit_grupo1.domain.validation

import junit.framework.TestCase.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

//Importamos
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ValidationTest{
    @Test
    fun validateEmail_email_ok(){
        val error = validateEmail("prueba@prueba.cl")
        //Evaluar los criterios de aceptación
        assertNull(error)
    }

    @Test
    fun validateEmail_vacio_da_error(){
        val error = validateEmail("")
    }
}