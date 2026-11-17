package `in`.athulanilthomas.tcllauncher

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date
import java.util.Locale

internal data class TvApp(val label: String, val packageName: String, val intent: Intent)

class MainActivity : ComponentActivity() {
    private var homeRequests by mutableIntStateOf(0)
    private val preferences by lazy { LauncherPreferences(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { LauncherScreen() } }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME)) homeRequests++
    }

    @Suppress("DEPRECATION")
    private fun installedApps(): List<TvApp> = listOf(
        Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER
    ).flatMap { category ->
        packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(category), 0)
            .mapNotNull { info ->
                val activity = info.activityInfo
                if (activity.packageName == packageName || !activity.exported || !activity.enabled || !activity.applicationInfo.enabled) null
                else TvApp(info.loadLabel(packageManager).toString(), activity.packageName,
                    Intent(Intent.ACTION_MAIN).addCategory(category)
                        .setClassName(activity.packageName, activity.name)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED))
            }
    }.distinctBy { it.packageName }.sortedBy { it.label.lowercase(Locale.getDefault()) }

    private fun open(vararg intents: Intent) {
        for (intent in intents) {
            try { startActivity(intent); return }
            catch (_: ActivityNotFoundException) { }
            catch (_: SecurityException) { }
        }
        Toast.makeText(this, "This action is unavailable. Try TV Settings or the remote's Input button.", Toast.LENGTH_LONG).show()
    }

    @Composable
    private fun LauncherScreen() {
        var apps by remember { mutableStateOf(emptyList<TvApp>()) }
        var favorites by remember { mutableStateOf<List<String?>>(emptyList()) }
        var photos by remember { mutableStateOf(preferences.photos()) }
        var page by remember { mutableStateOf("home") }
        var editingSlot by remember { mutableStateOf<Int?>(null) }
        var refresh by remember { mutableIntStateOf(0) }
        LaunchedEffect(homeRequests) { page = "home"; editingSlot = null }
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        DisposableEffect(lifecycle) {
            val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) refresh++ }
            lifecycle.addObserver(observer)
            onDispose { lifecycle.removeObserver(observer) }
        }
        LaunchedEffect(refresh) {
            apps = withContext(Dispatchers.IO) { installedApps() }
            favorites = preferences.favorites(apps.take(4).map { it.packageName })
            preferences.saveFavorites(favorites)
        }
        val homeFocus = remember { FocusRequester() }
        val contentFocus = remember { FocusRequester() }
        LaunchedEffect(page, editingSlot, apps) {
            // Request after the destination has composed, including Lazy grid items.
            delay(100)
            if (page == "home") homeFocus.requestFocus() else contentFocus.requestFocus()
        }
        BackHandler {
            if (editingSlot != null) { editingSlot = null; page = "personalize" }
            else page = "home"
        }
        val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            if (uris.isNotEmpty()) {
                val retained = uris.take(20).filter { uri ->
                    runCatching { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }.isSuccess
                }
                if (retained.isNotEmpty()) {
                    val previous = photos
                    preferences.savePhotos(retained)
                    photos = retained
                    previous.filterNot { it in retained }.forEach { uri ->
                        runCatching { contentResolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                    }
                } else Toast.makeText(this, "Unable to keep access to these photos. Try a local document provider.", Toast.LENGTH_LONG).show()
            }
        }
        var photoIndex by remember(photos) { mutableIntStateOf(0) }
        LaunchedEffect(photos) {
            while (photos.size > 1) { delay(30_000); photoIndex = (photoIndex + 1) % photos.size }
        }
        Box(Modifier.fillMaxSize().background(Color(0xFF111C30))) {
            photos.getOrNull(photoIndex)?.let { uri ->
                AsyncImage(model = uri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            }
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = .55f), Color.Black.copy(alpha = .8f)))))
            Column(Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 24.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ClockAndDate()
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Action("Settings", Modifier.focusRequester(homeFocus)) { open(Intent(Settings.ACTION_SETTINGS)) }
                        Action("Inputs") { open(Intent("android.settings.TV_INPUT_SETTINGS"), Intent(Settings.ACTION_SETTINGS)) }
                        Action(if (page == "home") "All apps" else "Home") { editingSlot = null; page = if (page == "home") "library" else "home" }
                        Action("Personalize") { editingSlot = null; page = "personalize" }
                    }
                }
                Spacer(Modifier.height(24.dp))
                when (page) {
                    "home" -> {
                        Spacer(Modifier.weight(1f))
                        Text("Welcome home", fontSize = 34.sp, fontWeight = FontWeight.Medium, color = Color.White)
                        Text("Your TV. Your space.", fontSize = 18.sp, color = Color.LightGray)
                        Spacer(Modifier.height(24.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            List(4) { favorites.getOrNull(it) }.forEachIndexed { slot, pkg ->
                                val app = apps.find { it.packageName == pkg }
                                Action(app?.label ?: "Choose app ${slot + 1}", Modifier.weight(1f).height(64.dp)) {
                                    if (app != null) open(app.intent) else { editingSlot = slot; page = "library" }
                                }
                            }
                        }
                    }
                    "library" -> {
                        Text(editingSlot?.let { "Choose favorite ${it + 1}" } ?: "Installed apps", fontSize = 26.sp, color = Color.White)
                        Spacer(Modifier.height(16.dp))
                        if (apps.isEmpty()) Action("No apps found · Back", Modifier.focusRequester(contentFocus)) { editingSlot = null; page = "home" }
                        else LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(8.dp)) {
                            items(apps, key = { it.packageName }) { app ->
                                Action(app.label, Modifier.height(64.dp).then(if (app == apps.first()) Modifier.focusRequester(contentFocus) else Modifier)) {
                                    val slot = editingSlot
                                    if (slot == null) open(app.intent)
                                    else {
                                        favorites = List(4) { if (it == slot) app.packageName else favorites.getOrNull(it) }
                                        preferences.saveFavorites(favorites)
                                        editingSlot = null; page = "personalize"
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        Text("Personalize", fontSize = 26.sp, color = Color.White)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            repeat(4) { slot ->
                                Action("Favorite ${slot + 1}", Modifier.then(if (slot == 0) Modifier.focusRequester(contentFocus) else Modifier)) { editingSlot = slot; page = "library" }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Action("Choose photos") {
                                try { picker.launch(arrayOf("image/*")) }
                                catch (_: ActivityNotFoundException) { Toast.makeText(this@MainActivity, "No file picker installed. Install a document provider to select local photos.", Toast.LENGTH_LONG).show() }
                            }
                            Action("Clear photos") {
                                val previous = photos
                                photos = emptyList(); preferences.savePhotos(emptyList())
                                previous.forEach { runCatching { contentResolver.releasePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) } }
                            }
                            Action("Screensaver settings") { open(Intent(Settings.ACTION_DREAM_SETTINGS), Intent(Settings.ACTION_SETTINGS)) }
                        }
                        Spacer(Modifier.height(24.dp))
                        Text("Photos stay on your TV. Up to 20 photos rotate every 30 seconds.\nSelect TCL Launcher in Android screensaver settings to use the photo screensaver.\nBack returns home. The stock launcher and TV settings remain available.", color = Color.White, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun Action(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier) {
        Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ClockAndDate() {
    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) { while (true) { now = Date(); delay(1000) } }
    val context = androidx.compose.ui.platform.LocalContext.current
    Column {
        Text(android.text.format.DateFormat.getTimeFormat(context).format(now), fontSize = 40.sp, color = Color.White)
        Text(DateFormat.getDateInstance(DateFormat.FULL).format(now), fontSize = 16.sp, color = Color.LightGray)
    }
}
