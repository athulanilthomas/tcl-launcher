package `in`.athulanilthomas.tcllauncher

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class LauncherTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @OptIn(ExperimentalTestApi::class)
    @Test fun remoteNavigationAndBack() {
        compose.onNodeWithText("All apps").performClick()
        compose.onNodeWithText("Installed apps").assertIsDisplayed()
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Welcome home").assertIsDisplayed()
        compose.onNodeWithText("Personalize").performClick()
        compose.onNodeWithText("Favorite 1").performClick()
        compose.onNodeWithText("Choose favorite 1").assertIsDisplayed()
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Choose photos").assertIsDisplayed()
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Welcome home").assertIsDisplayed()
        // Back at home must keep the launcher usable.
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Welcome home").assertIsDisplayed()
        compose.onNodeWithText("Settings").requestFocus().performKeyInput { pressKey(androidx.compose.ui.input.key.Key.DirectionRight) }
        compose.onNodeWithText("Inputs").assertIsFocused()
    }

    @Test fun homeIntentReturnsFromLibrary() {
        compose.onNodeWithText("All apps").performClick()
        compose.onNodeWithText("Installed apps").assertIsDisplayed()
        compose.activityRule.scenario.onActivity {
            it.startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                .setClass(it, MainActivity::class.java))
        }
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Welcome home").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Welcome home").assertIsDisplayed()
    }

    @Test fun preferencesSurviveRecreationAndMissingApps() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = LauncherPreferences(context)
        val before = preferences.favorites(emptyList())
        try {
            preferences.saveFavorites(listOf("missing.app", "two", null, "four"))
            val reloaded = LauncherPreferences(context).favorites(listOf("different.default"))
            assertEquals(listOf("missing.app", "two", null, "four"), reloaded)
            compose.activityRule.scenario.recreate()
            compose.waitForIdle()
            compose.onNodeWithText("Choose app 1").assertIsDisplayed()
        } finally { preferences.saveFavorites(before) }
    }
}
