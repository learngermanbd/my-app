package com.myapp.base

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.myapp.base.api.ApiClient
import com.myapp.base.api.User
import com.myapp.base.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSwipeRefresh()
        loadUsers()
    }

    private fun setupRecyclerView() {
        adapter = UserAdapter(emptyList()) { user ->
            showUserDetail(user)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener { loadUsers() }
    }

    private fun loadUsers() {
        lifecycleScope.launch {
            binding.swipeRefresh.isRefreshing = true
            binding.tvStatus.text = getString(R.string.loading)

            val result = ApiClient.getUsers()

            binding.swipeRefresh.isRefreshing = false

            result.fold(
                onSuccess = { response ->
                    if (response.users.isEmpty()) {
                        binding.tvStatus.text = getString(R.string.no_users)
                    } else {
                        binding.tvStatus.text = "${response.total} users loaded"
                    }
                    adapter.updateUsers(response.users)
                },
                onFailure = { error ->
                    binding.tvStatus.text = getString(R.string.error_loading)
                    Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun showUserDetail(user: User) {
        MaterialAlertDialogBuilder(this)
            .setTitle(user.name)
            .setMessage("Email: ${user.email}\n\nID: ${user.id}\nJoined: ${user.created_at}")
            .setPositiveButton("OK", null)
            .show()
    }
}
