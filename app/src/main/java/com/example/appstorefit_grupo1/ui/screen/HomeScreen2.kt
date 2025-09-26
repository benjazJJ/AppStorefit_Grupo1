package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.runtime.Composable
import com.example.appstorefit_grupo1.ui.utils.obtenerWindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

@Composable
fun HomeScreen2(){
    val  windowSizeClass = obtenerWindowSizeClass()

    when(windowSizeClass.widthSizeClass){
        WindowWidthSizeClass.Compact -> HomeScreenCompacta()
        WindowWidthSizeClass.Expanded -> HomeScreenExpandida()
    }
}