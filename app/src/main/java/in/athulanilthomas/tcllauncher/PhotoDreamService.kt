package `in`.athulanilthomas.tcllauncher

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.service.dreams.DreamService
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextClock
import coil.dispose
import coil.load

/** Android controls when this opt-in screensaver starts; any remote key exits it. */
class PhotoDreamService : DreamService() {
    private val handler = Handler(Looper.getMainLooper())
    private var image: ImageView? = null
    private var clock: TextClock? = null
    private var index = 0
    private val rotate = object : Runnable {
        override fun run() {
            val photos = LauncherPreferences(this@PhotoDreamService).photos()
            if (photos.isNotEmpty()) image?.load(photos[index % photos.size])
            index++
            // Shift the clock to reduce static bright pixels on long sessions.
            clock?.translationX = ((index % 5) * 24).toFloat()
            clock?.translationY = ((index % 3) * 16).toFloat()
            handler.postDelayed(this, 30_000)
        }
    }
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true
        isScreenBright = false
        val layout = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        image = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            alpha = .65f
        }
        layout.addView(image, FrameLayout.LayoutParams(-1, -1))
        clock = TextClock(this).apply {
            textSize = 36f
            setTextColor(Color.WHITE)
            setPadding(48, 48, 48, 48)
        }
        layout.addView(clock, FrameLayout.LayoutParams(-2, -2, Gravity.TOP or Gravity.START))
        setContentView(layout)
    }
    override fun onDreamingStarted() { super.onDreamingStarted(); handler.post(rotate) }
    override fun onDreamingStopped() { handler.removeCallbacks(rotate); image?.dispose(); super.onDreamingStopped() }
    override fun onDetachedFromWindow() { handler.removeCallbacks(rotate); image?.dispose(); image = null; clock = null; super.onDetachedFromWindow() }
}
