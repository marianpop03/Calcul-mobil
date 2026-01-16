package com.example.p2p_lending

data class Loan(
    var id: String = "",
    val borrowerName: String = "",
    val amount: Double = 0.0,
    val fundedAmount: Double = 0.0,
    val interestRate: Double = 0.0,
    val purpose: String = ""
)