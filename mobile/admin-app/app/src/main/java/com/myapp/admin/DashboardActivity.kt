package com.myapp.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.myapp.admin.api.ApiClient
import com.myapp.admin.databinding.ActivityDashboardBinding
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogout.setOnClickListener {
            ApiClient.authToken = null
            finish()
        }

        loadStats()
    }

    private fun loadStats() {
        setLoading(true)

        lifecycleScope.launch {
            val result = ApiClient.getStats()
            setLoading(false)

            result.fold(
                onSuccess = { stats ->
                    binding.tvTotalUsers.text = stats.totalUsers.toString()
                    binding.tvUptime.text = formatUptime(stats.uptime)
                },
                onFailure = { error ->
                    Toast.makeText(this@DashboardActivity, error.message, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.isVisible = loading
        binding.cardUsers.isVisible = !loading
        binding.cardUptime.isVisible = !loading
    }

    private fun formatUptime(seconds: Double): String {
        val hrs = (seconds / 3600).toInt()
        val mins = ((seconds % 3600) / 60).toInt()
        val secs = (seconds % 60).toInt()
        return if (hrs > 0) "${hrs}h ${mins}m ${secs}s" else "${mins}m ${secs}s"
    }
}
