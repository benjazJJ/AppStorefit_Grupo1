package com.example.appstorefit_grupo1.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.appstorefit_grupo1.data.local.Categoria.CategoriaDao
import com.example.appstorefit_grupo1.data.local.Categoria.CategoriaEntity
import com.example.appstorefit_grupo1.data.local.Productos.ProductosDao
import com.example.appstorefit_grupo1.data.local.Productos.ProductosEntity
import com.example.appstorefit_grupo1.data.local.registro.RegistroDao
import com.example.appstorefit_grupo1.data.local.registro.RegistroEntity
import com.example.appstorefit_grupo1.data.local.rol.RolDao
import com.example.appstorefit_grupo1.data.local.rol.RolEntity
import com.example.appstorefit_grupo1.data.local.user.UserDao
import com.example.appstorefit_grupo1.data.local.user.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// NUEVO: imports del carrito (mantengo lo tuyo)
import com.example.appstorefit_grupo1.data.local.Carrito.CarritoDao
import com.example.appstorefit_grupo1.data.local.Carrito.CarritoEntity

@Database(
    entities = [
        UserEntity::class,
        RegistroEntity::class,
        RolEntity::class,
        CategoriaEntity::class,
        ProductosEntity::class,
        CarritoEntity::class
    ],
    version = 24,                // ↑ subo por encima de 23 (compañero) y 20 (tú)
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun registroDao(): RegistroDao
    abstract fun rolDao(): RolDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun productosDao(): ProductosDao
    abstract fun carritoDao(): CarritoDao

    private class SeedCallback(
        private val scope: CoroutineScope,
        private val provider: () -> AppDatabase?
    ) : Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            scope.launch(Dispatchers.IO) { seed() }
        }

        private suspend fun seed() {
            val appDb = provider() ?: return
            val rDao = appDb.rolDao()
            val uDao = appDb.userDao()
            val regDao = appDb.registroDao()
            val cDao = appDb.categoriaDao()
            val pDao = appDb.productosDao()

            // --- Roles ---
            if (kotlin.runCatching { rDao.count() }.getOrDefault(0) == 0) {
                rDao.insert(RolEntity(rolId = 1L, nombreRol = "CLIENTE"))
                rDao.insert(RolEntity(rolId = 2L, nombreRol = "ADMIN"))
                rDao.insert(RolEntity(rolId = 3L, nombreRol = "SOPORTE"))
            }

            // --- Usuarios + Registro (emails normalizados en lowercase) ---
            suspend fun ensureUser(
                email: String,
                rut: String,
                nombre: String,
                pass: String,
                rolId: Long
            ) {
                val e = email.trim().lowercase()
                if (regDao.getByUsuario(e) == null) {
                    if (uDao.getByRut(rut) == null) {
                        uDao.insertar(
                            UserEntity(
                                rut = rut,
                                name = nombre,
                                email = e,
                                // Mantengo tu teléfono por defecto (no elimino nada tuyo)
                                phone = "12345678",
                                lastName = "",
                                address = "",
                                birthDate = ""
                            )
                        )
                    }
                    regDao.insertar(
                        RegistroEntity(
                            rolId = rolId,
                            usuario = e,
                            contrasenia = pass,
                            rut = rut
                        )
                    )
                }
            }

            ensureUser("a@a.cl", "11.111.111-1", "Admin", "Admin123!", 2L)
            ensureUser("b@b.cl", "22.222.222-2", "Jose",  "Jose123!",  1L)

            // --- Categorías ---
            if (kotlin.runCatching { cDao.count() }.getOrDefault(0) == 0) {
                cDao.insert(CategoriaEntity(nombre = "Poleras"))
                cDao.insert(CategoriaEntity(nombre = "Poleron"))
                cDao.insert(CategoriaEntity(nombre = "Buzo"))
                cDao.insert(CategoriaEntity(nombre = "Conjunto Femenino"))
            }

            // --- Productos (exactamente 4, uno por categoría) ---
            val hayProductos = kotlin.runCatching { pDao.count() }.getOrDefault(0) > 0
            if (!hayProductos) {
                data class ProdBase(val idCat: Long, val modelo: String, val precio: Int)

                val base = listOf(
                    ProdBase(1L, "XFITRX",    9990),   // Poleras
                    ProdBase(2L, "WARMGLIDE", 17990),  // Poleron
                    ProdBase(3L, "FLEXRUN",   14990),  // Buzo
                    ProdBase(4L, "FITQUEEN",  19990)   // Conjunto Femenino
                )

                val colorUnico = "Negro con detalles blancos"
                val tallaUnica = "M"
                val stockIni   = 80

                base.forEach { spec ->
                    pDao.insert(
                        ProductosEntity(
                            idCategoria = spec.idCat,
                            idProducto  = 1L,
                            marca       = "StoreFit",
                            modelo      = spec.modelo,
                            color       = colorUnico,
                            talla       = tallaUnica,
                            precio      = spec.precio,
                            stock       = stockIni
                        )
                    )
                }
            }
        }
    }

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "storefit.db"

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val callback = SeedCallback(
                    scope = CoroutineScope(Dispatchers.IO),
                    provider = { INSTANCE }
                )
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    // Si subes versión y no quieres escribir migraciones ahora,
                    // puedes habilitar esto en desarrollo:
                    // .fallbackToDestructiveMigration()
                    .addCallback(callback)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
