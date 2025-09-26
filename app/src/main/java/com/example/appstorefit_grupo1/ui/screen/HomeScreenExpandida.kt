package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenExpandida(){
    Scaffold( //Siempre debemos construir el Scaffold primero
        topBar = {
            TopAppBar({Text("Hola Mundo")})

        }
    ) { innerPadding ->
        Row (
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Column(modifier = Modifier.weight(1f)) {  }
            Column(modifier = Modifier.weight(1f)) {  }
        }
    }
}





@Preview(name = "Expandida", widthDp = 1000, heightDp = 800)
@Composable
fun HomeScreenExpandidaPreview(){
    HomeScreenCompacta()
}