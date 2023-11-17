package com.example.hoemwork2_mobileapp

import android.Manifest
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
import com.example.hoemwork2_mobileapp.ui.theme.Hoemwork2_mobileappTheme
import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hw02.ui.theme.HW02Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import retrofit2.http.GET
import retrofit2.http.Query
import androidx.core.content.ContextCompat
import android.location.LocationManager

import android.util.Log


const val logingConst = "log"
typealias cordinates = Pair<Double, Double>
class MainActivity : ComponentActivity() {
    private val permission = 165
    fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                permission
            )
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Hoemwork2_mobileappTheme {

                requestPermission()
                Location.create(this)
                val viewModel = MyView()
                Welcome(this, viewModel)
            }
        }
    }
}
interface Api {
    @GET("v1/current.json")
    suspend fun getWeather(
        @Query("q") name: String,
        @Query("key") apiKey: String
    ): Weather


}
object Location {
    private val cordinates = null
    fun getLocation(): cordinates? {
        return cordinates
    }

    @SuppressLint("No permission", "ServiceCast")
    fun create(context: Context) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 6500L, 10f) {
            it.longitude = 180 + it.longitude
            cordinates = it.longitude to it.latitude

            Log.e(logingConst, "current location: ${cordinates}")
        }
    }
}

typealias LocationCoordinate = Pair<Double, Double>
data class City(val name: String, val text: String, val image: Int)
data class Condition(
    val text: String,
    val icon: String,
    val code: Int
)



class MyView() : ViewModel() {
    private val _weather = MutableLiveData<Weather>()
    val weatherData: LiveData<Weather> get() = _weather

    private val _currentWeatherData = MutableLiveData<Weather>()
    val currentWeatherData: LiveData<Weather> get() = _currentWeatherData

    fun fetchWeather(
        context: Context,
        weatherApiService: Api,
        apiKey: String
    ) {
        try {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                viewModelScope.launch {
                    var location = Location.getLocation()
                    if (location == null) {
                        delay(7000L)
                        location = Location.getLocation()
                    }
                    Log.e(logingConst, "before requesting" + location.toString())
                    if (location != null) {
                        val city = "${location.first},${location.second}"
                        val weather = weatherApiService.getWeather(city, apiKey)
                        _currentWeatherData.postValue(weather)
                    } else {
                        Log.e(logingConst, "not available data")
                    }
                }
            } else {
                Log.e(logingConst, "no permission")
            }
        } catch (e: Exception) {
            Log.e(logingConst, "can't get the data", e)
        }
    }


    fun obtainData(city: City) {
        Log.e(logingConst, "getting data")
        viewModelScope.launch {
            try {
                val apiKey = getApiKey()
                val weather = Api.getWeather(city.name, apiKey)
                _weather.postValue(weather)
            } catch (e: Exception) {
                Log.e(logingConst, e.toString())
            }
        }
    }

    private fun getApiKey(): String {
        return "691d4bacab5c4fa4bec191058231411"
    }
}
data class Air(
    val co: Double,
    val no2: Double,
    val o3: Double,
    val so2: Double,
    val pm2_5: Double,
    val pm10: Double,
    val us_epa_index: Int,
    val gb_defra_index: Int
)
data class Weather(
    val location: Location,
    val current: Data
)



data class Data(
    val last_updated_epoch: Long,
    val last_updated: String,
    val temp_c: Double,
    val temp_f: Double,
    val is_day: Int,
    val condition: Condition,
    val wind_mph: Double,
    val wind_kph: Double,
    val wind_degree: Int,
    val wind_dir: String,
    val pressure_mb: Double,
    val pressure_in: Double,
    val precip_mm: Double,
    val precip_in: Double,
    val humidity: Int,
    val cloud: Int,
    val feelslike_c: Double,
    val feelslike_f: Double,
    val vis_km: Double,
    val vis_miles: Double,
    val uv: Int,
    val gust_mph: Double,
    val gust_kph: Double,
    val air_quality: Air
)

@Composable
fun Welcome(context: MainActivity, viewModel:ViewModel ) {


    val viewModel = MyView()
    viewModel.fetchWeather(
        context,
        Api,
        "691d4bacab5c4fa4bec191058231411"
    )
    NavHost(navController = navController, startDestination = "Screen1") {
        composable(
            route = "Screen1",
        ) {  val weather by viewModel.currentWeatherData

            weather.let { Text(text = "Temperature : ${it.current.temp_c}°C")}
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

