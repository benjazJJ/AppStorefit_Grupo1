package com.example.appstorefit_grupo1.data.local.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val rut: String,
    val name: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val address: String,
    val birthDate: String,
    val photoUri: String? = null
)
