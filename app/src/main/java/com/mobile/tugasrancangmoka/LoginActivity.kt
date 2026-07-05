package com.mobile.tugasrancangmoka

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.mobile.tugasrancangmoka.utils.SessionManager
import com.mobile.tugasrancangmoka.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Kalau udah login, langsung ke MainActivity
        if (SessionManager(this).isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val editEmail = findViewById<TextInputEditText>(R.id.edit_email)
        val editPassword = findViewById<TextInputEditText>(R.id.edit_password)
        val btnLogin = findViewById<MaterialButton>(R.id.btn_login)

        // Setup ViewModel
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        // Observe loading - disable tombol pas lagi loading
        viewModel.isLoading.observe(this) { loading ->
            btnLogin.isEnabled = !loading
            btnLogin.text = if (loading) "Loading..." else "Login"
        }

        // Observe hasil login
        viewModel.loginResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            result.onFailure { error ->
                Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
            }
        }

        // Tombol login
        btnLogin.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()

            if (email.isEmpty()) {
                editEmail.error = "Email tidak boleh kosong"
                editEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                editPassword.error = "Password tidak boleh kosong"
                editPassword.requestFocus()
                return@setOnClickListener
            }

            // Panggil API login beneran
            viewModel.login(email, password)
        }
    }
}
