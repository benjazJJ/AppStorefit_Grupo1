package com.example.appstorefit_grupo1.ui.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appstorefit_grupo1.data.local.user.AdminUserRow
import com.example.appstorefit_grupo1.data.local.user.UserEntity
import com.example.appstorefit_grupo1.data.local.rol.AdminRoleRow
import com.example.appstorefit_grupo1.data.repository.UserRepository
import com.example.appstorefit_grupo1.domain.validation.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// UI STATE
data class AdminUsuariosUiState(
    // Lista y carga
    val cargando: Boolean = false,
    val usuarios: List<AdminUserRow> = emptyList(),
    val mensajeError: String? = null,

    // Eliminar
    val rutAConfirmarEliminacion: String? = null,

    // Asignar rol
    val mostrarAsignarRol: Boolean = false,
    val rutParaAsignar: String = "",
    val rolesDisponibles: List<AdminRoleRow> = emptyList(),
    val rolSeleccionadoId: Long? = null,
    val asignandoRol: Boolean = false,

    // Crear usuario (UI)
    val mostrarCrear: Boolean = false,
    val creando: Boolean = false,
    val puedeCrear: Boolean = false,

    val cRut: String = "",
    val cNombre: String = "",
    val cEmail: String = "",
    val cTelefono: String = "",
    val cDireccion: String = "",
    val cNacimiento: String = "",
    val cPassword: String = "",


    val cRolId: Long? = null,
    val cRolNombreSeleccionado: String? = null,

    // Errores crear
    val errRut: String? = null,
    val errNombre: String? = null,
    val errEmail: String? = null,
    val errTelefono: String? = null,
    val errNacimiento: String? = null,
    val errRol: String? = null,
    val errPassword: String? = null,


    // Editar usuario (por RUT PK)
    val mostrarEditar: Boolean = false,
    val eRutPk: String = "",
    val eNombre2: String = "",
    val eEmail2: String = "",
    val eTelefono2: String = "",
    val eDireccion2: String = "",
    val eNacimiento2: String = "",

    // Errores editar
    val errNombre2: String? = null,
    val errTelefono2: String? = null,
    val errNacimiento2: String? = null,

    val puedeEditar: Boolean = false,
    val editando: Boolean = false
)

// VIEWMODEL
class AdminUsuariosViewModel(
    private val repositorio: UserRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(AdminUsuariosUiState(cargando = true))
    val ui: StateFlow<AdminUsuariosUiState> = _ui

    init { recargarUsuarios() }

    // LISTAR / RECARGAR
    fun recargarUsuarios() {
        viewModelScope.launch {
            _ui.update { it.copy(cargando = true, mensajeError = null) }
            val res = repositorio.adminListUsers()
            _ui.update { st ->
                res.fold(
                    onSuccess = { lista -> st.copy(cargando = false, usuarios = lista, mensajeError = null) },
                    onFailure = { err -> st.copy(cargando = false, mensajeError = err.message ?: "Error cargando usuarios") }
                )
            }
        }
    }

    // CREAR
    fun abrirCrear() {
        viewModelScope.launch {
            // Limpia estado y abre diálogo
            _ui.update {
                it.copy(
                    mostrarCrear = true,
                    creando = false,
                    puedeCrear = false,
                    cRut = "", cNombre = "", cEmail = "", cTelefono = "", cDireccion = "", cNacimiento = "",
                    cRolId = null, cRolNombreSeleccionado = null,
                    errRut = null, errNombre = null, errEmail = null, errTelefono = null, errNacimiento = null, errRol = null,
                    mensajeError = null,
                    cPassword = "",
                    errPassword = null,
                    )
            }
            // Cargar roles si está vacío
            if (_ui.value.rolesDisponibles.isEmpty()) {
                val resRoles = repositorio.adminListRoles()
                _ui.update { st ->
                    resRoles.fold(
                        onSuccess = { roles -> st.copy(rolesDisponibles = roles) },
                        onFailure = { err -> st.copy(mensajeError = err.message ?: "No se pudieron cargar los roles") }
                    )
                }
            }
        }
    }

    fun cerrarCrear() { _ui.update { it.copy(mostrarCrear = false) } }

    fun onCambiarRutCrear(v: String) {
        val t = v.trim()
        _ui.update { it.copy(cRut = t, errRut = validateRut(t)).recalcCrear() }
    }

    fun onCambiarNombreCrear(v: String) {
        val f = v.filter { it.isLetter() || it.isWhitespace() }
        _ui.update { it.copy(cNombre = f, errNombre = validateNombre(f)).recalcCrear() }
    }

    fun onCambiarEmailCrear(v: String) {
        val t = v.trim()
        _ui.update { it.copy(cEmail = t, errEmail = validateEmail(t)).recalcCrear() }
    }

    fun onCambiarTelefonoCrear(v: String) {
        val d = v.filter { it.isDigit() }
        _ui.update { it.copy(cTelefono = d, errTelefono = if (d.isBlank()) null else validateTelefono(d)).recalcCrear() }
    }

    fun onCambiarDireccionCrear(v: String) {
        _ui.update { it.copy(cDireccion = v).recalcCrear() }
    }

    fun onCambiarNacimientoCrear(v: String) {
        val t = v.trim()
        _ui.update { it.copy(cNacimiento = t, errNacimiento = if (t.isBlank()) "Fecha requerida" else validateBirthDate(t)).recalcCrear() }
    }

    fun onSeleccionarRolCrear(rolId: Long, rolNombre: String) {
        _ui.update { it.copy(cRolId = rolId, cRolNombreSeleccionado = rolNombre, errRol = null).recalcCrear() }
    }

    fun onCambiarPasswordCrear(v: String) {
        val p = v.trim()
        val err = if (p.length < 6) "Mínimo 6 caracteres" else null
        _ui.update { it.copy(cPassword = p, errPassword = err).recalcCrear() }
    }


    private fun AdminUsuariosUiState.recalcCrear(): AdminUsuariosUiState {
        val sinErrores = listOf(errRut, errNombre, errEmail, errTelefono, errNacimiento, errRol, errPassword).all { it == null }
        val completos = cRut.isNotBlank() && cNombre.isNotBlank() && cEmail.isNotBlank() &&
                cNacimiento.isNotBlank() && cRolId != null && cPassword.isNotBlank()
        return copy(puedeCrear = sinErrores && completos)
    }


    fun confirmarCrear() {
        val s = _ui.value
        if (!s.puedeCrear || s.creando) return

        viewModelScope.launch {
            _ui.update { it.copy(creando = true, mensajeError = null) }

            val usuario = UserEntity(
                rut = s.cRut,
                name = s.cNombre,
                email = s.cEmail,
                phone = if (s.cTelefono.isBlank()) null else s.cTelefono,
                address = s.cDireccion,
                birthDate = s.cNacimiento
            )

            val res = repositorio.adminCreateUser(usuario, s.cPassword, s.cRolId ?: -1L)

            if (res.isSuccess) {
                _ui.update {
                    it.copy(
                        mostrarCrear = false,
                        creando = false,
                        puedeCrear = false,
                        cRut = "", cNombre = "", cEmail = "", cTelefono = "", cDireccion = "", cNacimiento = "",
                        cRolId = null, cRolNombreSeleccionado = null,
                        errRut = null, errNombre = null, errEmail = null, errTelefono = null, errNacimiento = null, errRol = null,
                        cPassword = "",
                        errPassword = null,
                        )
                }
                recargarUsuarios()
            } else {
                _ui.update { it.copy(creando = false, mensajeError = res.exceptionOrNull()?.message) }
            }
        }
    }

    // EDITAR

    fun abrirEditar(rut: String) {
        // Edición de datos deshabilitada: solo se permite asignar roles
        _ui.update { it.copy(mensajeError = "La edición de datos está deshabilitada. Usa Asignar rol.") }
    }

    fun cerrarEditar() { _ui.update { it.copy(mostrarEditar = false) } }

    fun onCambiarNombreEditar(v: String) {
        val f = v.filter { it.isLetter() || it.isWhitespace() }
        _ui.update { it.copy(eNombre2 = f, errNombre2 = validateNombre(f)).recalcularEditar() }
    }

    fun onCambiarTelefonoEditar(v: String) {
        val d = v.filter { it.isDigit() }
        _ui.update { it.copy(eTelefono2 = d, errTelefono2 = if (d.isBlank()) null else validateTelefono(d)).recalcularEditar() }
    }

    private fun validarNacimientoSeguro(v: String): String? =
        if (v.isBlank()) null else validateBirthDate(v)

    private fun AdminUsuariosUiState.recalcularEditar(): AdminUsuariosUiState {
        val sinErrores = listOf(errNombre2, errTelefono2, errNacimiento2).all { it == null }
        val minimos = eRutPk.isNotBlank() && eNombre2.isNotBlank()
        return copy(puedeEditar = sinErrores && minimos)
    }

    fun confirmarEditar() {
        // Edición de datos deshabilitada
    }

    fun abrirAsignarRol(rut: String) {
        viewModelScope.launch {
            _ui.update { it.copy(mostrarAsignarRol = true, rutParaAsignar = rut, mensajeError = null) }
            val resRoles = repositorio.adminListRoles()
            _ui.update { st ->
                resRoles.fold(
                    onSuccess = { roles -> st.copy(rolesDisponibles = roles, rolSeleccionadoId = roles.firstOrNull()?.id) },
                    onFailure = { err -> st.copy(mensajeError = err.message ?: "No se pudieron cargar los roles") }
                )
            }
        }
    }

    fun cerrarAsignarRol() {
        _ui.update { it.copy(mostrarAsignarRol = false, rutParaAsignar = "", rolSeleccionadoId = null) }
    }

    fun onSeleccionarRol(idRol: Long) {
        _ui.update { it.copy(rolSeleccionadoId = idRol) }
    }

    fun confirmarAsignarRol() {
        val s = _ui.value
        val rut = s.rutParaAsignar
        val rolId = s.rolSeleccionadoId ?: return
        if (rut.isBlank() || s.asignandoRol) return

        viewModelScope.launch {
            _ui.update { it.copy(asignandoRol = true, mensajeError = null) }
            val res = repositorio.adminAssignRoleToUser(rut, rolId)
            if (res.isSuccess) {
                _ui.update { it.copy(asignandoRol = false, mostrarAsignarRol = false) }
                recargarUsuarios()
            } else {
                _ui.update { it.copy(asignandoRol = false, mensajeError = res.exceptionOrNull()?.message) }
            }
        }
    }
}
