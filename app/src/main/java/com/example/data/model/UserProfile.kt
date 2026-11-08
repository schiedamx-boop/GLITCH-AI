package com.example.data.model

data class UserProfile(
    val username: String = "Gebruiker",
    val email: String = "",
    val isLoggedIn: Boolean = false,
    val totalInteractions: Int = 0,
    val securityLevel: String = "Neuraal Niveau 2"
)
