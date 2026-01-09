package com.student.securechat.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.student.securechat.R
import com.student.securechat.data.model.Message
import com.student.securechat.security.CryptoManager

class ChatAdapter(
    private val currentUserId: String,
    private val cryptoManager: CryptoManager
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val messages = mutableListOf<Message>()

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    fun setMessages(messages: List<Message>) {
        this.messages.clear()
        this.messages.addAll(messages)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is SentMessageViewHolder) {
            holder.bind(message)
        } else if (holder is ReceivedMessageViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.txtSentMessage)

        fun bind(message: Message) {
            val decryptedContent = decryptMessage(message)
            messageText.text = decryptedContent
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.txtReceivedMessage)

        fun bind(message: Message) {
            val decryptedContent = decryptMessage(message)
            messageText.text = decryptedContent
        }
    }

    private fun decryptMessage(message: Message): String {
        return try {
            // ✅ CORRIGÉ: Logique de déchiffrement rétrocompatible
            val encryptedAesKeyString = if (message.encryptedKeys.isNotEmpty()) {
                message.encryptedKeys[currentUserId]
            } else {
                message.encryptedAesKey // Fallback pour les anciens messages
            } ?: throw SecurityException("No encrypted key found for current user")

            val encryptedAesKeyBytes = android.util.Base64.decode(encryptedAesKeyString, android.util.Base64.DEFAULT)
            val decryptedAesKeyBytes = cryptoManager.decryptWithRsa(encryptedAesKeyBytes)
            val secretKey = cryptoManager.byteArrayToSecretKey(decryptedAesKeyBytes)

            val iv = android.util.Base64.decode(message.iv, android.util.Base64.DEFAULT)
            val content = android.util.Base64.decode(message.content, android.util.Base64.DEFAULT)

            cryptoManager.decryptWithAes(content, secretKey, iv)
        } catch (e: Exception) {
            "Error decrypting message: ${e.message}"
        }
    }
}