package com.myapp.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.myapp.admin.api.ApiClient
import com.myapp.admin.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { handleLogin() }
    }

    private fun handleLogin() {
        val password = binding.etPassword.text.toString().trim()

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            return
        }
        binding.tilPassword.error = null

        setLoading(true)

        lifecycleScope.launch {
            val result = ApiClient.login(password)
            setLoading(false)

            result.fold(
                onSuccess = {
                    startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                    finish()
                },
                onFailure = { error ->
                    binding.tilPassword.error = error.message ?: "Login failed"
                    Toast.makeText(this@LoginActivity, error.message, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnLogin.isEnabled = !loading
        binding.progressBar.isVisible = loading
    }
}
