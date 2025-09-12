package com.storm.tornadoai

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var chatContainer: LinearLayout
    private lateinit var scrollView: ScrollView
    private lateinit var inputText: EditText
    private lateinit var sendButton: Button

    private val client = OkHttpClient()
    private val BASE_URL = "http://127.0.0.1:5010"   // Flask router in Termux

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chatContainer = findViewById(R.id.chatContainer)
        scrollView = findViewById(R.id.scrollView)
        inputText = findViewById(R.id.inputText)
        sendButton = findViewById(R.id.sendButton)

        // Ping backend on launch so we see status in the chat
        healthPing()

        sendButton.setOnClickListener {
            val msg = inputText.text.toString().trim()
            if (msg.isNotEmpty()) {
                addMessage("You", msg)
                inputText.text.clear()
                sendToBackend(msg)
            }
        }
    }

    private fun addMessage(sender: String, message: String) {
        val tv = TextView(this).apply {
            text = "$sender: $message"
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(8, 6, 8, 6)
        }
        chatContainer.addView(tv)
        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
    }

    private fun healthPing() {
        Thread {
            try {
                val req = Request.Builder()
                    .url("$BASE_URL/health")
                    .get()
                    .build()
                client.newCall(req).execute().use { resp ->
                    val body = resp.body?.string() ?: "no body"
                    runOnUiThread { addMessage("Health", body) }
                }
            } catch (e: Exception) {
                runOnUiThread { addMessage("Health", "failed: ${e.message}") }
            }
        }.start()
    }

    private fun sendToBackend(message: String) {
        Thread {
            try {
                val json = JSONObject().put("message", message).toString()
                val body = RequestBody.create("application/json".toMediaType(), json)
                val req = Request.Builder()
                    .url("$BASE_URL/chat")
                    .post(body)
                    .build()
                client.newCall(req).execute().use { resp ->
                    val raw = resp.body?.string() ?: "{}"
                    val reply = try {
                        JSONObject(raw).optString("reply", raw)
                    } catch (_: Exception) {
                        raw
                    }
                    runOnUiThread { addMessage("TornadoAI", reply) }
                }
            } catch (e: Exception) {
                runOnUiThread { addMessage("Error", e.message ?: "Unknown error") }
            }
        }.start()
    }
}