package com.example.hava.ui.cities

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.hava.model.City
import com.example.hava.model.CityPhoto
import com.example.hava.model.CurrentWeather
import com.example.hava.ui.LoadState
import com.example.hava.ui.components.*
import kotlin.math.roundToInt

@Composable
fun CityWeatherCard(
    city: City,
    remotePhoto: CityPhoto?,
    weatherState: LoadState<CurrentWeather>,
    onRetry: () -> Unit,
    onClick: () -> Unit,
) {
    var showPhotoCredit by remember { mutableStateOf(false) }
    var remoteVisible by remember(remotePhoto?.url) { mutableStateOf(false) }
    val language = LocalAppLanguage.current
    val context = LocalContext.current
    val photo =
        remember(city.photoKey) {
            context.resources.getIdentifier(
                "city_${city.photoKey}",
                "drawable",
                context.packageName,
            )
        }
    if (showPhotoCredit && remotePhoto != null)
        PhotoCreditsDialog(photo = remotePhoto) { showPhotoCredit = false }
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF18273B),
        contentColor = Color.White,
        border = BorderStroke(1.dp, Color(0xFF53617B)),
    ) {
        Box(Modifier.fillMaxWidth().heightIn(min = 132.dp)) {
            if (photo != 0)
                Image(
                    painterResource(photo),
                    null,
                    Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                )
            if (remotePhoto != null)
                AsyncImage(
                    model = remotePhoto.url,
                    imageLoader = CityPhotoImageLoader.get(context),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                    onSuccess = { remoteVisible = true },
                    onError = { remoteVisible = false },
                )
            Box(
                Modifier.matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF111E34).copy(alpha = .94f),
                                Color(0xFF142038).copy(alpha = .58f),
                                Color(0xFF101827).copy(alpha = .13f),
                            )
                        )
                    )
            )
            Box(
                Modifier.matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xFF07111D).copy(alpha = .24f))
                        )
                    )
            )
            if (remoteVisible)
                IconButton(
                    onClick = { showPhotoCredit = true },
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        localized("Fotoğraf bilgisi", "Photo information"),
                        Modifier.size(19.dp),
                        tint = Color.White,
                    )
                }
            Column(
                Modifier.padding(horizontal = 18.dp, vertical = 13.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    city.displayName(),
                    fontFamily = FontFamily.Serif,
                    fontSize = 23.sp,
                    color = Color(0xFFF9F5FF),
                )
                when (val state = weatherState) {
                    LoadState.Empty ->
                        EmptyView(localized("Hava durumu bulunamadı", "Weather unavailable"))
                    LoadState.Loading ->
                        Row(
                            Modifier.height(63.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CircularProgressIndicator(
                                Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFFC6B5FF),
                            )
                            Text(
                                localized("Yükleniyor…", "Loading…"),
                                fontSize = 13.sp,
                                color = Color(0xFFD3D2E0),
                            )
                        }
                    LoadState.Error ->
                        Column {
                            Text(
                                localized("Hava durumu alınamadı", "Weather unavailable"),
                                fontSize = 12.sp,
                            )
                            TextButton(onClick = onRetry, contentPadding = PaddingValues(0.dp)) {
                                Text(localized("Tekrar dene", "Retry"), color = Color(0xFFC6B5FF))
                            }
                        }
                    is LoadState.Success ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            Text(
                                "${state.data.temperature.roundToInt()}°",
                                fontFamily = FontFamily.Serif,
                                fontSize = 43.sp,
                                lineHeight = 50.sp,
                            )
                            Column(
                                Modifier.padding(top = 5.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                LineWeatherIcon(state.data.weatherCode, Modifier.size(34.dp))
                                Text(
                                    weatherDescription(state.data.weatherCode, language),
                                    fontSize = 13.sp,
                                    color = Color(0xFFF4F0FA),
                                )
                            }
                        }
                }
            }
        }
    }
}
