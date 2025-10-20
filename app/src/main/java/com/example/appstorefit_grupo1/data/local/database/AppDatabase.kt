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

@Database(
    entities = [
        UserEntity::class,
        RegistroEntity::class,
        RolEntity::class,
        CategoriaEntity::class,
        ProductosEntity::class
    ],
    version = 13,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun registroDao(): RegistroDao
    abstract fun rolDao(): RolDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun productosDao(): ProductosDao

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
            suspend fun ensureUser(email: String, rut: String, nombre: String, pass: String, rolId: Long) {
                val e = email.trim().lowercase()
                if (regDao.getByUsuario(e) == null) {
                    if (uDao.getByRut(rut) == null) {
                        uDao.insertar(
                            UserEntity(
                                rut = rut,
                                name = nombre,
                                email = e,
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

            // --- Productos ---
            val hayProductos = kotlin.runCatching { pDao.count() }.getOrDefault(0) > 0
            if (!hayProductos) {
                val preciosPorCategoria = mapOf(
                    1L to 9990,
                    2L to 17990,
                    3L to 14990,
                    4L to 19990
                )
                val modeloPorCategoria = mapOf(
                    1L to "XFITRX",
                    2L to "WARMGLIDE",
                    3L to "FLEXRUN",
                    4L to "FITQUEEN"
                )
                val tallas = listOf("XS","S","M","L","XL")
                val colores = listOf(
                    "Blanco con detalles negros",
                    "Negro con detalles blancos"
                )
                val stockInicial = 12

                for ((idCat, precio) in preciosPorCategoria) {
                    val modelo = modeloPorCategoria[idCat] ?: "GENERIC"
                    for (t in tallas) {
                        for (c in colores) {
                            val nextId = (pDao.getMaxIdForCategory(idCat) ?: 0L) + 1L
                            pDao.insert(
                                ProductosEntity(
                                    idCategoria = idCat,
                                    idProducto  = nextId,
                                    marca  = "StoreFit",
                                    modelo = modelo,
                                    color  = c,
                                    talla  = t,
                                    precio = precio,
                                    stock  = stockInicial
                                )
                            )
                        }
                    }
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
                    .addCallback(callback)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
