package com.example.polyhome

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Pair
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var topAnim: Animation
    private lateinit var bottomAnim: Animation
    private lateinit var image: ImageView
    private lateinit var bigTitle: TextView
    private lateinit var slogan: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)

        image = findViewById(R.id.imageSplash)
        bigTitle = findViewById(R.id.titleSplash)
        slogan = findViewById(R.id.subtitleSplash)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Chargement des animations
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)

        // Appliquer les animations
        image.animation = topAnim
        bigTitle.animation = bottomAnim
        slogan.animation = bottomAnim

        // Redirection vers Login après 5 secondes
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, Login::class.java)

            // Création des paires pour les transitions partagées
            val pairs = arrayOf(
                Pair<View, String>(image, "logo_image"),
                Pair<View, String>(bigTitle, "logo_text"),
                Pair<View, String>(slogan, "logo_desc")
            )

            // Lancer l'activité avec les transitions
            val options = ActivityOptions.makeSceneTransitionAnimation(this, *pairs)
            startActivity(intent, options.toBundle())

            // Terminer cette activité pour éviter le retour
            finish()
        }, 5000)
    }
}
