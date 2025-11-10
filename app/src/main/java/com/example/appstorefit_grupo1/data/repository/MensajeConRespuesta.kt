package com.example.appstorefit_grupo1.data.repository

import androidx.room.Embedded
import com.example.appstorefit_grupo1.data.local.Mensaje.MensajeEntity

data class MensajeConRespuesta(
    @Embedded(prefix = "cliente_")
    val clienteMensaje: MensajeEntity,

    @Embedded(prefix = "resp_")
    val respuesta: MensajeEntity?
)
