package com.storm.tornadoai

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

class MonitorFragment : Fragment(R.layout.fragment_monitor) {

    private lateinit var cpuText: TextView
    private lateinit var memText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cpuText = view.findViewById(R.id.cpuText)
        memText = view.findViewById(R.id.memText)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                SystemMonitor.observe(requireContext(), intervalMs = 1000L).collect { s ->
                    cpuText.text = "CPU: ${"%.1f".format(s.cpuPercent)} %"
                    memText.text = "RAM: ${s.memUsedMB}/${s.memTotalMB} MB (free ${s.memAvailMB} MB)"
                }
            }
        }
    }
}