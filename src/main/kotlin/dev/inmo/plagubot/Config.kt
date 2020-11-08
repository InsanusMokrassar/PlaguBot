package dev.inmo.plagubot

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val plugins: List<@Contextual Plugin>
)
