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
import com.example.hoemwork2_mobileapp.ui.theme.Hoemwork2_mobileappTheme
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.RadioButton
import androidx.compose.ui.Alignment
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import androidx.compose.foundation.Image
import androidx.compose.runtime.livedata.observeAsState



const val logingConst = "log"
typealias cordinates = Pair<Double, Double>
class MainActivity : ComponentActivity() {
    private val permission = 165
    fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
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
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permission) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {

            }
        }
    }
}
val loggingInterceptor = HttpLoggingInterceptor().also {
    it.level = HttpLoggingInterceptor.Level.BODY
}
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)
    .build();
val retrofit = Retrofit.Builder()
    .baseUrl("https://api.weatherapi.com/")
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .build()
interface Api {
    @GET("v1/current.json")
    suspend fun getWeather(
        @Query("q") name: String,
        @Query("key") apiKey: String
    ): Weather


}
val weatherApiService: Api = retrofit.create(Api::class.java)
object Location {
    private var cordinates: Pair<Double, Double>? = null
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
                    Manifest.permission.ACCESS_FINE_LOCATION
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
                val weather = weatherApiService.getWeather(city.name, apiKey)
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
enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT
}
object Settings {
    val temperatureType = MutableLiveData(TemperatureUnit.CELSIUS)
}

@Composable
fun Welcome(context: MainActivity, viewModel:MyView ) {
    val navController = rememberNavController()

    viewModel.fetchWeather(
        context,
        weatherApiService,
        "691d4bacab5c4fa4bec191058231411"
    )

    NavHost(navController = navController, startDestination = "Screen1") {
        composable(
            route = "Screen1",
        ) {
            val temperatureType = Settings.temperatureType

            val weather = viewModel.currentWeatherData.observeAsState()

            if (temperatureType.value == TemperatureUnit.CELSIUS)
                Text(text = "Temperature : ${weather.current.temp_c}°C")
            else
                Text(text = "Temperature : ${weather.current.temp_f}°C")

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
            Button(
                onClick = { navController.navigate("Setting Screen") },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Let's go to the setting screen to change the temperature unit")
            }

        }
        composable(
            route = "Setting Screen",
        ) {
            val temperatureType = Settings.temperatureType

            Text(text = "Temperature unit change")
            Spacer(modifier = Modifier.height(10.dp) )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RadioButton(
                        selected = temperatureType.value == TemperatureUnit.CELSIUS,
                        onClick = {
                            temperatureType.postValue(TemperatureUnit.CELSIUS)
                        }
                    )
                    Text(text = "Celsius")
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RadioButton(
                        selected = temperatureType.value == TemperatureUnit.FAHRENHEIT,
                        onClick = {
                            temperatureType.postValue(TemperatureUnit.FAHRENHEIT)
                        }
                    )
                    Text(text = "Fahrenheit")
                }
            }
            Button(
                onClick = { navController.navigate("Screen1") },
                modifier = Modifier.padding(10.dp)
            ) {
                Text("Back to the first screen")
            }


        }
        composable(
            route = "Screen2",
        ) {
            val temperatureType = Settings.temperatureType

            var cities = listOf("Yerevan", "Washington", "Madrid")
            val weather = viewModel.currentWeatherData.observeAsState()
            LazyColumn {
                items(cities) { city ->
                    Text(text = city, fontSize = 16.sp, modifier = Modifier.padding(16.dp))
                    if (city == "Yerevan"){
                        Text(text = "Yerevan is the capital of Armenia", fontSize = 16.sp, modifier = Modifier.padding(16.dp))
                        if (weather.current.temp_c != null) {
                            if (temperatureType.value == TemperatureUnit.CELSIUS) {
                                Text(
                                    text = "Temperature : ${weather.current.temp_c}°C",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(16.dp)
                                )
                            } else
                                Text(
                                    text = "Temperature : ${weather.current.temp_f}°C",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(16.dp)
                                )
                        }

                        else
                            Text(text = "Humidity : ${weather.current.humidity}°C", fontSize = 16.sp, modifier = Modifier.padding(16.dp))
                        val cityImage: Painter = painterResource(id = R.drawable.img)

                        Image(
                            painter = cityImage,
                            contentDescription=null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                    if (city =="Washington"){
                        Text(text = "Washington is the capital of US", fontSize = 16.sp, modifier = Modifier.padding(16.dp))
                        if (weather.current.temp_c != null) {
                            if (temperatureType.value == TemperatureUnit.CELSIUS) {
                                Text(
                                    text = "Temperature : ${weather.current.temp_c}°C",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(16.dp)
                                )
                            } else
                                Text(
                                    text = "Temperature : ${weather.current.temp_f}°C",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(16.dp)
                                )
                        }

                        else
                            Text(text = "Humidity : ${weather.current.humidity}°C", fontSize = 16.sp, modifier = Modifier.padding(16.dp))
                        val cityImage: Painter = painterResource(id = R.drawable.img_1)
                        Image(
                            painter = cityImage,
                            contentDescription=null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )}
                    if (city =="Madrid"){
                        Text(text = "Madrid is the capital of Spain", fontSize = 16.sp, modifier = Modifier.padding(16.dp))

                        if (weather.current.temp_c != null) {
                            if (temperatureType.value == TemperatureUnit.CELSIUS) {
                                Text(
                                    text = "Temperature : ${weather.current.temp_c}°C",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(16.dp)
                                )
                            } else
                                Text(
                                    text = "Temperature : ${weather.current.temp_f}°C",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(16.dp)
                                )
                        }

                        else
                            Text(text = "Humidity : ${weather.current.humidity}°C", fontSize = 16.sp, modifier = Modifier.padding(16.dp))

                        val cityImage: Painter = painterResource(id = R.drawable.img_2)
                        Image(
                            painter = cityImage,
                            contentDescription=null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )}
                }

            }
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(15.dp)
            ) {
                Text("Back to the first screen")
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

