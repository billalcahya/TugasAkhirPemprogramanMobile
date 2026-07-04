package com.mobile.tugasrancangmoka

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        btnLogin.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()

            if (email.isEmpty()) {
                editEmail.error = "Email cannot be empty"
                editEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                editPassword.error = "Password cannot be empty"
                editPassword.requestFocus()
                return@setOnClickListener
            }

            // Simple mock authentication success
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

            // Navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
