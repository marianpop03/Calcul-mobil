package com.example.p2p_lending

data class User(
    val username: String = "",
    val password: String = "",
    val balance: Double = 5000.0,
    val totalInvested: Double = 0.0
)