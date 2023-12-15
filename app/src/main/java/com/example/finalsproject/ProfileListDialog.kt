package com.example.finalsproject
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class ProfileListDialog : DialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var profileAdapter: ProfileAdapter
    private lateinit var dbReference: DatabaseReference

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_recyclerview_layout, null)

        recyclerView = view.findViewById(R.id.recyclerView)
        profileAdapter = ProfileAdapter(
            onItemClick = { profile ->
                // Handle the click event, e.g., show details
                Log.d("ProfileClick", "Clicked on profile: ${profile.userName}")
            },
            onPlayerSelect = { profile ->
                // Handle the long click event, e.g., select as Player 1 or Player 2
                Log.d("PlayerSelect", "Selected as Player: ${profile.userName}")

                // Pass the selected profile to the activity (you can use an interface or callback)
                if (activity is ProfileSelectionListener) {
                    (activity as ProfileSelectionListener).onProfileSelected(profile)
                }
            }
        )

        // Initialize Firebase
        dbReference = FirebaseDatabase.getInstance().getReference("users")

        // Attach a listener to read the data
        dbReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Handle data changes and update the adapter
                val profiles = mutableListOf<Profiles>()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(Profiles::class.java)
                    user?.let { profiles.add(it) }
                }
                profileAdapter.submitList(profiles)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })



        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = profileAdapter

        return Dialog(requireContext()).apply {
            setContentView(view)
            setTitle("Profile List")
        }
    }

    interface ProfileSelectionListener {
        fun onProfileSelected(profile: Profiles)
    }
}
