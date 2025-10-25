package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.local.Categoria.CategoriaDao
import com.example.appstorefit_grupo1.data.local.Categoria.CategoriaEntity
import com.example.appstorefit_grupo1.data.local.Categoria.CategoriaResumen
import kotlinx.coroutines.flow.Flow

class CategoriaRepository(private val dao: CategoriaDao) {

    // Crea una categoría validando que tenga nombre y que no esté repetido
    suspend fun create(nombre: String): Result<Long> {
        if (nombre.isBlank()) {
            return Result.failure(IllegalArgumentException("El nombre de la categoría no puede estar vacío"))
        }

        val existe = dao.getByNombre(nombre.trim())
        if (existe != null) {
            return Result.failure(IllegalStateException("La categoría ya existe"))
        }

        val id = dao.insert(
            CategoriaEntity(
                nombre = nombre.trim()
            )
        )
        return Result.success(id)
    }

    // Devuelve todas las categorías tal cual están en la base
    suspend fun getAll(): Result<List<CategoriaEntity>> {
        val list = dao.getAll()
        return Result.success(list)
    }

    // Busca una categoría por su id
    suspend fun getById(id: Long): Result<CategoriaEntity> {
        val cat = dao.getById(id) ?: return Result.failure(IllegalArgumentException("Categoría no encontrada"))
        return Result.success(cat)
    }

    // Actualiza la categoría (por ahora solo nombre). Valida que no choquemos con otro nombre existente.
    suspend fun update(categoria: CategoriaEntity): Result<Unit> {
        if (categoria.nombre.isBlank()) {
            return Result.failure(IllegalArgumentException("Nombre vacío"))
        }

        // Si hay otra categoría con este nombre y distinto id, no dejamos actualizar
        val existing = dao.getByNombre(categoria.nombre)
        if (existing != null && existing.id != categoria.id) {
            return Result.failure(IllegalStateException("Otra categoría ya usa ese nombre"))
        }

        val rows = dao.update(categoria)
        return if (rows > 0) Result.success(Unit)
        else Result.failure(IllegalStateException("No se pudo actualizar la categoría"))
    }

    // Resumen reactivo para la pestaña de Categorías
    // Entrega: id, nombre, cantidad de variantes (producto) y cantidad de modelos distintos
    fun observeResumen(): Flow<List<CategoriaResumen>> = dao.observeResumen()

    // ===== Renombrar directo por id =====
    // Ideal para el diálogo del admin. No borra nada.
    suspend fun renombrar(id: Long, nuevoNombre: String): Result<Unit> {
        val nombre = nuevoNombre.trim()
        if (nombre.isBlank()) return Result.failure(IllegalArgumentException("Nombre requerido"))

        // Si ya existe otra categoría con este nombre, frenamos
        val existing = dao.getByNombre(nombre)
        if (existing != null && existing.id != id) {
            return Result.failure(IllegalStateException("Otra categoría ya usa ese nombre"))
        }

        val rows = dao.renombrar(id, nombre)
        return if (rows > 0) Result.success(Unit)
        else Result.failure(IllegalStateException("No se pudo renombrar la categoría"))
    }
}
