package com.example.hava

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import kotlin.math.abs
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain

class LandscapeTest {
    val compose = createAndroidComposeRule<MainActivity>()
    @get:Rule val rules = RuleChain.outerRule(WeatherFixtureRule()).around(compose)

    private fun waitFor(text: String) {
        try {
            compose.waitUntil(30000) {
                compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
            }
        } catch (exception: Throwable) {
            screenshot("rotation-failure.png")
            File(
                    InstrumentationRegistry.getInstrumentation()
                        .targetContext
                        .getExternalFilesDir(null),
                    "rotation-failure.txt",
                )
                .writeText(compose.onRoot(useUnmergedTree = true).printToString())
            throw exception
        }
    }

    private fun rotate(landscape: Boolean) {
        compose.activityRule.scenario.onActivity {
            it.requestedOrientation =
                if (landscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        compose.waitUntil(15000) {
            compose.activity.resources.configuration.orientation ==
                if (landscape) Configuration.ORIENTATION_LANDSCAPE
                else Configuration.ORIENTATION_PORTRAIT
        }
        compose.waitForIdle()
        android.os.SystemClock.sleep(750)
    }

    private fun countries(name: String) {
        compose.onNode(hasText("Ülke:", substring = true)).performClick()
        compose.onNodeWithText(name).performScrollTo().performClick()
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

    @After
    fun restorePortrait() {
        rotate(false)
    }

    @Test
    fun rotationPreservesSearchLanguageCountryAndSheet() {
        rotate(false)
        compose.onNodeWithText("TR").performClick()
        countries("Almanya")
        compose.onNodeWithText("EN").performClick()
        compose.onNode(hasSetTextAction()).performTextInput("mun")
        waitFor("Munich")
        Espresso.closeSoftKeyboard()
        rotate(true)
        waitFor("Search results")
        compose.onNodeWithText("Country: Germany").assertIsDisplayed()
        compose.onNode(hasSetTextAction()).assertTextEquals("mun")
        compose.onNodeWithText("Munich").assertIsDisplayed()
        compose.onNode(hasSetTextAction()).performTextClearance()
        compose.waitForIdle()
        android.os.SystemClock.sleep(750)
        screenshot("landscape-keyboard.png")
        compose.onNodeWithTag("nav_2").assertIsDisplayed()
        Espresso.closeSoftKeyboard()
        compose.waitUntil(30000) {
            compose.onAllNodesWithText("Loading…").fetchSemanticsNodes().isEmpty()
        }
        compose.waitForIdle()
        val berlin = compose.onNodeWithText("Berlin").fetchSemanticsNode().boundsInRoot
        val hamburg = compose.onNodeWithText("Hamburg").fetchSemanticsNode().boundsInRoot
        assertTrue("Landscape city cards must share a row", abs(berlin.top - hamburg.top) < 2f)
        assertTrue(berlin.left < hamburg.left)
        screenshot("landscape-cities.png")
        compose.onNodeWithText("Country: Germany").performClick()
        compose.onAllNodes(hasSetTextAction()).onLast().performTextInput("fr")
        Espresso.closeSoftKeyboard()
        rotate(false)
        waitFor("Choose country")
        compose.onAllNodes(hasSetTextAction()).onLast().assertTextEquals("fr")
        compose.onNodeWithText("France").performScrollTo().performClick()
        waitFor("Cities in France")
    }

    @Test
    fun selectedCityTodayAndForecastSurviveRotation() {
        rotate(false)
        compose.onNodeWithText("TR").performClick()
        countries("Almanya")
        compose.onNodeWithText("Münih").performClick()
        waitFor("Saatlik tahmin")
        Espresso.closeSoftKeyboard()
        rotate(true)
        waitFor("Saatlik tahmin")
        compose.onNodeWithText("Münih").assertIsDisplayed()
        screenshot("landscape-today.png")
        compose.onNodeWithTag("nav_1").performClick()
        waitFor("7 günlük tahmin")
        compose.onNodeWithText("Yağış olasılığı").assertIsDisplayed()
        screenshot("landscape-forecast.png")
        rotate(false)
        waitFor("7 günlük tahmin")
        compose.onNodeWithText("Münih").assertIsDisplayed()
        screenshot("portrait-forecast.png")
        compose.onNodeWithTag("nav_2").performClick()
        waitFor("Almanya şehirleri")
    }
}
