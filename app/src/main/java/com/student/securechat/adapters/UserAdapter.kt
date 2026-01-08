package com.student.securechat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.student.securechat.R
import com.student.securechat.models.User

class UserAdapter(
    private var userList: List<User>,
    private val listener: OnUserClickListener
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    interface OnUserClickListener {
        fun onUserClick(user: User)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun updateUsers(users: List<User>) {
        this.userList = users
        notifyDataSetChanged()
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userName: TextView = itemView.findViewById(R.id.username)
        private val profileImage: ShapeableImageView = itemView.findViewById(R.id.profile_image)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onUserClick(userList[position])
                }
            }
        }

        fun bind(user: User) {
            userName.text = user.displayName
            if (user.avatarUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(user.avatarUrl)
                    .into(profileImage)
            } else {
                profileImage.setImageResource(R.drawable.ic_profile)
            }
        }
    }
}