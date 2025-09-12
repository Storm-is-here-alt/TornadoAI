package com.storm.tornadoai

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var chatContainer: LinearLayout
    private lateinit var scrollView: ScrollView
    private lateinit var inputText: EditText
    private lateinit var sendButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chatContainer = findViewById(R.id.chatContainer)
        scrollView = findViewById(R.id.scrollView)
        inputText = findViewById(R.id.inputText)
        sendButton = findViewById(R.id.sendButton)

        sendButton.setOnClickListener {
            val msg = inputText.text.toString().trim()
            if (msg.isNotEmpty()) {
                addMessage("You", msg)
                inputText.text.clear()
                fakeReply()
            }
        }
    }

    private fun addMessage(sender: String, message: String) {
        val textView = TextView(this)
        textView.text = "$sender: $message"
        textView.setTextColor(0xFFFFFFFF.toInt())
        textView.setPadding(8, 4, 8, 4)

        chatContainer.addView(textView)

        // auto-scroll
        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
    }

    // Placeholder bot reply
    private fun fakeReply() {
        addMessage("TornadoAI", "⚡ Processing... (hook me to backend later)")
    }
}