package com.example.appstorefit_grupo1.ui.ViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appstorefit_grupo1.data.local.Mensaje.MensajeEntity
import com.example.appstorefit_grupo1.data.remote.dto.support.MensajeConRespuestaDto
import com.example.appstorefit_grupo1.data.remote.dto.support.SupportDto
import com.example.appstorefit_grupo1.data.repository.MensajeConRespuesta
import com.example.appstorefit_grupo1.data.repository.MensajeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

data class EnvioMensajeUi(
    val enviando: Boolean = false,
    val ok: Boolean = false,
    val error: String? = null
)

data class RespuestaUi(
    val enviando: Boolean = false,
    val ok: Boolean = false,
    val error: String? = null
)

class MensajesViewModel(
    private val repo: MensajeRepository
) : ViewModel() {

    private val _envio = MutableStateFlow(EnvioMensajeUi())
    val envio: StateFlow<EnvioMensajeUi> = _envio.asStateFlow()

    private val _respuesta = MutableStateFlow(RespuestaUi())
    val respuesta: StateFlow<RespuestaUi> = _respuesta.asStateFlow()

    //----Helpers internos----

    // Convierte un RUT a un Long estable, igual que en SoporteScreen.
    private fun rutToStableLong(rut: String?): Long {
        if (rut.isNullOrBlank()) return 0L
        val onlyDigits = rut.filter { it.isDigit() }
        return onlyDigits.toLongOrNull() ?: rut.hashCode().toLong()
    }

    // Mapear SupportDto (del backend) a MensajeEntity (que usa la UI).
    private fun SupportDto.toMensajeEntity(): MensajeEntity =
        MensajeEntity(
            id = id,
            senderUserId = rutToStableLong(rutRemitente),
            targetRoleId = idRolDestino,
            targetUserId = null, // no lo usamos en la UI
            content = contenido,
            createdAt = creadoEn,
            read = leido,
            isResponse = esRespuesta,
            repliedToId = respondeAId,
            threadId = idHilo,
            respondedAt = respondidoEn
        )

    private fun MensajeConRespuestaDto.toMensajeConRespuestaUi(): MensajeConRespuesta =
        MensajeConRespuesta(
            clienteMensaje = this.clienteMensaje.toMensajeEntity(),
            respuesta = this.respuesta?.toMensajeEntity()
        )

    // ---- Bandejas / lecturas ----

    //Bandeja para soporte.
    //Antes leía de Room, ahora llama al microservicio y devuelve un Flow
    //con MensajeEntity para no romper SoporteScreen.

    fun observarBandejaSoporte(
        idRolSoporte: Int = MensajeRepository.ROL_SOPORTE
    ): Flow<List<MensajeEntity>> = flow {
        // Ignoramos idRolSoporte porque el backend ya filtra por rol SOPORTE
        val remotos: List<MensajeConRespuestaDto> = repo.obtenerBandejaSoporte()
        val lista = remotos.map { it.clienteMensaje.toMensajeEntity() }
        emit(lista)
    }


     // Historial por remitente (cliente): solo lo que ese usuario envió.
     // Ahora se monta a partir de la bandeja del usuario remoto.

    fun observarMensajesDe(idUsuario: Long): Flow<List<MensajeEntity>> = flow {
        val remotos: List<MensajeConRespuestaDto> = repo.obtenerBandejaUsuario()
        val clienteMsgs = remotos.map { it.clienteMensaje.toMensajeEntity() }
        val filtrados = clienteMsgs.filter { it.senderUserId == idUsuario }
        emit(filtrados)
    }


     // Outbox del cliente + posible respuesta del soporte (orden configurable).
     // Se arma a partir de la bandeja de usuario que entrega el backend.

    fun observarOutboxClienteConRespuesta(
        idUsuario: Long,
        asc: Boolean
    ): Flow<List<MensajeConRespuesta>> = flow {
        val remotos: List<MensajeConRespuestaDto> = repo.obtenerBandejaUsuario()
        val convertidos = remotos.map { it.toMensajeConRespuestaUi() }

        val ordenados = if (asc) {
            convertidos.sortedBy { it.clienteMensaje.createdAt }
        } else {
            convertidos.sortedByDescending { it.clienteMensaje.createdAt }
        }

        emit(ordenados)
    }

    // ---- Acciones ----

    //Cliente ⇒ Soporte (usa los headers del SessionManager en el repo)
    fun enviarMensaje(
        idUsuarioRemitente: Long,
        idRolDestinoSoporte: Int,
        contenido: String
    ) {
        viewModelScope.launch {
            _envio.value = EnvioMensajeUi(enviando = true)
            runCatching {
                // Los IDs se obtienen desde la sesión en el repo, así que acá no se usan
                repo.enviarMensajeCliente(contenido)
            }.onSuccess {
                _envio.value = EnvioMensajeUi(ok = true)
            }.onFailure { e ->
                _envio.value = EnvioMensajeUi(error = e.message ?: "Error desconocido")
            }
        }
    }

    // Soporte ⇒ Cliente (responder una vez).
    fun responderMensaje(
        threadId: Long,
        idUsuarioSoporte: Long,
        idUsuarioCliente: Long,
        contenido: String
    ) {
        viewModelScope.launch {
            _respuesta.value = RespuestaUi(enviando = true)
            runCatching {
                // El soporte real (RUT) también se toma desde la sesión en el repo
                repo.responderSoporteAlCliente(threadId, contenido)
            }.onSuccess {
                _respuesta.value = RespuestaUi(ok = true)
            }.onFailure { e ->
                _respuesta.value = RespuestaUi(error = e.message ?: "No se pudo enviar la respuesta")
            }
        }
    }

    //Marcar como leído (por quien lo recibe).
    fun marcarComoLeido(idMensaje: Long) {
        viewModelScope.launch {
            runCatching {
                repo.marcarComoLeido(idMensaje)
            }
        }
    }

    // Reset de estados
    fun limpiarEstadoEnvio() { _envio.value = EnvioMensajeUi() }
    fun limpiarEstadoRespuesta() { _respuesta.value = RespuestaUi() }
}