package com.example.appstorefit_grupo1.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.appstorefit_grupo1.data.local.Categoria.CategoriaDao
import com.example.appstorefit_grupo1.data.local.Categoria.CategoriaEntity
import com.example.appstorefit_grupo1.data.local.registro.RegistroDao
import com.example.appstorefit_grupo1.data.local.registro.RegistroEntity
import com.example.appstorefit_grupo1.data.local.rol.RolDao
import com.example.appstorefit_grupo1.data.local.rol.RolEntity
import com.example.appstorefit_grupo1.data.local.user.UserDao
import com.example.appstorefit_grupo1.data.local.user.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    // añadir aquí todas las entidades que usará la app (agregada CategoriaEntity)
    entities = [UserEntity::class, RegistroEntity::class, RolEntity::class, CategoriaEntity::class],

    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun registroDao(): RegistroDao
    abstract fun rolDao(): RolDao
    abstract fun categoriaDao(): CategoriaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "ui_navegacion.db"

        // obtener la instancia de la BD
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // instancia auxiliar para crear la BD
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // lanzar una corutina para los insert de las tablas
                            CoroutineScope(Dispatchers.IO).launch {
                                kotlin.runCatching {
                                    val rDao = getInstance(context).rolDao()
                                    val rolesCount = rDao.count()
                                    if (rolesCount == 0) {
                                        // 1 = CLIENTE, 2 = ADMIN, 3 = SOPORTE
                                        rDao.insert(RolEntity(rolId = 1L, nombreRol = "CLIENTE"))
                                        rDao.insert(RolEntity(rolId = 2L, nombreRol = "ADMIN"))
                                        rDao.insert(RolEntity(rolId = 3L, nombreRol = "SOPORTE"))
                                    }
                                }

                                kotlin.runCatching {
                                    val uDao = getInstance(context).userDao()
                                    val regDao = getInstance(context).registroDao()

                                    if (uDao.count() == 0 && regDao.count() == 0) {
                                        // Usuario ADMIN
                                        uDao.insertar(
                                            UserEntity(
                                                rut = "11.111.111-1",
                                                name = "Admin",
                                                email = "a@a.cl",
                                                phone = "12345678",
                                                lastName = "",
                                                address = "",
                                                birthDate = "",
                                                registerDate = ""
                                            )
                                        )
                                        regDao.insertar(
                                            RegistroEntity(
                                                rolId = 2L,                // ADMIN
                                                usuario = "a@a.cl",
                                                contrasenia = "Admin123!", // demo
                                                rut = "11.111.111-1"
                                            )
                                        )

                                        // Usuario CLIENTE
                                        uDao.insertar(
                                            UserEntity(
                                                rut = "22.222.222-2",
                                                name = "Jose",
                                                email = "b@b.cl",
                                                phone = "12345678",
                                                lastName = "Perez",
                                                address = "Av. Felicia 213",
                                                birthDate = "05/05/1990",
                                            )
                                        )
                                        regDao.insertar(
                                            RegistroEntity(
                                                rolId = 1L,               // CLIENTE
                                                usuario = "b@b.cl",
                                                contrasenia = "Jose123!",
                                                rut = "22.222.222-2"
                                            )
                                        )
                                    }
                                }.onFailure {
                                    // opcional: log o manejo del fallo
                                }

                                // ====== CATEGORÍAS (seed) ======
                                kotlin.runCatching {
                                    val cDao = getInstance(context).categoriaDao()
                                    val catCount = cDao.count()
                                    if (catCount == 0) {
                                        cDao.insert(CategoriaEntity(nombre = "Remeras", descripcion = "Remeras deportivas"))
                                        cDao.insert(CategoriaEntity(nombre = "Zapatillas", descripcion = "Calzado deportivo"))
                                        cDao.insert(CategoriaEntity(nombre = "Gorras", descripcion = "Gorras y accesorios"))
                                    }
                                }.onFailure {
                                    // opcional: log o manejo del fallo
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}