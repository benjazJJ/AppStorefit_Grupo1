// data/local/database/AppDatabase.kt
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
    entities = [UserEntity::class, RegistroEntity::class, RolEntity::class, CategoriaEntity::class],
    version = 7,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun registroDao(): RegistroDao
    abstract fun rolDao(): RolDao
    abstract fun categoriaDao(): CategoriaDao

    private class SeedCallback(
        private val scope: CoroutineScope,
        private val provider: () -> AppDatabase?
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            seed()
        }
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            // opcional si quieres “backfill” al abrir
            seed()
        }
        private fun seed() = scope.launch(Dispatchers.IO) {
            val appDb = provider() ?: return@launch
            val rDao = appDb.rolDao()
            val uDao = appDb.userDao()
            val regDao = appDb.registroDao()
            val cDao = appDb.categoriaDao()

            // Roles
            if (kotlin.runCatching { rDao.count() }.getOrDefault(0) == 0) {
                rDao.insert(RolEntity(rolId = 1L, nombreRol = "CLIENTE"))
                rDao.insert(RolEntity(rolId = 2L, nombreRol = "ADMIN"))
                rDao.insert(RolEntity(rolId = 3L, nombreRol = "SOPORTE"))
            }

            // Admin
            if (regDao.getByUsuario("a@a.cl") == null) {
                if (uDao.getByRut("11.111.111-1") == null) {
                    uDao.insertar(
                        UserEntity(
                            rut = "11.111.111-1",
                            name = "Admin",
                            email = "a@a.cl",
                            phone = "12345678",
                            lastName = "",
                            address = "",
                            birthDate = ""
                        )
                    )
                }
                regDao.insertar(
                    RegistroEntity(
                        rolId = 2L,
                        usuario = "a@a.cl",
                        contrasenia = "Admin123!",
                        rut = "11.111.111-1"
                    )
                )
            }

            // Cliente
            if (regDao.getByUsuario("b@b.cl") == null) {
                if (uDao.getByRut("22.222.222-2") == null) {
                    uDao.insertar(
                        UserEntity(
                            rut = "22.222.222-2",
                            name = "Jose",
                            email = "b@b.cl",
                            phone = "12345678",
                            lastName = "Perez",
                            address = "Av. Felicia 213",
                            birthDate = ""
                        )
                    )
                }
                regDao.insertar(
                    RegistroEntity(
                        rolId = 1L,
                        usuario = "b@b.cl",
                        contrasenia = "Jose123!",
                        rut = "22.222.222-2"
                    )
                )
            }

            // Categorías
            if (kotlin.runCatching { cDao.count() }.getOrDefault(0) == 0) {
                cDao.insert(CategoriaEntity(nombre = "Remeras", descripcion = "Remeras deportivas"))
                cDao.insert(CategoriaEntity(nombre = "Zapatillas", descripcion = "Calzado deportivo"))
                cDao.insert(CategoriaEntity(nombre = "Gorras", descripcion = "Gorras y accesorios"))
            }
        }
    }

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "ui_navegacion.db"

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Creamos el builder con callback que accede a la MISMA instancia ya creada
                val callback = SeedCallback(
                    scope = CoroutineScope(Dispatchers.IO),
                    provider = { INSTANCE }     // << evita llamar getInstance()
                )
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addCallback(callback)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
