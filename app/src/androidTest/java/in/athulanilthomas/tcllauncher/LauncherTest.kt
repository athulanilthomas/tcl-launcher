package `in`.athulanilthomas.tcllauncher

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class LauncherTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun remoteNavigationAndBack() {
        select("All apps")
        awaitDisplayed("Installed apps")
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        awaitDisplayed("Welcome home")
        select("Personalize")
        select("Favorite 1")
        awaitDisplayed("Choose favorite 1")
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        awaitDisplayed("Choose photos")
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        awaitDisplayed("Welcome home")
        // Back at home must keep the launcher usable.
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        awaitDisplayed("Welcome home")
        compose.onNodeWithTag("settings").performSemanticsAction(SemanticsActions.RequestFocus)
        compose.onNodeWithTag("settings").performKeyInput { pressKey(Key.DirectionRight) }
        compose.onNodeWithText("Inputs").assertIsFocused()
    }

    private fun select(text: String) {
        val node = compose.onNodeWithText(text)
        node.performSemanticsAction(SemanticsActions.RequestFocus)
        node.assertIsFocused()
        node.performKeyInput { pressKey(Key.DirectionCenter) }
    }

    private fun awaitDisplayed(text: String) {
        try {
            compose.waitUntil(5_000) {
                runCatching { compose.onNodeWithText(text).assertIsDisplayed(); true }.getOrDefault(false)
            }
        } catch (failure: Throwable) {
            throw AssertionError("Waiting for $text\n" + compose.onRoot().printToString(), failure)
        }
    }

    @Test fun homeIntentReturnsFromLibrary() {
        select("All apps")
        awaitDisplayed("Installed apps")
        compose.activityRule.scenario.onActivity {
            it.startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                .setClass(it, MainActivity::class.java))
        }
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Welcome home").fetchSemanticsNodes().isNotEmpty() }
        awaitDisplayed("Welcome home")
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
            awaitDisplayed("Choose app 1")
        } finally { preferences.saveFavorites(before) }
    }
}
