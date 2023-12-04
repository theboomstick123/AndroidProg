package com.example.finalsproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.btnGameLink)
        // Button click listener
        button.setOnClickListener {
            // Create an Intent to launch Activity2
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
    }


}