package com.example.hava.ui.cities

import android.text.Html
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hava.model.CityPhoto
import com.example.hava.ui.components.localized
import org.json.JSONArray

private data class PhotoCredit(
    val city: String,
    val author: String,
    val license: String,
    val url: String,
    val licenseUrl: String,
)

@Composable
fun PhotoCreditsDialog(
    photo: CityPhoto? = null,
    photos: Collection<CityPhoto> = emptyList(),
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val uri = LocalUriHandler.current
    val localCredits = remember {
        runCatching {
                val json =
                    JSONArray(
                        context.assets.open("photo_credits.json").bufferedReader().use {
                            it.readText()
                        }
                    )
                (0 until json.length()).map { index ->
                    val entry = json.getJSONObject(index)
                    PhotoCredit(
                        entry.optString("page"),
                        Html.fromHtml(entry.optString("author"), Html.FROM_HTML_MODE_LEGACY)
                            .toString(),
                        entry.optString("license"),
                        entry.optString("source"),
                        entry.optString("licenseUrl"),
                    )
                }
            }
            .getOrDefault(emptyList())
    }
    fun CityPhoto.asCredit() =
        PhotoCredit(
            city,
            Html.fromHtml(author, Html.FROM_HTML_MODE_LEGACY).toString(),
            license,
            source,
            licenseUrl,
        )
    val credits =
        if (photo != null) listOf(photo.asCredit())
        else (photos.map { it.asCredit() } + localCredits).distinctBy { it.url }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF152137),
        title = { Text(localized("Fotoğraf kaynakları", "Photo credits")) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Text(
                        localized(
                            "Fotoğraflar kartlara sığacak şekilde kırpılarak ve koyu bir katmanla gösterilir. Görseller kendi belirtilen lisanslarıyla sunulur.",
                            "Photos are cropped to fit cards and displayed with a dark overlay. Each photo retains its stated license.",
                        ),
                        fontSize = 12.sp,
                    )
                }
                items(credits) { credit ->
                    Column {
                        Text(credit.city, fontSize = 16.sp)
                        Text("${credit.author}\n${credit.license}", fontSize = 11.sp)
                        Row {
                            TextButton(onClick = { uri.openUri(credit.url) }) {
                                Text(localized("Kaynak", "Source"), fontSize = 12.sp)
                            }
                            if (credit.licenseUrl.startsWith("http"))
                                TextButton(onClick = { uri.openUri(credit.licenseUrl) }) {
                                    Text(localized("Lisans", "License"), fontSize = 12.sp)
                                }
                        }
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(localized("Kapat", "Close")) } },
    )
}
