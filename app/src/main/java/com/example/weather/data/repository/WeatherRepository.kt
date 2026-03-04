package com.example.weather.data.repository

import com.example.weather.data.model.City
import com.example.weather.data.model.WeatherResponse
import com.example.weather.data.remote.RetrofitInstance

class WeatherRepository {
    
    suspend fun getWeather(latitude: Double, longitude: Double): Result<WeatherResponse> {
        return try {
            val response = RetrofitInstance.api.getWeather(latitude, longitude)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getCities(): List<City> {
        return CityList.cities
    }
}
