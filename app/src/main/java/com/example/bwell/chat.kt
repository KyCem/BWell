package com.example.bwell

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatFragment : Fragment() {

    private lateinit var recyclerChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_chat, container, false)

        recyclerChat = root.findViewById(R.id.recyclerChat)
        etMessage = root.findViewById(R.id.etMessage)
        btnSend = root.findViewById(R.id.btnSend)

        recyclerChat.layoutManager = LinearLayoutManager(requireContext())

        // TODO: attach your adapter here
        // recyclerChat.adapter = YourChatAdapter()

        btnSend.setOnClickListener {
            val msg = etMessage.text.toString().trim()
            if (msg.isNotEmpty()) {
                // Placeholder action (print to log or clear input)
                println("Message sent: $msg")
                etMessage.text.clear()
            }
        }

        return root
    }
}
