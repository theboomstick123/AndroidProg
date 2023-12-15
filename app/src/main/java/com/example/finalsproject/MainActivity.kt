package com.example.finalsproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Button click listener

        val button = findViewById<ImageButton>(R.id.startBTN)
        button.setOnClickListener {
            val intent = Intent(this, ProfileInterface::class.java)
            startActivity(intent)
        }


        val startButton = findViewById<ImageButton>(R.id.startBTN)
        startButton.setOnClickListener{
            val intentStart = Intent(this, ProfileInterface::class.java)
            startActivity(intentStart)
        }
    }
}