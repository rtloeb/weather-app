package com.example.weather.data.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("timezone") val timezone: String,
    @SerializedName("current") val current: CurrentWeather,
    @SerializedName("daily") val daily: DailyForecast
)

data class CurrentWeather(
    @SerializedName("time") val time: String,
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("relative_humidity_2m") val humidity: Int,
    @SerializedName("apparent_temperature") val apparentTemperature: Double,
    @SerializedName("is_day") val isDay: Int,
    @SerializedName("precipitation") val precipitation: Double,
    @SerializedName("weather_code") val weatherCode: Int,
    @SerializedName("wind_speed_10m") val windSpeed: Double,
    @SerializedName("wind_direction_10m") val windDirection: Int
)

data class DailyForecast(
    @SerializedName("time") val times: List<String>,
    @SerializedName("temperature_2m_max") val maxTemperatures: List<Double>,
    @SerializedName("temperature_2m_min") val minTemperatures: List<Double>,
    @SerializedName("weather_code") val weatherCodes: List<Int>,
    @SerializedName("precipitation_probability_max") val precipitationProbability: List<Int>
)

data class City(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)

// 预定义城市列表
object CityList {
    val cities = listOf(
        City("Beijing", "China", 39.9042, 116.4074),
        City("Shanghai", "China", 31.2304, 121.4737),
        City("Guangzhou", "China", 23.1291, 113.2644),
        City("Shenzhen", "China", 22.5431, 114.0579),
        City("Chengdu", "China", 30.5728, 104.0668),
        City("Hangzhou", "China", 30.2741, 120.1551),
        City("New York", "USA", 40.7128, -74.0060),
        City("London", "UK", 51.5074, -0.1278),
        City("Tokyo", "Japan", 35.6762, 139.6503),
        City("Singapore", "Singapore", 1.3521, 103.8198)
    )
}
