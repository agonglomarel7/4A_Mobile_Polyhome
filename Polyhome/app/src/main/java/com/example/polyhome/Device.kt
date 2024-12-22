package com.example.polyhome

data class Device(
    val id: String,
    val type: String,
    val availableCommands: List<String>
)