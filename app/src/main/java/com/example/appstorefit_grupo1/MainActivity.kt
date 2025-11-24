package com.example.appstorefit_grupo1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.navigation.compose.rememberNavController
import com.example.appstorefit_grupo1.navigation.AppNavGraph
import com.example.appstorefit_grupo1.ui.theme.AppStoreFit_Grupo1Theme

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val wsc = calculateWindowSizeClass(this)
            val widthClass: WindowWidthSizeClass = wsc.widthSizeClass
            val navController = rememberNavController()

            AppStoreFit_Grupo1Theme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavGraph(
                        navController = navController,
                        widthClass = widthClass
                    )
                }
            }
        }
    }
}
//perro