package com.example.hasloya.data

import java.io.Serializable

data class Task(
    val id: Long = 0,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val priority: String = "Media", // Baja, Media, Alta
    val category: String = "Personal", // Personal, Trabajo, Estudio, Hogar, Otro
    val createdAt: String = "",
    val dueDate: String = ""
) : Serializable
