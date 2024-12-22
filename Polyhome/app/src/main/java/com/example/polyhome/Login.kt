package com.example.polyhome

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.androidtp2.LoginData

class Login : AppCompatActivity() {
    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Assurez-vous que le fichier XML est chargé avant de référencer les vues
        setContentView(R.layout.activity_login)

        // Initialisation des vues après setContentView
        usernameField = findViewById(R.id.lblUserName)
        passwordField = findViewById(R.id.lblUSerPassword)
        loginButton = findViewById(R.id.btnRegister)

        // Paramétrage de l'écran plein
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        enableEdgeToEdge()

        // Gestion du clic sur le bouton de connexion
        loginButton.setOnClickListener {
            login()
        }
    }

    // Gestion du clic sur le bouton d'inscription
    fun registerNewAccount(view: View) {
        val intent = Intent(this, Register::class.java)
        startActivity(intent)
    }

    // Méthode de connexion
    private fun login() {
        runOnUiThread {

            val username = usernameField.text.toString()
            val password = passwordField.text.toString()

            if (username.isNotBlank() && password.isNotBlank()) {
                Log.d("LoginActivity", "Username: $username, Password: $password") // Debug
                val loginData = LoginData(username, password)

                Api().post<LoginData, Map<String, String>>(
                    "https://polyhome.lesmoulinsdudev.com/api/users/auth",
                        loginData,
                        onSuccess = { responseCode: Int, tokenData: Map<String, String>? ->
                            if (responseCode == 200 && tokenData != null) {
                                loginSuccess(responseCode, tokenData["token"])
                            } else {
                                runOnUiThread {
                                    Toast.makeText(
                                        this,
                                        "Erreur de connexion. Vérifiez vos informations.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        securityToken = null
                    )
                } else {
                    Toast.makeText(this, "Veuillez remplir tous les champs.", Toast.LENGTH_SHORT).show()
                }
        }
    }


    // Gestion du succès de la connexion
    private fun loginSuccess(responseCode: Int, token: String?) {
        runOnUiThread {
            if (responseCode == 200 && token != null) {
                val username = usernameField.text.toString()
                // Stockage du token dans SharedPreferences
                val sharedPreferences = getSharedPreferences("PolyKeyPrefs", MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putString("Key_Token", token)
                    apply()
                }

                // Redirection vers le Dashboard
                val intent = Intent(this, home::class.java)
                intent.putExtra("Key_UserName", username) // Passe le nom d'utilisateur
                startActivity(intent)
                finish()
            }
        }
    }
}
