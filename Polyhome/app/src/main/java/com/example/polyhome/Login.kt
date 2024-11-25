package com.example.polyhome

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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

    private lateinit var callsignUp:Button;

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        usernameField = findViewById(R.id.lblusernameLogin)
        passwordField = findViewById(R.id.lblPasswordLogin)
        loginButton = findViewById(R.id.btnLogin)

        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)



        loginButton.setOnClickListener {
            login()
        }

    }


    public fun registerNewAccount(view: View) {
        val intent = Intent(this, Register::class.java)
        startActivity(intent)
    }

    public fun login() {
        val username = usernameField.text.toString()
        val password = passwordField.text.toString()

        if (username.isNotBlank() && password.isNotBlank()) {
            val loginData = LoginData(username, password)

            Api().post(
                "https://polyhome.lesmoulinsdudev.com/api/users/auth",
                loginData,
                onSuccess = { responseCode, token: String? ->
                    if (responseCode == 200 && token != null) {
                        loginSuccess(responseCode, token)
                    } else {
                        Toast.makeText(
                            this,
                            "Erreur de connexion. Vérifiez vos informations.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                securityToken = null
            )
        }else{
            Toast.makeText(this, "Veuillez remplir tous les champs.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginSuccess(responseCode: Int, token: String?) {
        if (responseCode == 200 && token != null) {
            //val intent = Intent(this, Dashboard::class.java)
            intent.putExtra("TOKEN", token)
            startActivity(intent)
        }
    }


}