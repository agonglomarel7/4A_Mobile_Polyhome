package com.example.polyhome

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.androidtp2.RegisterData

class Register : AppCompatActivity() {
    private lateinit var lblName: EditText
    private lateinit var lblPassword: EditText
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        // Gestion des insets système
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialisation des vues
        lblName = findViewById(R.id.txtRegisterName)
        lblPassword = findViewById(R.id.txtRegisterPassword)
        btnRegister = findViewById(R.id.btnLogin)

        // Gestion du clic sur le bouton d'inscription
        btnRegister.setOnClickListener {
            register()
        }
    }

    // Méthode appelée lors d'un clic pour retourner à la connexion
    fun goToLogin(view: View) {
        finish() // Ferme l'activité actuelle et retourne à l'activité précédente (Login)
    }

    // Méthode pour effectuer l'inscription
    private fun register() {
        val username = lblName.text.toString()
        val password = lblPassword.text.toString()

        if (username.isNotEmpty() && password.isNotEmpty()) {
            Log.d("RegisterActivity", "Tentative d'inscription...")

            val registerData = RegisterData(username, password)

            Api().post<RegisterData>(
                "https://polyhome.lesmoulinsdudev.com/api/users/register",
                registerData,
                ::RegisterResponse
            )
        } else {
            Toast.makeText(this, "Veuillez remplir tous les champs.", Toast.LENGTH_SHORT).show()
        }
    }

// Méthode pour traiter la réponse de l'inscription
    private fun RegisterResponse(responseCode: Int) {
        runOnUiThread {
            when (responseCode) {
                200 -> {

                    finish() // Ferme l'activité actuelle
                }
                409 -> {
                    Toast.makeText(
                        this,
                        "Le login est déjà utilisé par un autre compte.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                500 -> {
                    Toast.makeText(
                        this,
                        "Erreur serveur. Veuillez réessayer plus tard.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    Toast.makeText(
                        this,
                        "Erreur inconnue. Code : $responseCode",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

}
