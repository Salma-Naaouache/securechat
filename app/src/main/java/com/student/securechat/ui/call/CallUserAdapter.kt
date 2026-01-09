package com.student.securechat.ui.call

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.student.securechat.R
import com.student.securechat.data.model.User

class CallUserAdapter(
    private val users: List<User>
) : RecyclerView.Adapter<CallUserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_call_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount() = users.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userName: TextView = itemView.findViewById(R.id.txtUsername_call)
        private val profileImage: ShapeableImageView = itemView.findViewById(R.id.imgUserProfile_call)
        private val voiceCallButton: ImageButton = itemView.findViewById(R.id.btn_voice_call)
        private val videoCallButton: ImageButton = itemView.findViewById(R.id.btn_video_call)

        fun bind(user: User) {
            userName.text = user.displayName
            if (user.avatarUrl.isNotEmpty()) {
                Glide.with(itemView.context).load(user.avatarUrl).into(profileImage)
            } else {
                profileImage.setImageResource(R.drawable.ic_profile)
            }

            voiceCallButton.setOnClickListener {
                Toast.makeText(itemView.context, "Appel vocal avec ${user.displayName}", Toast.LENGTH_SHORT).show()
            }

            videoCallButton.setOnClickListener {
                Toast.makeText(itemView.context, "Appel vidéo avec ${user.displayName}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}