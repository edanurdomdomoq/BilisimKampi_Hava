package com.example.hava.ui.forecast

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hava.model.*
import com.example.hava.ui.components.*
import com.example.hava.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

private val Turkish = Locale.forLanguageTag("tr-TR")

@Composable
internal fun RainNote() {
    Text(
        localized(
            "Yağış sütunları günlük en yüksek olasılığı gösterir.",
            "Bars show the highest precipitation probability each day.",
        ),
        fontSize = 11.sp,
        color = WeatherMuted,
        modifier = Modifier.padding(horizontal = 8.dp),
    )
}

@Composable
internal fun ForecastMetrics(response: Forecast) {
    val current = response.current
    val rain = response.currentHour?.precipitationProbability
    val uv = response.daily.firstOrNull()?.uvIndex
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricPanel(
                localized("Nem", "Humidity"),
                localized("%${current.humidity}", "${current.humidity}%"),
                Icons.Filled.WaterDrop,
                Modifier.weight(1f),
            )
            MetricPanel(
                localized("Rüzgâr", "Wind"),
                localized(
                    "${current.windSpeed.roundToInt()} km/sa",
                    "${current.windSpeed.roundToInt()} km/h",
                ),
                Icons.Filled.Air,
                Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricPanel(
                localized("Yağış olasılığı", "Chance of rain"),
                rain?.let { localized("%$it", "$it%") } ?: "—",
                Icons.Filled.BeachAccess,
                Modifier.weight(1f),
            )
            MetricPanel(
                localized("UV · günlük en yüksek", "UV · daily maximum"),
                uv?.let {
                    String.format(
                        if (LocalAppLanguage.current == "en") Locale.US else Turkish,
                        "%.1f",
                        it,
                    )
                } ?: "—",
                Icons.Filled.WbSunny,
                Modifier.weight(1f),
                sun = true,
            )
        }
    }
}

@Composable
internal fun TodayHero(response: Forecast) {
    val current = response.current
    val daily = response.daily
    Box(Modifier.fillMaxWidth().heightIn(min = 223.dp)) {
        WeatherArt(
            current.weatherCode,
            Modifier.size(185.dp).align(Alignment.CenterEnd).offset(x = 13.dp, y = (-2).dp),
            isNight = current.isDay == 0,
        )
        Column(Modifier.padding(top = 10.dp, bottom = 4.dp)) {
            Text(
                formatDate(current.time.take(10), "d MMMM EEEE", LocalAppLanguage.current),
                fontSize = 15.sp,
                color = WeatherMuted,
            )
            Text(
                "${current.temperature.roundToInt()}°",
                fontSize = if (isLandscapeLayout()) 72.sp else 86.sp,
                lineHeight = 98.sp,
                fontWeight = FontWeight.Bold,
                color = WeatherInk,
                letterSpacing = (-4).sp,
            )
            // The label stays below the illustration and can wrap at large font sizes.
            Text(
                weatherDescription(current.weatherCode, LocalAppLanguage.current),
                fontSize = 23.sp,
                fontWeight = FontWeight.SemiBold,
                color = WeatherInk,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                localized(
                    "Hissedilen ${current.feelsLike.roundToInt()}°",
                    "Feels like ${current.feelsLike.roundToInt()}°",
                ),
                fontSize = 16.sp,
                color = WeatherMuted,
                modifier = Modifier.padding(top = 4.dp),
            )
            val high = daily.firstOrNull()?.maximumTemperature?.roundToInt()?.let { "$it°" } ?: "—"
            val low = daily.firstOrNull()?.minimumTemperature?.roundToInt()?.let { "$it°" } ?: "—"
            Text(
                localized("En yüksek $high  ·  En düşük $low", "High $high  ·  Low $low"),
                fontSize = 14.sp,
                color = WeatherMuted,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
internal fun WeatherPanel(
    modifier: Modifier = Modifier,
    spacing: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF152137),
        contentColor = WeatherInk,
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(spacing),
            content = content,
        )
    }
}

@Composable
internal fun HourlyPanel(response: Forecast) {
    WeatherPanel {
        Text(
            localized("Saatlik tahmin", "Hourly forecast"),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
        val hours = response.upcomingHours()
        if (hours.isEmpty())
            EmptyView(localized("Saatlik tahmin bulunamadı", "Hourly forecast unavailable"))
        else
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                hours.forEachIndexed { index, hour ->
                    Column(
                        Modifier.width(76.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        val isCurrentHour = hour.time.take(13) == response.current.time.take(13)
                        Text(
                            if (isCurrentHour) localized("Şimdi", "Now")
                            else hour.time.substringAfter("T"),
                            fontSize = 13.sp,
                            color = WeatherMuted,
                        )
                        LineWeatherIcon(hour.weatherCode, Modifier.size(44.dp))
                        Text(
                            "${hour.temperature.roundToInt()}°",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    if (index < hours.lastIndex)
                        VerticalDivider(Modifier.height(100.dp), color = Color(0xFF29364F))
                }
            }
    }
}

@Composable
internal fun MetricPanel(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier,
    sun: Boolean = false,
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(22.dp), color = Color(0xFF152137)) {
        Row(
            Modifier.heightIn(min = 84.dp).padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier.size(44.dp).background(Color(0xFF24334F), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (sun) WeatherArt(0, Modifier.size(43.dp))
                else Icon(icon, null, Modifier.size(27.dp), tint = WeatherBlue)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(label, fontSize = 11.sp, lineHeight = 14.sp, color = WeatherMuted)
                Text(value, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = WeatherInk)
            }
        }
    }
}

@Composable
internal fun DailyPanel(daily: List<DailyForecast>) {
    WeatherPanel(spacing = 0.dp) {
        if (daily.isEmpty())
            Text(
                localized("Günlük tahmin bulunamadı", "Daily forecast unavailable"),
                color = WeatherMuted,
            )
        else {
            val count = minOf(7, daily.size)
            val minimum = daily.take(count).minOfOrNull { it.minimumTemperature } ?: 0.0
            val maximum = daily.take(count).maxOfOrNull { it.maximumTemperature } ?: minimum + 1
            val range = (maximum - minimum).coerceAtLeast(1.0)
            repeat(count) { index ->
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 51.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Text(
                        if (index == 0) localized("Bugün", "Today")
                        else formatDate(daily[index].date, "EEEE", LocalAppLanguage.current),
                        modifier = Modifier.width(86.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    LineWeatherIcon(daily[index].weatherCode, Modifier.size(34.dp))
                    Text(
                        "${daily[index].minimumTemperature.roundToInt()}°",
                        Modifier.width(28.dp),
                        fontSize = 13.sp,
                        color = WeatherMuted,
                    )
                    BoxWithConstraints(
                        Modifier.weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF28334A))
                    ) {
                        val offset = ((daily[index].minimumTemperature - minimum) / range).toFloat()
                        val fraction =
                            ((daily[index].maximumTemperature - daily[index].minimumTemperature) /
                                    range)
                                .toFloat()
                        Box(
                            Modifier.offset(x = maxWidth * offset)
                                .width(maxWidth * fraction.coerceAtLeast(.06f))
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF70ACFF),
                                            Color(0xFFACD9EF),
                                            Color(0xFFFFDD39),
                                        )
                                    ),
                                    RoundedCornerShape(8.dp),
                                )
                        )
                    }
                    Text(
                        "${daily[index].maximumTemperature.roundToInt()}°",
                        Modifier.width(28.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (index < count - 1) HorizontalDivider(color = Color(0xFF29364F))
            }
        }
    }
}

@Composable
internal fun RainPanel(daily: List<DailyForecast>) {
    WeatherPanel {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Filled.BeachAccess, null, tint = WeatherBlue)
            Text(
                localized("Yağış olasılığı", "Chance of rain"),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        val count = minOf(7, daily.size)
        if (count == 0)
            Text(
                localized("Yağış tahmini bulunamadı", "Rain forecast unavailable"),
                color = WeatherMuted,
            )
        else
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                repeat(count) { index ->
                    val probability = daily[index].precipitationProbability
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            Modifier.fillMaxWidth().height(88.dp),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    probability?.let { localized("%$it", "$it%") } ?: "—",
                                    fontSize = 11.sp,
                                    color = WeatherInk,
                                )
                                Spacer(Modifier.height(5.dp))
                                Box(
                                    Modifier.fillMaxWidth()
                                        .height(
                                            (probability?.coerceIn(0, 100)?.times(.58f) ?: 0f)
                                                .coerceAtLeast(3f)
                                                .dp
                                        )
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color(0xFF73ABFF), Color(0xFFACD6FF))
                                            ),
                                            RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                                        )
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (index == 0) localized("Bugün", "Today")
                            else formatDate(daily[index].date, "EEE", LocalAppLanguage.current),
                            fontSize = 10.sp,
                            color = WeatherMuted,
                        )
                    }
                }
            }
    }
}

internal fun formatDate(date: String, pattern: String, language: String = "tr"): String =
    try {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(date)
        if (parsed == null) date
        else
            SimpleDateFormat(pattern, if (language == "en") Locale.US else Turkish)
                .format(parsed)
                .replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(if (language == "en") Locale.US else Turkish)
                    else it.toString()
                }
    } catch (_: Exception) {
        date
    }
