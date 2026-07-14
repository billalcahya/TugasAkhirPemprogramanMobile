package com.mobile.tugasrancangmoka.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mobile.tugasrancangmoka.activity.MainActivity
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.mobile.tugasrancangmoka.databinding.ActivityLoginBinding
import com.mobile.tugasrancangmoka.api.ApiClient
import com.mobile.tugasrancangmoka.model.LoginRequest
import com.mobile.tugasrancangmoka.repository.AuthRepo
import com.mobile.tugasrancangmoka.utils.SessionManager
import com.mobile.tugasrancangmoka.viewmodel.AuthVM
import com.mobile.tugasrancangmoka.viewmodel.LoginResult
import com.mobile.tugasrancangmoka.viewmodel.ViewModelFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var binding:ActivityLoginBinding
    private lateinit var viewModel: AuthVM
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Inisialisasi tingkat komponen data
        val apiService = ApiClient.getApiService(this)
        val repository = AuthRepo(apiService)

        // Menerapkan ViewModel dengan Factory pattern
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AuthVM::class.java]

        setupActionListeners()
        observeLoginState()
        ViewCompat.setOnApplyWindowInsetsListener(binding.loginRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupActionListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()

            // Validasi client-side sederhana sebelum hit API
            if (email.isEmpty()) {
                binding.editEmail.error = "Email cannot be empty"
                binding.editEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.editPassword.error = "Password cannot be empty"
                binding.editPassword.requestFocus()
                return@setOnClickListener
            }

            val request = LoginRequest(email, password)
            viewModel.loginUser(request)
        }
    }

    private fun observeLoginState() {
        viewModel.loginState.observe(this) { result ->
            when (result) {
                is LoginResult.Loading -> {
                    // Tampilkan indikator loading (bisa diatur lewat XML ProgressBar)
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                }
                is LoginResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true

                    // Ambil data payload dari response body
                    val loginData = result.response.data
                    if (loginData != null) {
                        // Persist ke SharedPreferences melalui SessionManager
                        sessionManager.saveSession(
                            token = loginData.token,
                            name = loginData.user.name,
                            role = loginData.user.role
                        )

                        Toast.makeText(this, "Selamat datang, ${loginData.user.name}!", Toast.LENGTH_SHORT).show()

                        // Navigasi ke halaman utama Dashboard
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
                is LoginResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    // Sesuai dengan spesifikasi SDD, tampilkan pesan error via Snackbar
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}
