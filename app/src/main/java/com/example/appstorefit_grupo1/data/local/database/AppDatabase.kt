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
abstract class AppDatabase: RoomDatabase() {
    //exponer los dao de las tablas con registros por defecto
    abstract fun userDao(): UserDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "ui_navegacion.db"

        //obtener la instancia de la BD
        fun getInstance(context: Context): AppDatabase{
            return INSTANCE ?: synchronized(this){
                //instancias auxiliar para crear la BD
                var instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addCallback(object : RoomDatabase.Callback(){
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            //lanzar una corutina para los insert de las tablas
                            CoroutineScope(Dispatchers.IO).launch {
                                //repetir por cada tabla con insert
                                val dao = getInstance(context).userDao()
                                //genero la lista de los insert
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