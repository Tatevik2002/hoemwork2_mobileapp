package com.example.hoemwork2_mobileapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hoemwork2_mobileapp.ui.theme.Hoemwork2_mobileappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Hoemwork2_mobileappTheme {
                // A surface container using the 'background' color from the theme
                Welcome()
            }
        }
    }
}
@Composable
fun Welcome() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "Screen1") {
        composable(
            route = "Screen1",
        ) {
            Text(
                text = "Welcome to the Tatevik's app",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
            Button(
                onClick = { navController.navigate("Screen2") },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Let's go to the second screen")
            }

        }
        composable(
            route = "Screen2",
        ) {
            var cities = listOf("Yerevan", "Washington", "Madrid")
            LazyColumn {
                items(cities) { city ->
                    Text(text = city, fontSize = 16.sp, modifier = Modifier.padding(16.dp))
                    if (city == "Yerevan"){
                        Text(text = "Yerevan is the capital of Armenia", fontSize = 16.sp, modifier = Modifier.padding(16.dp))
                        // I did not understand how to have my image in android studio, so i just wrote your_image, supposing
                        // that I have the image in drawable folder, I would write the name of the image instead of
                        // your_image
                        val cityImage: Painter = painterResource(id = R.drawable.your_image)
                        Image(
                            painter = cityImage,

                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                    if (city =="Washington"){
                        Text(text = "Washington is the capital of US", fontSize = 16.sp, modifier = Modifier.padding(16.dp))
                        // I did not understand how to have my image in android studio, so i just wrote your_image, supposing
                        // that I have the image in drawable folder, I would write the name of the image instead of
                        // your_image
                        val cityImage: Painter = painterResource(id = R.drawable.your_image)
                        Image(
                            painter = cityImage,

                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )}
                    if (city =="Madrid"){
                        Text(text = "Madrid is the capital of Spain", fontSize = 16.sp, modifier = Modifier.padding(16.dp))
                        // I did not understand how to have my image in android studio, so i just wrote your_image, supposing
                        // that I have the image in drawable folder, I would write the name of the image instead of
                        // your_image
                        val cityImage: Painter = painterResource(id = R.drawable.your_image)
                        Image(
                            painter = cityImage,

                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )}
                }

            }
            BackHandler {
                navController.popBackStack()
            }

        }
    }
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Hoemwork2_mobileappTheme {
        Greeting("Android")
    }
}