package com.example.p2p_lending

data class ChatMessage(
    val sender: String = "",
    val message: String = "",
    val timestamp: Long = 0
)