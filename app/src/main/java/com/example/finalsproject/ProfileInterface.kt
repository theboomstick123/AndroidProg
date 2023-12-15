package com.example.finalsproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView

class ProfileInterface : AppCompatActivity(), ProfileListDialog.ProfileSelectionListener {
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

    private var player1Selected = false
    private var player2Selected = false

    override fun onProfileSelected(profile: Profiles) {
        Log.d("ProfileSelected", "Selected profile: ${profile.userName}")


        if (!player1Selected) {
            // Set the selected profile as Player 1
            var txtprofile1 = findViewById<TextView>(R.id.txtprofile)
            var txtwinrate1 = findViewById<TextView>(R.id.txtwinrate)
            var txtwinratio1 = findViewById<TextView>(R.id.txtwinsandloss)
            txtprofile1.text = "Player name: ${profile.userName}"
            txtwinrate1.text = "Winrate :${profile.winRate}%"
            txtwinratio1.text = "Wins : ${profile.wins}  Loss : ${profile.loss}"
            player1Selected = true
        } else {
            // Set the selected profile as Player 2
            var txtprofile2 = findViewById<TextView>(R.id.txtprofile2)
            var txtwinrate2 = findViewById<TextView>(R.id.txtwinrate2)
            var txtwinratio2 = findViewById<TextView>(R.id.txtwinsandloss2)
            txtprofile2.text = "Player name: ${profile.userName}"
            txtwinrate2.text = "Winrate : ${profile.winRate}%"
            txtwinratio2.text = "Wins : ${profile.wins}  Loss : ${profile.loss}"
            player1Selected = false
        }
    }
}