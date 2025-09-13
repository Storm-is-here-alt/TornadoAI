package com.storm.tornadoai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.FrameLayout
import android.view.Gravity
import android.view.ViewGroup

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Simple UI so the project compiles and runs
        val tv = TextView(this).apply {
            text = "TornadoAI build is working 🚀"
            textSize = 20f
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        val root = FrameLayout(this).apply { addView(tv) }
        setContentView(root)
    }
}