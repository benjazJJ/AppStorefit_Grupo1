package com.example.appstorefit_grupo1.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.appstorefit_grupo1.data.local.user.UserDao
import com.example.appstorefit_grupo1.data.local.user.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase: RoomDatabase(){

    //exponer los dao de las tablas con registros por defecto
    abstract fun  userDao(): UserDao

    companion object{
        @Volatile

        //VARIABLE PARA GUARDAR LA INSTANCIA DE LA CONEXIÓN DE LA BASE DE DATOS, LA INSTANCIA SE GUARDA EN ESTA VARIABLE
        private var INSTANCE: AppDatabase? = null

        //NOMBRE DE LA BASE DE DATOS, ES IMPORTANTE QUE SIEMPRE TENGA EL .db
        private const val DB_STOREFIT = "AppStoreFit_Grupo1.db"

        //Obtener la instancia de la BD
        fun getInstance(context: Context): AppDatabase{
            return INSTANCE ?: synchronized(this){
                //Instancia auxiliar para crear la base de datos
                var instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_STOREFIT
                )
                    .addCallback(object : RoomDatabase.Callback(){
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            //lanzar una corrutina para los inserts de las tablas
                            CoroutineScope(Dispatchers.10).launch {
                                //repetir por cada tabla con insert
                                val dao = getInstance(context).userDao()
                                //genero la lista de los inserts
                                val seed = listOf(
                                    UserEntity(
                                        name = "Admin",
                                        email = "a@a.cl",
                                        phone = "12345678",
                                        pass = "JOSE123"
                                    )
                                )
                                if (dao.count() == 0){
                                    seed.forEach { dao.insertar(it) }
                                }
                            }
                        }

                    }
            }
        }
    }
}