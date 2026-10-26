package com.example.hava.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hava.ui.HavaDestination
import com.example.hava.ui.theme.*

@Composable
fun HavaNavigation(
    selected: HavaDestination,
    landscape: Boolean = false,
    onSelect: (HavaDestination) -> Unit,
) {
    val compact = landscape && WindowInsets.ime.getBottom(LocalDensity.current) > 0
    Surface(color = Color(0xFF080F1C)) {
        if (landscape) {
            Column(
                Modifier.width(76.dp).fillMaxHeight().padding(vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HavaDestination.entries.forEach { destination ->
                    NavigationItem(
                        destination,
                        selected == destination,
                        compact,
                        onSelect,
                        Modifier.fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = if (compact) 2.dp else 12.dp),
                    )
                }
            }
        } else {
            Column {
                HorizontalDivider(color = Color(0xFF27334B), thickness = .5.dp)
                Row(
                    Modifier.fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(top = 12.dp, bottom = 6.dp)
                ) {
                    HavaDestination.entries.forEach { destination ->
                        NavigationItem(
                            destination,
                            selected == destination,
                            false,
                            onSelect,
                            Modifier.weight(1f).padding(vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationItem(
    destination: HavaDestination,
    selected: Boolean,
    compact: Boolean,
    onSelect: (HavaDestination) -> Unit,
    modifier: Modifier,
) {
    val label =
        when (destination) {
            HavaDestination.Today -> localized("Bugün", "Today")
            HavaDestination.Forecast -> localized("Tahmin", "Forecast")
            HavaDestination.Cities -> localized("Şehirler", "Cities")
        }
    val icon =
        when (destination) {
            HavaDestination.Today -> Icons.Outlined.WbSunny
            HavaDestination.Forecast -> Icons.Outlined.BarChart
            HavaDestination.Cities -> Icons.Outlined.Place
        }
    val tint = if (selected) WeatherBlue else Color(0xFFA2A7C1)
    Column(
        modifier
            .testTag("nav_${destination.tabIndex}")
            .selectable(selected, role = Role.Tab, onClick = { onSelect(destination) }),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.spacedBy(if (compact) 2.dp else 6.dp, Alignment.CenterVertically),
    ) {
        Icon(icon, null, Modifier.size(if (compact) 20.dp else 26.dp), tint = tint)
        Text(
            label,
            color = tint,
            fontSize = if (compact) 10.sp else 12.sp,
            lineHeight = if (compact) 12.sp else 16.sp,
        )
        if (!compact)
            Box(
                Modifier.size(5.dp)
                    .background(if (selected) tint else Color.Transparent, CircleShape)
            )
    }
}
