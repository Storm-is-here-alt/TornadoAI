package com.storm.tornadoai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.storm.tornadoai.databinding.ActivityMainBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var monitorJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.title.text = getString(R.string.app_name)
    }

    override fun onStart() {
        super.onStart()
        monitorJob?.cancel()
        monitorJob = lifecycleScope.launch {
            SystemMonitor.observe(this@MainActivity).collectLatest { s ->
                // CPU
                val cpuPct = s.cpuPercent.coerceIn(0f, 100f)
                binding.cpuPercent.text = String.format("%.1f%%", cpuPct)
                binding.cpuBar.progress = cpuPct.toInt()

                // Memory
                val usedMB = s.memUsedMB
                val freeMB = s.memAvailMB
                val totalMB = s.memTotalMB
                val memPct = if (totalMB > 0) (usedMB * 100f / totalMB).coerceIn(0f, 100f) else 0f

                binding.memUsed.text = "${usedMB} MB"
                binding.memFree.text = "${freeMB} MB"
                binding.memTotal.text = "${totalMB} MB"
                binding.memPercent.text = String.format("%.1f%%", memPct)
                binding.memBar.progress = memPct.toInt()
            }
        }
    }

    override fun onStop() {
        monitorJob?.cancel()
        super.onStop()
    }
}