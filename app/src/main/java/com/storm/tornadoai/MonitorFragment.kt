package com.storm.tornadoai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.storm.tornadoai.databinding.FragmentMonitorBinding
import kotlinx.coroutines.launch

class MonitorFragment : Fragment() {

    private var _binding: FragmentMonitorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMonitorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                SystemMonitor.observe(requireContext()).collect { s ->
                    val cpu = s.cpuPercent.coerceIn(0f, 100f)
                    binding.cpuPercent.text = String.format("%.1f%%", cpu)
                    binding.cpuBar.progress = cpu.toInt()

                    val used = s.memUsedMB
                    val free = s.memAvailMB
                    val total = s.memTotalMB
                    val pct = if (total > 0) (used * 100f / total) else 0f
                    binding.memPercent.text = String.format("%.1f%%", pct)
                    binding.memBar.progress = pct.toInt()
                    binding.memUsed.text = getString(R.string.mem_used_fmt, used)
                    binding.memFree.text = getString(R.string.mem_free_fmt, free)
                    binding.memTotal.text = getString(R.string.mem_total_fmt, total)
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}