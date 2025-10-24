package com.example.appstorefit_grupo1.ViewModel

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

    // Crear usuario
    val mostrarCrear: Boolean = false,
    val cRut: String = "",
    val cNombre: String = "",
    val cEmail: String = "",
    val cTelefono: String = "",
    val cDireccion: String = "",
    val cContrasena: String = "",
    val cRolId: Long = 1L,

    // Errores crear
    val eRut: String? = null,
    val eNombre: String? = null,
    val eEmail: String? = null,
    val eTelefono: String? = null,
    val eContrasena: String? = null,

    val puedeCrear: Boolean = false,
    val creando: Boolean = false,

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

//VIEWMODEL
class AdminUsuariosViewModel(
    private val repositorio: UserRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(AdminUsuariosUiState(cargando = true))
    val ui: StateFlow<AdminUsuariosUiState> = _ui

    init { recargarUsuarios() }

    //LISTAR / RECARGAR
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

    //ELIMINAR
    fun solicitarEliminar(rut: String) { _ui.update { it.copy(rutAConfirmarEliminacion = rut) } }
    fun cancelarEliminar() { _ui.update { it.copy(rutAConfirmarEliminacion = null) } }
    fun confirmarEliminar() {
        val rut = _ui.value.rutAConfirmarEliminacion ?: return
        viewModelScope.launch {
            val res = repositorio.adminDeleteUserByRut(rut)
            _ui.update { it.copy(rutAConfirmarEliminacion = null) }
            if (res.isSuccess) recargarUsuarios()
            else _ui.update { it.copy(mensajeError = res.exceptionOrNull()?.message) }
        }
    }

    //CREAR
    fun abrirCrear() { _ui.update { it.copy(mostrarCrear = true) } }
    fun cerrarCrear() { _ui.update { it.copy(mostrarCrear = false) } }

    fun onCambiarRutCrear(v: String) {
        val t = v.trim()
        _ui.update { it.copy(cRut = t, eRut = validateRut(t)).recalcularCrear() }
    }

    fun onCambiarNombreCrear(v: String) {
        val f = v.filter { it.isLetter() || it.isWhitespace() }
        _ui.update { it.copy(cNombre = f, eNombre = validateNombre(f)).recalcularCrear() }
    }

    fun onCambiarEmailCrear(v: String) {
        val t = v.trim()
        _ui.update { it.copy(cEmail = t, eEmail = validateEmail(t)).recalcularCrear() }
    }

    fun onCambiarTelefonoCrear(v: String) {
        val d = v.filter { it.isDigit() }
        _ui.update { it.copy(cTelefono = d, eTelefono = if (d.isBlank()) null else validateTelefono(d)).recalcularCrear() }
    }

    fun onCambiarDireccionCrear(v: String) { _ui.update { it.copy(cDireccion = v) } }

    fun onCambiarContrasenaCrear(v: String) {
        _ui.update { it.copy(cContrasena = v, eContrasena = validateContraseña(v)).recalcularCrear() }
    }

    fun onCambiarRolCrear(rolId: Long) { _ui.update { it.copy(cRolId = rolId) } }

    private fun AdminUsuariosUiState.recalcularCrear(): AdminUsuariosUiState {
        val sinErrores = listOf(eRut, eNombre, eEmail, eTelefono, eContrasena).all { it == null }
        val completos = cRut.isNotBlank() && cNombre.isNotBlank() && cEmail.isNotBlank() && cContrasena.isNotBlank()
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
                birthDate = ""
            )

            val res = repositorio.adminCreateUser(usuario, s.cContrasena, s.cRolId)
            if (res.isSuccess) {
                _ui.update {
                    it.copy(
                        mostrarCrear = false,
                        creando = false,
                        cRut = "", cNombre = "", cEmail = "", cTelefono = "", cDireccion = "", cContrasena = "",
                        puedeCrear = false,
                        eRut = null, eNombre = null, eEmail = null, eTelefono = null, eContrasena = null
                    )
                }
                recargarUsuarios()
            } else {
                _ui.update { it.copy(creando = false, mensajeError = res.exceptionOrNull()?.message) }
            }
        }
    }

    //EDITAR
    fun abrirEditar(rut: String) {
        viewModelScope.launch {
            val res = repositorio.adminGetUserByRut(rut)
            res.onSuccess { u ->
                _ui.update {
                    it.copy(
                        mostrarEditar = true,
                        eRutPk = u.rut,
                        eNombre2 = u.name,
                        eEmail2 = u.email,
                        eTelefono2 = u.phone.orEmpty(),
                        eDireccion2 = u.address,
                        eNacimiento2 = u.birthDate,
                        errNombre2 = null,
                        errTelefono2 = null,
                        errNacimiento2 = validarNacimientoSeguro(u.birthDate),
                    ).recalcularEditar()
                }
            }.onFailure { err ->
                _ui.update { it.copy(mensajeError = err.message ?: "No se pudo cargar el usuario para editar") }
            }
        }
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

    fun onCambiarDireccionEditar(v: String) { _ui.update { it.copy(eDireccion2 = v).recalcularEditar() } }

    fun onCambiarNacimientoEditar(v: String) {
        _ui.update { it.copy(eNacimiento2 = v, errNacimiento2 = validarNacimientoSeguro(v)).recalcularEditar() }
    }

    private fun validarNacimientoSeguro(v: String): String? =
        if (v.isBlank()) null else validateBirthDate(v)

    private fun AdminUsuariosUiState.recalcularEditar(): AdminUsuariosUiState {
        val sinErrores = listOf(errNombre2, errTelefono2, errNacimiento2).all { it == null }
        val minimos = eRutPk.isNotBlank() && eNombre2.isNotBlank()
        return copy(puedeEditar = sinErrores && minimos)
    }

    fun confirmarEditar() {
        val s = _ui.value
        if (!s.puedeEditar || s.editando) return

        viewModelScope.launch {
            _ui.update { it.copy(editando = true, mensajeError = null) }

            val usuario = UserEntity(
                rut = s.eRutPk,
                name = s.eNombre2,
                email = s.eEmail2,
                phone = if (s.eTelefono2.isBlank()) null else s.eTelefono2,
                address = s.eDireccion2,
                birthDate = s.eNacimiento2
            )

            val res = repositorio.adminUpdateUser(usuario)
            if (res.isSuccess) {
                _ui.update { it.copy(mostrarEditar = false, editando = false) }
                recargarUsuarios()
            } else {
                _ui.update { it.copy(editando = false, mensajeError = res.exceptionOrNull()?.message) }
            }
        }
    }

    //ASIGNAR ROL
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
