package com.example.hava

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain

class WeatherNavigationTest {
    val compose = createAndroidComposeRule<MainActivity>()
    @get:Rule val rules = RuleChain.outerRule(WeatherFixtureRule()).around(compose)

    private fun waitFor(text: String) {
        compose.waitUntil(30000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitCards() {
        compose.waitUntil(30000) {
            compose.onAllNodesWithText("Yükleniyor…").fetchSemanticsNodes().isEmpty() &&
                compose.onAllNodesWithText("Loading…").fetchSemanticsNodes().isEmpty()
        }
    }

    private fun screenshot(name: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        try {
            File(instrumentation.targetContext.getExternalFilesDir(null), name).outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        } finally {
            bitmap.recycle()
        }
    }

    private fun chooseCountry(name: String) {
        compose.onNode(hasText("Ülke:", substring = true)).performClick()
        compose.onNodeWithText(name).performClick()
    }

    @Test
    fun countriesLanguageSearchPagingAndForecast() {
        compose.onNodeWithText("TR").performClick()
        chooseCountry("Türkiye")
        waitFor("Türkiye şehirleri")
        waitCards()
        screenshot("dark-turkey.png")
        compose.onNode(hasText("Ülke:", substring = true)).performClick()
        waitFor("Ülke seç")
        screenshot("dark-country-picker.png")
        compose.onNodeWithText("Almanya").performClick()
        waitFor("Almanya şehirleri")
        waitCards()
        compose.onNodeWithText("Münih").assertIsDisplayed()
        screenshot("dark-germany-tr.png")
        compose.onNodeWithText("EN").performClick()
        waitFor("Cities in Germany")
        compose.onNodeWithText("Munich").assertIsDisplayed()
        screenshot("dark-germany-en.png")
        compose.onNodeWithText("Munich").performClick()
        waitFor("Hourly forecast")
        compose.onNodeWithText("Munich").assertIsDisplayed()
        screenshot("dark-today.png")
        compose.onNodeWithTag("nav_1").performClick()
        waitFor("7-day forecast")
        compose.onNodeWithText("Chance of rain").assertIsDisplayed()
        screenshot("dark-forecast.png")
        compose.onNodeWithTag("nav_2").performClick()
        compose.onNodeWithText("TR").performClick()
        chooseCountry("Türkiye")
        waitFor("7 / 81")
        compose.onNode(hasScrollAction()).performScrollToIndex(6)
        waitFor("14 / 81")
        compose.onNode(hasSetTextAction()).performTextInput("İSTANBUL")
        compose.onNodeWithText("İstanbul").assertIsDisplayed()
        compose.onNode(hasSetTextAction()).performTextClearance()
        compose.onNodeWithText("EN").performClick()
        compose.onNodeWithText("Country: Türkiye").performClick()
        compose
            .onNode(
                hasSetTextAction() and hasAnyAncestor(hasText("Choose country", substring = true))
            )
            .assertDoesNotExist()
        // Both search fields exist while the sheet is open; use the visible placeholder's parent.
        compose.onAllNodes(hasSetTextAction()).onLast().performTextInput("ger")
        compose.onNodeWithText("Germany").assertIsDisplayed()
        compose.onNodeWithText("France").assertDoesNotExist()
        compose.onNodeWithText("Germany").performClick()
        compose.onNodeWithText("TR").performClick()
        waitFor("Almanya şehirleri")
    }
}
