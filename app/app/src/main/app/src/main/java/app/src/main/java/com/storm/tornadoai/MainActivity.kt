package com.storm.tornadoai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.storm.tornadoai.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.textView.text = "TornadoAI is alive ✅"
    }
}