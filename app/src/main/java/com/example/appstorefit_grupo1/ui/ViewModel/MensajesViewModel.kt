package com.example.appstorefit_grupo1.ui.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appstorefit_grupo1.data.local.Mensaje.MensajeEntity
import com.example.appstorefit_grupo1.data.repository.MensajeConRespuesta
import com.example.appstorefit_grupo1.data.repository.MensajeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
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

    // ====== Bandejas / lecturas ======

    /** Bandeja para soporte (targetRoleId = 3 por defecto). */
    fun observarBandejaSoporte(idRolSoporte: Int = MensajeRepository.ROL_SOPORTE): Flow<List<MensajeEntity>> =
        repo.observarBandejaSoporte(idRolSoporte)

    /** Historial por remitente (cliente): solo lo que ese usuario envió. */
    fun observarMensajesDe(idUsuario: Long): Flow<List<MensajeEntity>> =
        repo.observarMensajesDe(idUsuario)

    /** Outbox del cliente con la posible respuesta del soporte (orden configurable). */
    fun observarOutboxClienteConRespuesta(
        idUsuario: Long,
        asc: Boolean
    ): Flow<List<MensajeConRespuesta>> =
        repo.observarOutboxClienteConRespuesta(idUsuario, asc)

    // ====== Acciones ======

    /** Cliente ⇒ Soporte */
    fun enviarMensaje(idUsuarioRemitente: Long, idRolDestinoSoporte: Int, contenido: String) {
        viewModelScope.launch {
            _envio.value = EnvioMensajeUi(enviando = true)
            runCatching {
                repo.enviarMensaje(idUsuarioRemitente, idRolDestinoSoporte, contenido)
            }.onSuccess {
                _envio.value = EnvioMensajeUi(ok = true)
            }.onFailure { e ->
                _envio.value = EnvioMensajeUi(error = e.message ?: "Error desconocido")
            }
        }
    }

    /** Soporte ⇒ Cliente (Responder una vez) */
    fun responderMensaje(
        threadId: Long,
        idUsuarioSoporte: Long,
        idUsuarioCliente: Long,
        contenido: String
    ) {
        viewModelScope.launch {
            _respuesta.value = RespuestaUi(enviando = true)
            runCatching {
                repo.responderSoporteAlCliente(threadId, idUsuarioSoporte, idUsuarioCliente, contenido)
            }.onSuccess {
                _respuesta.value = RespuestaUi(ok = true)
            }.onFailure { e ->
                _respuesta.value = RespuestaUi(error = e.message ?: "No se pudo enviar la respuesta")
            }
        }
    }

    /** Marcar como leído (por quien lo recibe). */
    fun marcarComoLeido(idMensaje: Long) {
        viewModelScope.launch { runCatching { repo.marcarComoLeido(idMensaje) } }
    }

    /** Reset de estados */
    fun limpiarEstadoEnvio() { _envio.value = EnvioMensajeUi() }
    fun limpiarEstadoRespuesta() { _respuesta.value = RespuestaUi() }
}
