package com.storm.tornadoai

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.storm.tornadoai.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val sharedVm: SharedPrefsViewModel by viewModels() // holds bias filter across tabs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        val nav = findViewById<BottomNavigationView>(R.id.bottomNav)
        nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_chat -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, ChatFragment())
                        .commit()
                    true
                }
                R.id.menu_monitor -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, MonitorFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
        nav.selectedItemId = R.id.menu_chat
    }
}