package com.student.securechat.ui.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.student.securechat.R
import com.student.securechat.data.model.User

class UserSelectAdapter(
    private val users: List<User>,
    private val onUserSelected: (String, Boolean) -> Unit
) : RecyclerView.Adapter<UserSelectAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_selection, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount() = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userName: TextView = itemView.findViewById(R.id.txtUsername_selection)
        private val profileImage: ShapeableImageView = itemView.findViewById(R.id.imgUserProfile_selection)
        private val checkBox: CheckBox = itemView.findViewById(R.id.user_checkbox)

        fun bind(user: User) {
            userName.text = user.displayName
            if (user.avatarUrl.isNotEmpty()) {
                Glide.with(itemView.context).load(user.avatarUrl).into(profileImage)
            } else {
                profileImage.setImageResource(R.drawable.ic_profile)
            }

            itemView.setOnClickListener {
                checkBox.isChecked = !checkBox.isChecked
            }

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                onUserSelected(user.userId, isChecked)
            }
        }
    }
}