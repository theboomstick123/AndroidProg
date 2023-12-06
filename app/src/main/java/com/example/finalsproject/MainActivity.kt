package com.example.finalsproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Button click listener

        val button = findViewById<Button>(R.id.btnGameLink)
        button.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
    }
}