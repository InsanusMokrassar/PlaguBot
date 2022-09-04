package dev.inmo.plagubot.config

import kotlinx.serialization.Serializable

@Serializable
data class DBConnectOptions(
    val attempts: Int = 3,
    val delay: Long = 1000L
)
