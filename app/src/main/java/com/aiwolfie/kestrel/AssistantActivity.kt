package com.aiwolfie.kestrel

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class AssistantActivity : Activity() {
    private lateinit var question: EditText
    private lateinit var answer: TextView
    private lateinit var apiKey: EditText
    private lateinit var model: EditText
    private lateinit var kkm: EditText
    private val prefs by lazy { getSharedPreferences("kestrel_settings", MODE_PRIVATE) }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(22), dp(18), dp(18)); setBackgroundColor(0xFFF7F5FC.toInt()) }
        root.addView(TextView(this).apply { text = "Asisten belajar"; textSize = 25f; setTextColor(0xFF211A2C.toInt()) })
        question = EditText(this).apply { hint = "Tempel atau ketik soal latihan di sini"; minLines = 4; gravity = android.view.Gravity.TOP; setText(intent.getStringExtra(Intent.EXTRA_TEXT) ?: "") }
        root.addView(question, LinearLayout.LayoutParams(-1, dp(130)))
        apiKey = EditText(this).apply { hint = "OpenAI API key (disimpan lokal di perangkat)"; setSingleLine(true); inputType = 129; setText(prefs.getString("api_key", "")) }
        root.addView(apiKey)
        model = EditText(this).apply { hint = "Model API"; setSingleLine(true); setText(prefs.getString("model", "gpt-4o-mini")) }
        root.addView(model)
        kkm = EditText(this).apply { hint = "Target nilai / KKM (contoh: 75)"; inputType = 2; setText(prefs.getString("kkm", "75")) }
        root.addView(kkm)
        addButton(root, "Simpan pengaturan") {
            prefs.edit().putString("api_key", apiKey.text.toString().trim()).putString("model", model.text.toString().trim().ifBlank { "gpt-4o-mini" }).putString("kkm", kkm.text.toString().trim().ifBlank { "75" }).apply()
            toast("Pengaturan disimpan di perangkat.")
        }
        addButton(root, "Jawab otomatis (soal latihan)") { askAi(false) }
        addButton(root, "Penjelasan lengkap") { askAi(true) }
        addButton(root, "Tanya ChatGPT") { shareToChatGPT() }
        answer = TextView(this).apply { text = "Jawaban dan pembahasan akan muncul di sini."; textSize = 15f; setTextColor(0xFF292333.toInt()); setPadding(0, dp(14), 0, 0) }
        val scroll = ScrollView(this).apply { addView(answer) }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun addButton(root: LinearLayout, label: String, action: () -> Unit) {
        root.addView(Button(this).apply { text = label; setOnClickListener { action() } }, LinearLayout.LayoutParams(-1, dp(50)))
    }

    private fun askAi(detailed: Boolean) {
        val q = question.text.toString().trim()
        val key = apiKey.text.toString().trim()
        if (q.isBlank()) { toast("Masukkan soal terlebih dahulu."); return }
        if (key.isBlank()) { toast("Isi API key di pengaturan terlebih dahulu."); return }
        prefs.edit().putString("api_key", key).putString("model", model.text.toString().trim().ifBlank { "gpt-4o-mini" }).putString("kkm", kkm.text.toString()).apply()
        answer.text = "Memproses soal latihan…"
        val target = kkm.text.toString().toIntOrNull() ?: 75
        val instruction = if (detailed) "Jelaskan konsep, langkah penyelesaian, perhitungan, dan verifikasi jawaban secara lengkap." else "Berikan jawaban paling tepat, lalu alasan singkat dan langkah inti."
        val prompt = "Kamu tutor belajar. Bantu untuk latihan dan pemahaman, bukan mengerjakan ujian aktif atas nama siswa. Target belajar pengguna: nilai $target/100. $instruction\n\nSoal:\n$q"
        thread {
            try {
                val conn = (URL("https://api.openai.com/v1/chat/completions").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"; connectTimeout = 20000; readTimeout = 60000; doOutput = true
                    setRequestProperty("Authorization", "Bearer $key")
                    setRequestProperty("Content-Type", "application/json")
                }
                val body = JSONObject().put("model", model.text.toString().trim().ifBlank { "gpt-4o-mini" })
                    .put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
                    .put("temperature", 0.2)
                conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val raw = stream.bufferedReader().use { it.readText() }
                val result = if (code in 200..299) JSONObject(raw).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content") else "API error ($code): $raw"
                runOnUiThread { answer.text = result }
                conn.disconnect()
            } catch (e: Exception) { runOnUiThread { answer.text = "Gagal menghubungi API: ${e.message}\nPeriksa koneksi, API key, dan model." } }
        }
    }

    private fun shareToChatGPT() {
        val q = question.text.toString().trim()
        if (q.isBlank()) { toast("Masukkan soal terlebih dahulu."); return }
        val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "Bantu saya memahami dan menyelesaikan soal latihan berikut dengan langkah yang jelas:\n\n$q") }
        startActivity(Intent.createChooser(send, "Kirim soal ke ChatGPT atau aplikasi AI lain"))
    }
    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}
