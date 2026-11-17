package in.athulanilthomas.tcllauncher

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class TvApp(val label: String, val packageName: String, val launchIntent: Intent)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                LauncherScreen(
                    apps = remember { installedApps() },
                    launch = { app -> launchApp(app) },
                    openSettings = { openSettings() }
                )
            }
        }
    }

    private fun installedApps(): List<TvApp> {
        val pm = packageManager
        val categories = listOf(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER)
        return categories.flatMap { category ->
            val query = Intent(Intent.ACTION_MAIN).addCategory(category)
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(query, 0).mapNotNull { info ->
                val pkg = info.activityInfo.packageName
                if (pkg == packageName) return@mapNotNull null
                val intent = Intent(Intent.ACTION_MAIN)
                    .addCategory(category)
                    .setClassName(pkg, info.activityInfo.name)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                TvApp(info.loadLabel(pm).toString(), pkg, intent)
            }
        }.distinctBy { it.packageName }.sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    private fun launchApp(app: TvApp) {
        try {
            startActivity(app.launchIntent)
        } catch (_: ActivityNotFoundException) {
            // The app may have been removed or disabled since the launcher opened.
        } catch (_: SecurityException) {
            // A device policy may restrict starting an otherwise visible app.
        }
    }

    private fun openSettings() {
        try {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        } catch (_: ActivityNotFoundException) {
            // Some TV firmware hides the standard Settings entry point.
        }
    }
}

@Composable
private fun LauncherScreen(
    apps: List<TvApp>,
    launch: (TvApp) -> Unit,
    openSettings: () -> Unit
) {
    var showLibrary by remember { mutableStateOf(false) }
    BackHandler(enabled = showLibrary) { showLibrary = false }

    val background = Brush.linearGradient(
        colors = listOf(Color(0xFF111C30), Color(0xFF172A36), Color(0xFF080E18))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(horizontal = 48.dp, vertical = 32.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            ClockAndDate()
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = openSettings) { Text("Settings") }
                Button(onClick = { showLibrary = !showLibrary }) {
                    Text(if (showLibrary) "Home" else "All apps")
                }
            }
        }
        Spacer(Modifier.weight(1f))
        if (showLibrary) {
            Text("Installed apps", fontSize = 28.sp, color = Color.White)
            Spacer(Modifier.height(16.dp))
            if (apps.isEmpty()) {
                Text("No launchable apps found", color = Color.White)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(apps, key = { it.packageName }) { app ->
                        Button(onClick = { launch(app) }, modifier = Modifier.fillMaxWidth()) {
                            Text(app.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        } else {
            Text("Welcome home", fontSize = 34.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text("Your TV. Your space.", fontSize = 18.sp, color = Color(0xFFCCD7E0))
            Spacer(Modifier.height(30.dp))
            Text("Favorites", fontSize = 20.sp, color = Color.White)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                apps.take(4).forEach { app ->
                    Button(
                        onClick = { launch(app) },
                        modifier = Modifier.width(190.dp).height(60.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF324B62))
                    ) {
                        Text(app.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            if (apps.isEmpty()) {
                Text("Use All apps after installing TV applications.", color = Color.LightGray)
            }
        }
    }
}

@Composable
private fun ClockAndDate() {
    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = Date()
            delay(1000L)
        }
    }
    val time = remember(now) { SimpleDateFormat("h:mm a", Locale.getDefault()).format(now) }
    val date = remember(now) { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(now) }
    Column {
        Text(time, fontSize = 46.sp, color = Color.White, fontWeight = FontWeight.Light)
        Text(date, fontSize = 17.sp, color = Color(0xFFCAD5E2))
    }
}
