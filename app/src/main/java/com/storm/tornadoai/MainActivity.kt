package com.storm.tornadoai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.storm.tornadoai.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.title.text = getString(R.string.app_name)
    }

    override fun onStart() {
        super.onStart()
        // Lifecycle-safe collection prevents crashes on rotate/leave
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                SystemMonitor.observe(this@MainActivity).collect { s ->
                    // CPU
                    val cpuPct = s.cpuPercent.coerceIn(0f, 100f)
                    binding.cpuPercent.text = String.format("%.1f%%", cpuPct)
                    binding.cpuBar.progress = cpuPct.toInt()

                    // Memory
                    val usedMB = s.memUsedMB
                    val freeMB = s.memAvailMB
                    val totalMB = s.memTotalMB
                    val memPct =
                        if (totalMB > 0) (usedMB * 100f / totalMB).coerceIn(0f, 100f) else 0f

                    binding.memUsed.text = "${usedMB} MB used"
                    binding.memFree.text = "${freeMB} MB free"
                    binding.memTotal.text = "${totalMB} MB total"
                    binding.memPercent.text = String.format("%.1f%%", memPct)
                    binding.memBar.progress = memPct.toInt()
                }
            }
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}