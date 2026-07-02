package com.example.api

import android.util.Log
import com.example.ui.viewmodel.WeatherState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object WeatherService {
    private const val TAG = "WeatherService"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Fetches real-time weather for a given city name.
     * Returns null if search fails or coordinates are not found.
     */
    suspend fun fetchWeatherForCity(cityName: String): WeatherState? = withContext(Dispatchers.IO) {
        try {
            val sanitizedCity = cityName.trim()
            if (sanitizedCity.isEmpty()) return@withContext null

            // 1. Geocoding API to resolve coordinates
            val encodedCity = URLEncoder.encode(sanitizedCity, "UTF-8")
            val geocodingUrl = "https://geocoding-api.open-meteo.com/v1/search?name=$encodedCity&count=1&language=en&format=json"

            val geoRequest = Request.Builder().url(geocodingUrl).build()
            val geoResponse = client.newCall(geoRequest).execute()
            if (!geoResponse.isSuccessful) {
                Log.e(TAG, "Geocoding failed for city: $sanitizedCity, response code: ${geoResponse.code}")
                return@withContext null
            }

            val geoResponseBody = geoResponse.body?.string() ?: return@withContext null
            val geoJson = JSONObject(geoResponseBody)
            val results = geoJson.optJSONArray("results")
            if (results == null || results.length() == 0) {
                Log.w(TAG, "No geocoding results found for city: $sanitizedCity")
                return@withContext null
            }

            val locationObj = results.getJSONObject(0)
            val lat = locationObj.getDouble("latitude")
            val lon = locationObj.getDouble("longitude")
            val resolvedName = locationObj.optString("name", sanitizedCity)
            val resolvedCountry = locationObj.optString("country", "")
            val resolvedCountryCode = locationObj.optString("country_code", "")

            // 2. Fetch current weather using resolved coordinates
            val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code"
            val weatherRequest = Request.Builder().url(weatherUrl).build()
            val weatherResponse = client.newCall(weatherRequest).execute()
            if (!weatherResponse.isSuccessful) {
                Log.e(TAG, "Weather forecast request failed for lat: $lat, lon: $lon")
                return@withContext null
            }

            val weatherResponseBody = weatherResponse.body?.string() ?: return@withContext null
            val weatherJson = JSONObject(weatherResponseBody)
            val currentObj = weatherJson.optJSONObject("current") ?: return@withContext null

            val temp = currentObj.optDouble("temperature_2m", 20.0)
            val humidity = currentObj.optInt("relative_humidity_2m", 60)
            val windSpeed = currentObj.optDouble("wind_speed_10m", 10.0)
            val weatherCode = currentObj.optInt("weather_code", 0)

            val weatherDescription = mapWeatherCode(weatherCode)

            Log.d(TAG, "Successfully fetched weather for $resolvedName: $temp°C, $humidity% humidity, $weatherDescription, country: $resolvedCountry")

            return@withContext WeatherState(
                city = resolvedName,
                temperature = temp,
                humidity = humidity,
                weatherDescription = weatherDescription,
                windSpeed = windSpeed,
                country = resolvedCountry,
                countryCode = resolvedCountryCode
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather for city: $cityName", e)
            return@withContext null
        }
    }

    suspend fun fetchWeatherForCoordinates(lat: Double, lon: Double, resolvedCityName: String? = null): WeatherState? = withContext(Dispatchers.IO) {
        try {
            // Fetch current weather using coordinates
            val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code"
            val weatherRequest = Request.Builder().url(weatherUrl).build()
            val weatherResponse = client.newCall(weatherRequest).execute()
            if (!weatherResponse.isSuccessful) {
                Log.e(TAG, "Weather forecast request failed for lat: $lat, lon: $lon")
                return@withContext null
            }

            val weatherResponseBody = weatherResponse.body?.string() ?: return@withContext null
            val weatherJson = JSONObject(weatherResponseBody)
            val currentObj = weatherJson.optJSONObject("current") ?: return@withContext null

            val temp = currentObj.optDouble("temperature_2m", 20.0)
            val humidity = currentObj.optInt("relative_humidity_2m", 60)
            val windSpeed = currentObj.optDouble("wind_speed_10m", 10.0)
            val weatherCode = currentObj.optInt("weather_code", 0)

            val weatherDescription = mapWeatherCode(weatherCode)

            // Try to reverse-geocode via API or use local coordinate strings
            var finalCity = resolvedCityName ?: "Lat: ${String.format(java.util.Locale.US, "%.3f", lat)}, Lon: ${String.format(java.util.Locale.US, "%.3f", lon)}"
            var resolvedCountry = ""
            var resolvedCountryCode = ""

            if (resolvedCityName == null) {
                try {
                    val revGeoUrl = "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=$lat&longitude=$lon&localityLanguage=en"
                    val revGeoRequest = Request.Builder().url(revGeoUrl).build()
                    val revGeoResponse = client.newCall(revGeoRequest).execute()
                    if (revGeoResponse.isSuccessful) {
                        val body = revGeoResponse.body?.string()
                        if (body != null) {
                            val json = JSONObject(body)
                            val city = json.optString("city").takeIf { it.isNotEmpty() }
                                ?: json.optString("locality").takeIf { it.isNotEmpty() }
                                ?: json.optString("principalSubdivision").takeIf { it.isNotEmpty() }
                            if (city != null) {
                                finalCity = city
                            }
                            resolvedCountry = json.optString("countryName", "")
                            resolvedCountryCode = json.optString("countryCode", "")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Reverse geocoding API failed", e)
                }
            }

            Log.d(TAG, "Successfully fetched weather for coordinates $lat, $lon -> $finalCity: $temp°C, $humidity% humidity, $weatherDescription")

            return@withContext WeatherState(
                city = finalCity,
                temperature = temp,
                humidity = humidity,
                weatherDescription = weatherDescription,
                windSpeed = windSpeed,
                country = resolvedCountry,
                countryCode = resolvedCountryCode
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather for coordinates", e)
            return@withContext null
        }
    }

    private fun mapWeatherCode(code: Int): String {
        return when (code) {
            0 -> "Clear skies"
            1 -> "Mainly clear skies"
            2 -> "Partly cloudy"
            3 -> "Overcast skies"
            45, 48 -> "Foggy conditions"
            51, 53, 55 -> "Light drizzling rain"
            56, 57 -> "Freezing drizzle"
            61, 63 -> "Moderate rain"
            65 -> "Heavy rain showers"
            66, 67 -> "Freezing rain"
            71, 73 -> "Slight snowfall"
            75 -> "Heavy snowfall"
            77 -> "Snow grains"
            80, 81, 82 -> "Passing rain showers"
            85, 86 -> "Passing snow showers"
            95 -> "Thunderstorm conditions"
            96, 99 -> "Thunderstorm with hail"
            else -> "Temperate weather"
        }
    }
}
