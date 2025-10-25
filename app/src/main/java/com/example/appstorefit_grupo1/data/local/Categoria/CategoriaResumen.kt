package com.example.appstorefit_grupo1.data.local.Categoria


//ESTE ES UN DTO PARA MOSTRARLO EN EL APARTADO DE CATEGORIAS DEL ADMIN, ID Y NOMBRE DE LA CATEGORÍA
data class CategoriaResumen(
    val id: Long,
    val nombre: String,
    val productos: Int,
    val modelos: Int
)