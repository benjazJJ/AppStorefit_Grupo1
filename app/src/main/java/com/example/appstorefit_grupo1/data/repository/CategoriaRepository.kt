package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.local.Categoria.CategoriaDao
import com.example.appstorefit_grupo1.data.local.Categoria.CategoriaEntity

class CategoriaRepository(private val dao: CategoriaDao) {

    // Crear nueva categoría (valida nombre no vacío y no duplicado)
    suspend fun create(nombre: String): Result<Long> {
        if (nombre.isBlank()) {
            return Result.failure(IllegalArgumentException("El nombre de la categoría no puede estar vacío"))
        }

        val exists = dao.getByNombre(nombre.trim())
        if (exists != null) {
            return Result.failure(IllegalStateException("La categoría ya existe"))
        }

        val id = dao.insert(
            CategoriaEntity(
                nombre = nombre.trim()
            )
        )
        return Result.success(id)
    }

    // Obtener todas
    suspend fun getAll(): Result<List<CategoriaEntity>> {
        val list = dao.getAll()
        return Result.success(list)
    }

    suspend fun getById(id: Long): Result<CategoriaEntity> {
        val cat = dao.getById(id) ?: return Result.failure(IllegalArgumentException("Categoría no encontrada"))
        return Result.success(cat)
    }

    suspend fun update(categoria: CategoriaEntity): Result<Unit> {
        if (categoria.nombre.isBlank()) {
            return Result.failure(IllegalArgumentException("Nombre vacío"))
        }

        // Validar duplicado por otro id
        val existing = dao.getByNombre(categoria.nombre)
        if (existing != null && existing.id != categoria.id) {
            return Result.failure(IllegalStateException("Otra categoría ya usa ese nombre"))
        }

        val updated = dao.update(categoria)
        return if (updated > 0) Result.success(Unit)
        else Result.failure(IllegalStateException("No se pudo actualizar la categoría"))
    }

    suspend fun delete(id: Long): Result<Boolean> {
        val entity = dao.getById(id) ?: return Result.failure(IllegalArgumentException("Categoría no encontrada"))
        val deleted = dao.delete(entity)
        return if (deleted > 0) Result.success(true)
        else Result.failure(IllegalStateException("No se pudo eliminar la categoría"))
    }
}