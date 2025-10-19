package com.example.appstorefit_grupo1.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.appstorefit_grupo1.data.local.user.UserDao
import com.example.appstorefit_grupo1.data.local.user.UserEntity
import com.example.appstorefit_grupo1.data.local.rol.RolDao
import com.example.appstorefit_grupo1.data.local.rol.RolEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Database(
    // añadir aquí todas las entidades que usará la app
    // AÑADIDO: RolEntity para registrar la tabla 'rol'
    entities = [UserEntity::class, RolEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun rolDao(): RolDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "ui_navegacion.db"

        // obtener la instancia de la BD
        fun getInstance(context: Context): AppDatabase{
            return INSTANCE ?: synchronized(this){
                // instancias auxiliar para crear la BD
                var instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addCallback(object : RoomDatabase.Callback(){
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // lanzar una corutina para los insert de las tablas
                            CoroutineScope(Dispatchers.IO).launch {
                                // repetir por cada tabla con insert
                                val dao = getInstance(context).userDao()
                                // genero la lista de los insert
                                val seed = listOf(
                                    UserEntity(
                                        name = "Admin",
                                        email = "a@a.cl",
                                        phone = "12345678",
                                        pass = "Admin123!"
                                    ),
                                    UserEntity(
                                        name = "Jose",
                                        email = "b@b.cl",
                                        phone = "12345678",
                                        pass = "Jose123!"
                                    )
                                )
                                if(dao.count() == 0){
                                    seed.forEach { dao.insertar(it) }
                                }
                                // Insertar roles base solo si la tabla está vacía.
                                kotlin.runCatching {
                                    val rolDao = getInstance(context).rolDao()
                                    val rolesCount = rolDao.count()
                                    if (rolesCount == 0) {
                                        // 1 = CLIENTE, 2 = ADMIN, 3 = SOPORTE
                                        rolDao.insert(
                                            RolEntity(
                                                rolId = 1L,
                                                nombreRol = "CLIENTE"
                                            )
                                        )
                                        rolDao.insert(
                                            RolEntity(
                                                rolId = 2L,
                                                nombreRol = "ADMIN"
                                            )
                                        )
                                        rolDao.insert(
                                            RolEntity(
                                                rolId = 3L,
                                                nombreRol = "SOPORTE"
                                            )
                                        )
                                    }
                                }.onFailure {
                                }
                            }
                        }
                    }).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

    }
}
