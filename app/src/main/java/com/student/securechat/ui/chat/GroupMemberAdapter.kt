package com.student.securechat.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.student.securechat.R
import com.student.securechat.data.model.User

class GroupMemberAdapter(private val members: List<User>) : 
    RecyclerView.Adapter<GroupMemberAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_member, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(members[position])
    }

    override fun getItemCount() = members.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userName: TextView = itemView.findViewById(R.id.txtMemberName)
        private val profileImage: ShapeableImageView = itemView.findViewById(R.id.imgMemberAvatar)

        fun bind(user: User) {
            userName.text = user.displayName
            if (user.avatarUrl.isNotEmpty()) {
                Glide.with(itemView.context).load(user.avatarUrl).into(profileImage)
            } else {
                profileImage.setImageResource(R.drawable.ic_profile)
            }
        }
    }
}