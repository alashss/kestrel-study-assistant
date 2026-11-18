package com.aiwolfie.kestrel

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(30), dp(22), dp(20))
            setBackgroundColor(0xFFF7F5FC.toInt())
        }
        root.addView(TextView(this).apply {
            text = "Kestrel Study"
            textSize = 28f
            setTextColor(0xFF211A2C.toInt())
        })
        root.addView(TextView(this).apply {
            text = "Asisten belajar dengan bubble mengambang, jawaban AI, penjelasan, dan akses ChatGPT."
            textSize = 15f
            setTextColor(0xFF625B71.toInt())
            setPadding(0, dp(8), 0, dp(18))
        })
        addButton(root, "Izinkan tampilan mengambang") {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            } else showMessage(root, "Izin overlay sudah aktif.")
        }
        addButton(root, "Jalankan bubble") {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            } else {
                val i = Intent(this, OverlayService::class.java)
                if (Build.VERSION.SDK_INT >= 26) startForegroundService(i) else startService(i)
                showMessage(root, "Bubble aktif. Tarik bubble untuk memindahkannya.")
            }
        }
        addButton(root, "Buka asisten AI") { startActivity(Intent(this, AssistantActivity::class.java)) }
        addButton(root, "Hentikan bubble") { stopService(Intent(this, OverlayService::class.java)); showMessage(root, "Layanan bubble dihentikan.") }
        root.addView(TextView(this).apply {
            text = "Catatan: overlay memerlukan izin khusus Android. Fitur ini tidak membaca layar atau mengisi aplikasi lain secara diam-diam. Masukkan soal latihan sendiri atau bagikan teks soal ke aplikasi ini."
            textSize = 13f
            setTextColor(0xFF625B71.toInt())
            setPadding(0, dp(22), 0, 0)
        })
        setContentView(root)
    }

    private fun addButton(root: LinearLayout, label: String, action: () -> Unit) {
        root.addView(Button(this).apply { text = label; setOnClickListener { action() } },
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)).apply { bottomMargin = dp(8) })
    }
    private fun showMessage(root: LinearLayout, msg: String) {
        (root.getChildAt(root.childCount - 1) as? TextView)?.text = msg
    }
}
