package com.example.hava

import com.example.hava.data.*
import com.example.hava.model.HourlyForecast
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class ForecastMapperTest {
    private fun response() =
        ForecastResponse(CurrentWeatherResponse("2026-10-26T15:15", 18.0, 60, 17.0, 12.0, 2, 1))

    @Test
    fun parsesApiFieldNamesAndConvertsColumnsToRows() {
        val json =
            """{
          "current":{"time":"2026-10-26T15:15","temperature_2m":18,"relative_humidity_2m":60,
            "apparent_temperature":17,"wind_speed_10m":12,"weather_code":2,"is_day":1},
          "hourly":{"time":["2026-10-26T15:00"],"temperature_2m":[19],"weather_code":[3],"precipitation_probability":[25]},
          "daily":{"time":["2026-10-26"],"temperature_2m_max":[22],"temperature_2m_min":[11],"weather_code":[2],
            "precipitation_probability_max":[40],"uv_index_max":[3.2]}
        }"""
        val forecast = Gson().fromJson(json, ForecastResponse::class.java).toForecast()
        assertEquals(18.0, forecast.current.temperature, 0.0)
        assertEquals(19.0, forecast.hourly.single().temperature, 0.0)
        assertEquals(25, forecast.currentHour?.precipitationProbability)
        assertEquals(11.0, forecast.daily.single().minimumTemperature, 0.0)
        assertEquals(3.2, forecast.daily.single().uvIndex!!, 0.0)
    }

    @Test
    fun incompleteColumnsCannotCauseIndexErrors() {
        val forecast =
            response()
                .copy(
                    hourly = HourlyWeatherResponse(listOf("a", "b"), listOf(18.0), listOf(2, 3)),
                    daily =
                        DailyWeatherResponse(
                            listOf("a", "b"),
                            listOf(20.0, 21.0),
                            listOf(10.0),
                            listOf(2, 3),
                        ),
                )
                .toForecast()
        assertEquals(1, forecast.hourly.size)
        assertEquals(1, forecast.daily.size)
    }

    @Test
    fun missingOptionalForecastsRemainEmptyAndUnknownRainIsNotZero() {
        assertTrue(response().toForecast().daily.isEmpty())
        val forecast =
            response()
                .copy(
                    hourly =
                        HourlyWeatherResponse(listOf("2026-10-26T15:00"), listOf(18.0), listOf(2))
                )
                .toForecast()
        assertNull(forecast.currentHour?.precipitationProbability)
    }

    @Test
    fun boundsProbabilitiesAndIgnoresInvalidUv() {
        val forecast =
            response()
                .copy(
                    hourly =
                        HourlyWeatherResponse(listOf("a"), listOf(18.0), listOf(2), listOf(110)),
                    daily =
                        DailyWeatherResponse(
                            listOf("a"),
                            listOf(20.0),
                            listOf(10.0),
                            listOf(2),
                            listOf(-1),
                            listOf(Double.NaN),
                        ),
                )
                .toForecast()
        assertEquals(100, forecast.hourly.single().precipitationProbability)
        assertEquals(0, forecast.daily.single().precipitationProbability)
        assertNull(forecast.daily.single().uvIndex)
    }

    @Test
    fun upcomingHoursSkipThePastWithoutInventingCurrentHour() {
        val forecast =
            response()
                .toForecast()
                .copy(
                    hourly =
                        listOf(
                            HourlyForecast("2026-10-26T14:00", 17.0, 2, 90),
                            HourlyForecast("2026-10-26T16:00", 18.0, 2, 10),
                        )
                )
        assertNull(forecast.currentHour)
        assertEquals("2026-10-26T16:00", forecast.upcomingHours().single().time)
        assertTrue(forecast.upcomingHours(0).isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidCurrentTemperatureFailsBeforeReachingTheUi() {
        response().copy(current = response().current.copy(temperature = Double.NaN)).toForecast()
    }
}
