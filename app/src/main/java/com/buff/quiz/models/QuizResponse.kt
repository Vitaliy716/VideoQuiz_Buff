package com.buff.quiz.models

data class QuizResponse (
    val id: Int,
    val client_id: Int,
    val stream_id: Int,
    val time_to_show: Int,
    val priority: Int,
    val created_at: String,
    val author: Author,
    val question: Question,
    val answers: List<Answer>,
    val language: String
)