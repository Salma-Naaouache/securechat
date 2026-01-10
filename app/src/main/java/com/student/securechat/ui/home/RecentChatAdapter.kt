package com.student.securechat.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.student.securechat.R
import com.student.securechat.data.model.ChatSummary

class RecentChatAdapter(
    private var chatList: List<ChatSummary>,
    private val listener: OnChatClickListener,
    private val currentUserId: String
) : RecyclerView.Adapter<RecentChatAdapter.ChatViewHolder>() {

    interface OnChatClickListener {
        fun onChatClick(chatSummary: ChatSummary)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recent_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        holder.bind(chat)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    fun updateChats(chats: List<ChatSummary>) {
        this.chatList = chats
        notifyDataSetChanged()
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userName: TextView = itemView.findViewById(R.id.txtUsername)
        private val lastMessage: TextView = itemView.findViewById(R.id.txtLastMessage)
        private val profileImage: ShapeableImageView = itemView.findViewById(R.id.imgUserProfile)
        private val unreadCountBadge: TextView = itemView.findViewById(R.id.txtUnreadCount)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onChatClick(chatList[position])
                }
            }
        }

        fun bind(chat: ChatSummary) {
            val displayName = if (chat.isGroup) chat.groupName else chat.otherParticipantName
            userName.text = displayName ?: "Conversation"

            // ✅ CORRIGÉ: Afficher un placeholder au lieu du texte crypté
            val lastMessagePreview = chat.lastMessage
            var displayText = ""
            if (!lastMessagePreview.isNullOrEmpty()) {
                displayText = if (chat.lastMessageSenderId == currentUserId) {
                    "Vous: Message"
                } else {
                    "Message"
                }
            }
            lastMessage.text = displayText

            val imageUrl = if (chat.isGroup) "" else chat.otherParticipantAvatar
            if (imageUrl?.isNotEmpty() == true) {
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .into(profileImage)
            } else {
                profileImage.setImageResource(R.drawable.ic_profile)
            }

            val count = chat.unreadCount[currentUserId] ?: 0
            if (count > 0) {
                unreadCountBadge.visibility = View.VISIBLE
                unreadCountBadge.text = count.toString()
            } else {
                unreadCountBadge.visibility = View.GONE
            }
        }
    }
}