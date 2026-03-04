package com.example.weather.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.data.model.City
import com.example.weather.data.model.CityList
import com.example.weather.data.model.WeatherResponse
import com.example.weather.data.repository.WeatherRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen() {
    val repository = remember { WeatherRepository() }
    val scope = rememberCoroutineScope()
    
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
                title = { Text("Weather App") },
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Error: $error",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun WeatherContent(weather: WeatherResponse, padding: PaddingValues) {
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
                text = "7-Day Forecast",
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
                text = "${current.temperature.toInt()}°C",
                fontSize = 72.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Text(
                text = getWeatherDescription(current.weatherCode),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherInfoItem("Humidity", "${current.humidity}%")
                WeatherInfoItem("Wind", "${current.windSpeed.toInt()} km/h")
                WeatherInfoItem("Feels Like", "${current.apparentTemperature.toInt()}°C")
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
                text = getWeatherDescription(daily.weatherCodes[index]),
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select City") },
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
                Text("Cancel")
            }
        }
    )
}

fun getWeatherDescription(code: Int): String {
    return when (code) {
        0 -> "Clear Sky"
        1, 2, 3 -> "Partly Cloudy"
        45, 48 -> "Foggy"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rain"
        71, 73, 75 -> "Snow"
        77 -> "Snow Grains"
        80, 81, 82 -> "Showers"
        85, 86 -> "Snow Showers"
        95, 96, 99 -> "Thunderstorm"
        else -> "Unknown"
    }
}
