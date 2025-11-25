package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appstorefit_grupo1.navigation.Route
import com.example.appstorefit_grupo1.session.SessionManager
import androidx.compose.ui.platform.LocalContext
import com.example.appstorefit_grupo1.data.local.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        // 1) Fuerza apertura/creación de DB en hilo IO
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            db.openHelper.writableDatabase

            // 2) Esperamos a que termine el seed
            val start = System.currentTimeMillis()
            while (true) {
                val listo = kotlin.runCatching {
                    // Semáforos mínimos de que el seed ya corrió
                    (db.rolDao().count() > 0) && (db.registroDao().getByUsuario("a@a.cl") != null)
                }.getOrDefault(false)

                if (listo) break
                if (System.currentTimeMillis() - start > 4000) break  // timeout de seguridad
                delay(100)
            }
        }

        // 3) Restaura sesión desde DataStore
        SessionManager.restoreFromStore(context)

        // 4) Decide destino según rol
        val target = when {
            SessionManager.user == null -> Route.Login.path
            SessionManager.roleId == 2L -> Route.Panel.path   // Admin → Panel
            else                        -> Route.Productos.path
        }

        // 5) Navega limpiando back stack
        navController.navigate(target) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    // UI mínima mientras se resuelve
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(8.dp))
            Text("Cargando…")
        }
    }
}
