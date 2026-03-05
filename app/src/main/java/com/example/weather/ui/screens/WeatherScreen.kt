package com.example.weather.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.data.model.City
import com.example.weather.data.model.CityList
import com.example.weather.data.model.CurrentWeather
import com.example.weather.data.model.DailyForecast
import com.example.weather.data.model.WeatherResponse
import com.example.weather.data.repository.WeatherRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen() {
    val repository = remember { WeatherRepository() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var weather by remember { mutableStateOf<WeatherResponse?>(null) }
    var selectedCity by remember { mutableStateOf(CityList.cities[0]) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showCityDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(selectedCity) {
        isLoading = true
        error = null
        repository.getWeather(selectedCity.latitude, selectedCity.longitude)
            .onSuccess { weather = it }
            .onFailure { error = it.message }
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.title_weather_app)) },
                actions = {
                    TextButton(onClick = { showCityDialog = true }) {
                        Text(selectedCity.name)
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> LoadingContent(padding)
            error != null -> ErrorContent(error!!, padding)
            weather != null -> WeatherContent(weather!!, padding)
        }
    }
    
    if (showCityDialog) {
        CitySelectionDialog(
            cities = CityList.cities,
            selectedCity = selectedCity,
            onCitySelected = { selectedCity = it },
            onDismiss = { showCityDialog = false }
        )
    }
}

@Composable
private fun LoadingContent(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(error: String, padding: PaddingValues) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${context.getString(R.string.error_prefix)}$error",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun WeatherContent(weather: WeatherResponse, padding: PaddingValues) {
    val context = LocalContext.current
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CurrentWeatherCard(weather.current)
        }
        item {
            Text(
                text = context.getString(R.string.title_7day_forecast),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        items(weather.daily.times.size) { index ->
            ForecastItem(weather.daily, index)
        }
    }
}

@Composable
private fun CurrentWeatherCard(current: CurrentWeather) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${current.temperature.toInt()}${context.getString(R.string.unit_celsius)}",
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = getWeatherDescription(context, current.weatherCode),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherInfoItem(
                    context.getString(R.string.label_humidity), 
                    "${current.humidity}${context.getString(R.string.unit_percent)}"
                )
                WeatherInfoItem(
                    context.getString(R.string.label_wind), 
                    "${current.windSpeed.toInt()} ${context.getString(R.string.unit_km_h)}"
                )
                WeatherInfoItem(
                    context.getString(R.string.label_feels_like), 
                    "${current.apparentTemperature.toInt()}${context.getString(R.string.unit_celsius)}"
                )
            }
        }
    }
}

@Composable
private fun WeatherInfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium)
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ForecastItem(daily: DailyForecast, index: Int) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = daily.times[index].substring(5),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = getWeatherDescription(context, daily.weatherCodes[index]),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${daily.minTemperatures[index].toInt()}° / ${daily.maxTemperatures[index].toInt()}°",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun CitySelectionDialog(
    cities: List<City>,
    selectedCity: City,
    onCitySelected: (City) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(context.getString(R.string.label_select_city)) },
        text = {
            LazyColumn {
                items(cities) { city ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${city.name}, ${city.country}")
                        RadioButton(
                            selected = city == selectedCity,
                            onClick = {
                                onCitySelected(city)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(context.getString(R.string.action_cancel))
            }
        }
    )
}

fun getWeatherDescription(context: android.content.Context, code: Int): String {
    val resId = when (code) {
        0 -> R.string.weather_clear_sky
        1, 2, 3 -> R.string.weather_partly_cloudy
        45, 48 -> R.string.weather_foggy
        51, 53, 55 -> R.string.weather_drizzle
        61, 63, 65 -> R.string.weather_rain
        71, 73, 75 -> R.string.weather_snow
        77 -> R.string.weather_snow_grains
        80, 81, 82 -> R.string.weather_showers
        85, 86 -> R.string.weather_snow_showers
        95, 96, 99 -> R.string.weather_thunderstorm
        else -> R.string.weather_unknown
    }
    return context.getString(resId)
}
