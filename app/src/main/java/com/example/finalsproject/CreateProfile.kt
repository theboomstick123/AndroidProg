package com.example.finalsproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CreateProfile : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var registerButton: Button
    val dbReference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference("users")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_profile)

        // Initialize views
        usernameEditText = findViewById(R.id.editTextUsername)
        registerButton = findViewById(R.id.buttonRegister)

        // Set click listener for the register button
        registerButton.setOnClickListener {
            // Get the entered username
            val username = usernameEditText.text.toString()

            // Create a new user entry in the database
            registerUser(username)
        }
    }

    //insert new user to database function
    private fun registerUser(username: String) {
        // Check if the username is not empty
        if (username.isNotEmpty()) {
            // Generate a unique identifier for the user
            val userId = dbReference.push().key

            // Create a User object
            val user = Profiles(username, 100.0, 0, 0) // Initial win rate set to 100%, initial wins and losses set to 0

            // Store user data in the Realtime Database
            if (userId != null) {
                dbReference.child(userId).setValue(user)

                // Finish the registration activity or navigate to the next screen
                finish()
            }
        } else {
            // Handle the case where the username is empty
            // show an error message to the user
            // prevent them from proceeding without entering a username
        }
    }
}