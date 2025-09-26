package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appstorefit_grupo1.R


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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Bienvenido",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleLarge
                )
                Button(onClick = {}) {
                    Text("Presioname...")
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Image(
                    painter = painterResource(R.drawable.gatito),
                    contentDescription = "Logo App",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}





@Preview(name = "Expandida", widthDp = 1000, heightDp = 800)
@Composable
fun HomeScreenExpandidaPreview(){
    HomeScreenCompacta()
}