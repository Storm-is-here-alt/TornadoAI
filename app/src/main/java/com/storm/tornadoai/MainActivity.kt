package com.storm.tornadoai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.storm.tornadoai.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var vb: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)

        vb.btnPing.setOnClickListener {
            vb.output.text = "✅ Build is stable. Ready to add features."
        }
    }
}