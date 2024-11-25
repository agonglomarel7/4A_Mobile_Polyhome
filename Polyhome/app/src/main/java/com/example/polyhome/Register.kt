package com.example.polyhome

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        btnRegister.setOnClickListener {
            register()
        }
    }


    public  fun goToLogin(view: View){
        finish()
    }

    public  fun register(){

        val lblName = findViewById<EditText>(R.id.txtRegisterName)
        val lblPassword = findViewById<EditText>(R.id.txtRegisterPassword)


        val username = lblName.text.toString()
        val password = lblPassword.text.toString()

        if (username.isNotEmpty() && password.isNotEmpty()) {
            Log.d("RegisterActivity", "Les champs ne sont pas vides. Tentative d'inscription...")  // Log pour vérifier que les champs sont remplis

            val registerData = RegisterData(username, password)


            Api().post<RegisterData>(
                "https://polyhome.lesmoulinsdudev.com/api/users/register",
                registerData,
                ::registerSuccess
            )
        }else{
            Toast.makeText(this, "Veuillez remplir tous les champs.", Toast.LENGTH_SHORT).show()
        }
    }

    public fun registerSuccess(responseCode:Int) {
        if (responseCode == 200){

            Toast.makeText(this, "Inscription réussie!", Toast.LENGTH_SHORT).show()
            /*
            startActivity(intentTologinActivity);

             */
            finish()


        }else {
            Toast.makeText(this, "Inscription échouée", Toast.LENGTH_SHORT).show()
        }
    }
}