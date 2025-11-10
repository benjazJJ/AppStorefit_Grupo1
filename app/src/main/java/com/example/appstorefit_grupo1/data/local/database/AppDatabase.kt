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
import com.example.appstorefit_grupo1.data.local.Compras.CompraEntity
import com.example.appstorefit_grupo1.data.local.Compras.CompraDetalleEntity
import com.example.appstorefit_grupo1.data.local.Carrito.CarritoDao
import com.example.appstorefit_grupo1.data.local.Carrito.CarritoEntity
import com.example.appstorefit_grupo1.data.local.Compras.CompraDao
import com.example.appstorefit_grupo1.data.local.Mensaje.MensajeDao
import com.example.appstorefit_grupo1.data.local.Mensaje.MensajeEntity




@Database(
    entities = [
        UserEntity::class,
        RegistroEntity::class,
        RolEntity::class,
        CategoriaEntity::class,
        ProductosEntity::class,
        CarritoEntity::class,
        MensajeEntity::class,
        CompraEntity::class,
        CompraDetalleEntity::class
    ],
    version = 37,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun registroDao(): RegistroDao
    abstract fun rolDao(): RolDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun productosDao(): ProductosDao
    abstract fun carritoDao(): CarritoDao
    abstract fun mensajeDao(): MensajeDao

    abstract fun compraDao(): CompraDao


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

            //Roles
            if (kotlin.runCatching { rDao.count() }.getOrDefault(0) == 0) {
                rDao.insert(RolEntity(rolId = 1L, nombreRol = "CLIENTE"))
                rDao.insert(RolEntity(rolId = 2L, nombreRol = "ADMIN"))
                rDao.insert(RolEntity(rolId = 3L, nombreRol = "SOPORTE"))
            }

            //Usuarios + Registro
            suspend fun ensureUser(
                email: String,
                rut: String,
                nombre: String,
                pass: String,
                rolId: Long,
                telefono: String
            ) {
                val e = email.trim().lowercase()
                if (regDao.getByUsuario(e) == null) {
                    if (uDao.getByRut(rut) == null) {
                        uDao.insertar(
                            UserEntity(
                                rut = rut,
                                name = nombre,
                                email = e,
                                phone = telefono,
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

            //USUARIOS PRECARGADOS PARA TESTEAR
            ensureUser("a@a.cl", "11.111.111-1", "Admin", "Admin123!", 2L, telefono = "941827012") //ADMINISTRADOR
            ensureUser("b@b.cl", "22.222.222-2", "Jose",  "Jose123!",  1L, telefono = "941827013") //CLIENTE
            ensureUser("s@s.cl", "33.333.333-3", "Soporte", "Soporte123!", 3L, telefono = "941827014") //SOPORTE


            // Categorías
            if (kotlin.runCatching { cDao.count() }.getOrDefault(0) == 0) {
                cDao.insert(CategoriaEntity(nombre = "Poleras"))
                cDao.insert(CategoriaEntity(nombre = "Poleron"))
                cDao.insert(CategoriaEntity(nombre = "Buzo"))
                cDao.insert(CategoriaEntity(nombre = "Conjunto Femenino"))
            }

            //Productos (exactamente 4, uno por categoría)
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

            //Variantes por modelo: Negro/Blanco x XS..XL
            run {
                val tallas = listOf("XS","S","M","L","XL")
                val COLOR_NEGRO  = "Negro con detalles blancos"
                val COLOR_BLANCO = "Blanco con detalles negros"

                val existentes = pDao.getAll()

                data class ModeloBase(val idCat: Long, val modelo: String, val precio: Int)
                val modelosBase = existentes
                    .groupBy { it.idCategoria }
                    .flatMap { (idCat, lista) ->
                        // normaliza quitando prefijo B para agrupar
                        val modelosDistinct = lista
                            .map { it.modelo.removePrefix("B") }
                            .distinct()

                        modelosDistinct.map { mb ->
                            // referencia para precio (si no hay exacto, usa cualquiera)
                            val ref = lista.firstOrNull { it.modelo == mb } ?: lista.first()
                            ModeloBase(idCat = idCat, modelo = mb, precio = ref.precio)
                        }
                    }

                // Generar combinaciones faltantes
                modelosBase.forEach { base ->
                    var nextId = (pDao.getMaxIdForCategory(base.idCat) ?: 0L) + 1L
                    val aInsertar = mutableListOf<ProductosEntity>()

                    // 1) Negro (modelo base)
                    for (t in tallas) {
                        val yaExiste = pDao.countByCatModeloColorTalla(
                            idCategoria = base.idCat,
                            modelo = base.modelo,
                            color = COLOR_NEGRO,
                            talla = t
                        ) > 0
                        if (!yaExiste) {
                            aInsertar += ProductosEntity(
                                idCategoria = base.idCat,
                                idProducto  = nextId++,
                                marca       = "StoreFit",
                                modelo      = base.modelo,
                                color       = COLOR_NEGRO,
                                talla       = t,
                                precio      = base.precio,
                                stock       = if (t == "M") 80 else 30
                            )
                        }
                    }

                    // 2) Blanco (modelo con prefijo B)
                    val modeloBlanco = "B${base.modelo}"
                    for (t in tallas) {
                        val yaExiste = pDao.countByCatModeloColorTalla(
                            idCategoria = base.idCat,
                            modelo = modeloBlanco,
                            color = COLOR_BLANCO,
                            talla = t
                        ) > 0
                        if (!yaExiste) {
                            aInsertar += ProductosEntity(
                                idCategoria = base.idCat,
                                idProducto  = nextId++,
                                marca       = "StoreFit",
                                modelo      = modeloBlanco,
                                color       = COLOR_BLANCO,
                                talla       = t,
                                precio      = base.precio,
                                stock       = 30
                            )
                        }
                    }

                    if (aInsertar.isNotEmpty()) {
                        pDao.insertAll(aInsertar)
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
