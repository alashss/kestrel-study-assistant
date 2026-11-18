package com.aiwolfie.kestrel

import android.app.*
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.*
import android.widget.*

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private var bubble: TextView? = null
    private var panel: LinearLayout? = null
    private var privacyMode = false
    private var params: WindowManager.LayoutParams? = null
    private val channel = "kestrel_overlay"
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotification()
        startForeground(41, Notification.Builder(this, channel).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Kestrel Study aktif").setContentText("Bubble belajar berjalan. Ketuk untuk membuka menu.").setOngoing(true).build())
        showBubble()
    }

    private fun createNotification() {
        if (Build.VERSION.SDK_INT >= 26) {
            val c = NotificationChannel(channel, "Kestrel Study", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(c)
        }
    }

    private fun layoutType(): Int = if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE

    private fun showBubble() {
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return }
        val b = TextView(this).apply {
            text = "K"; textSize = 20f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER
            setTextColor(0xFFFFFFFF.toInt()); setBackgroundColor(0xFF6750A4.toInt()); elevation = dp(8).toFloat()
            setOnClickListener { togglePanel() }
        }
        bubble = b
        val p = WindowManager.LayoutParams(dp(54), dp(54), layoutType(), WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.START; x = dp(16); y = dp(180) }
        params = p
        b.setOnTouchListener(object : View.OnTouchListener {
            var sx = 0f; var sy = 0f; var ox = 0; var oy = 0; var moved = false
            override fun onTouch(v: View, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> { sx = e.rawX; sy = e.rawY; ox = p.x; oy = p.y; moved = false; return true }
                    MotionEvent.ACTION_MOVE -> { val dx = (e.rawX - sx).toInt(); val dy = (e.rawY - sy).toInt(); if (kotlin.math.abs(dx) + kotlin.math.abs(dy) > dp(6)) moved = true; p.x = ox + dx; p.y = oy + dy; wm.updateViewLayout(b, p); return true }
                    MotionEvent.ACTION_UP -> { if (!moved) togglePanel(); return true }
                }; return false
            }
        })
        wm.addView(b, p)
    }

    private fun togglePanel() {
        if (panel != null) { removePanel(); return }
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), dp(10), dp(12), dp(10)); setBackgroundColor(0xFFF7F5FC.toInt()); elevation = dp(12).toFloat() }
        box.addView(TextView(this).apply { text = if (privacyMode) "Kestrel • Privasi" else "Kestrel Study"; textSize = 16f; setTextColor(0xFF211A2C.toInt()); setPadding(0, 0, 0, dp(6)) })
        if (!privacyMode) {
            addAction(box, "Jawab otomatis", "Buka asisten untuk memasukkan soal latihan.") { openAssistant() }
            addAction(box, "Penjelasan lengkap", "Buka asisten dan pilih penjelasan lengkap.") { openAssistant() }
            addAction(box, "Tanya ChatGPT", "Masukkan soal lalu bagikan ke ChatGPT.") { openAssistant() }
        }
        addAction(box, if (privacyMode) "Tampilkan menu" else "Samarkan tampilan", "Mode privasi mengecilkan bubble dan menyembunyikan menu fitur.") {
            privacyMode = !privacyMode
            bubble?.apply { text = if (privacyMode) "•" else "K"; alpha = if (privacyMode) 0.55f else 1f }
            removePanel(); togglePanel()
        }
        addAction(box, "Tutup bubble", "Menghentikan layanan overlay.") { stopSelf() }
        val pp = WindowManager.LayoutParams(dp(220), WindowManager.LayoutParams.WRAP_CONTENT, layoutType(), WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.TOP or Gravity.START
            val bp = params; x = (bp?.x ?: dp(16)).coerceAtMost(resources.displayMetrics.widthPixels - dp(230)); y = (bp?.y ?: dp(180)) + dp(62)
        }
        panel = box
        try { wm.addView(box, pp) } catch (_: Exception) { panel = null }
    }

    private fun addAction(parent: LinearLayout, label: String, description: String, action: () -> Unit) {
        val b = Button(this).apply { text = label; textSize = 13f; setOnClickListener { action() } }
        parent.addView(b, LinearLayout.LayoutParams(-1, dp(44)))
    }
    private fun removePanel() { panel?.let { try { wm.removeView(it) } catch (_: Exception) {} }; panel = null }
    private fun openAssistant() { val i = Intent(this, AssistantActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i); removePanel() }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onDestroy() { removePanel(); bubble?.let { try { wm.removeView(it) } catch (_: Exception) {} }; bubble = null; super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
