package com.example.p2p_lending

data class Investment(
    val id: String = "",
    val amount: Double = 0.0,
    val profitToBeEarned: Double = 0.0,
    val interestRate: Double = 0.0,
    val timestamp: Long = 0,
    val claimed: Boolean = false
)