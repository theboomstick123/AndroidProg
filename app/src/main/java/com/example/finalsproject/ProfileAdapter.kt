package com.example.finalsproject
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ProfileAdapter(private val onItemClick: (Profiles) -> Unit) :
    ListAdapter<Profiles, ProfileAdapter.ProfileViewHolder>(ProfileDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.profile_list_item, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val profile = getItem(position)

        holder.itemView.setOnClickListener {
            onItemClick(profile)
        }

        holder.bind(profile)
    }

    class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        private val winRateTextView: TextView = itemView.findViewById(R.id.winRateTextView)
        private val winsTextView: TextView = itemView.findViewById(R.id.winsTextView)
        private val lossTextView: TextView = itemView.findViewById(R.id.lossTextView)

        fun bind(user: Profiles) {
            usernameTextView.text = user.userName
            winRateTextView.text = "Win Rate: ${user.winRate}%"
            winsTextView.text = "Wins: ${user.wins.toString()}"
            lossTextView.text = "Losses: ${user.loss.toString()}"
        }
    }

    private class ProfileDiffCallback : DiffUtil.ItemCallback<Profiles>() {
        override fun areItemsTheSame(oldItem: Profiles, newItem: Profiles): Boolean {
            return oldItem.userName == newItem.userName
        }

        override fun areContentsTheSame(oldItem: Profiles, newItem: Profiles): Boolean {
            return oldItem == newItem
        }
    }

}
