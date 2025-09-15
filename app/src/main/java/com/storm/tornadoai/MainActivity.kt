package com.storm.tornadoai

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.lifecycle.repeatOnLifecycle

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cpuText = findViewById<TextView>(R.id.cpuText)
        val memText = findViewById<TextView>(R.id.memText)

        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                SystemMonitor.observe(this@MainActivity, 1000L).collect { s ->
                    cpuText.text = "CPU: ${"%.1f".format(s.cpuPercent)} %"
                    memText.text = "RAM: ${s.memUsedMB}/${s.memTotalMB} MB"
                }
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ChatFragment())
                .commit()
        }
    }
}