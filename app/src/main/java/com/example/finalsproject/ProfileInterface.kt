package com.example.finalsproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton

class ProfileInterface : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_interface)

        val btnDOTs = findViewById<ImageButton>(R.id.imgbtnDOTs)
        val profileListDialog = ProfileListDialog()
        btnDOTs.setOnClickListener {
            profileListDialog.show(supportFragmentManager, "ProfileListDialog")
        }

        val createProfile = findViewById<ImageButton>(R.id.imgbtnNEW)
        createProfile.setOnClickListener {
            val intentCreate = Intent(this, CreateProfile::class.java)
            startActivity(intentCreate)
        }
    }
}