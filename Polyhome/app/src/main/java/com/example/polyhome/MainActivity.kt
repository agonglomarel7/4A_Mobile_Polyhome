package com.example.polyhome

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Pair


class MainActivity : AppCompatActivity() {

    private lateinit var topAnim: Animation
    private lateinit var bottomAnim: Animation
    private lateinit var image: ImageView
    private lateinit var bigTitle: TextView
    private lateinit var slogan: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize views after setContentView
        image = findViewById(R.id.imageSplash)
        bigTitle = findViewById(R.id.titleSplash)
        slogan = findViewById(R.id.subtitleSplash)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Load animations
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)

        // Apply animations to views
        image.animation = topAnim
        bigTitle.animation = bottomAnim
        slogan.animation = bottomAnim

        // Navigate to Login activity after a delay
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, Login::class.java)

            // Create pairs for shared element transitions
            val pairs = arrayOf(
                Pair<View, String>(image, "logo_image"),
                Pair<View, String>(bigTitle, "logo_text"),
                Pair<View, String>(slogan, "logo_desc")

            )

            val options = ActivityOptions.makeSceneTransitionAnimation(this, *pairs)
            startActivity(intent, options.toBundle())
        }, 3000)

    }
}