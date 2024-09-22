package dev.inmo.plagubot

import kotlinx.serialization.InternalSerializationApi

/**
 * This method by default expects one argument in [args] field: path to config
 */
@InternalSerializationApi
suspend fun main(args: Array<String>) {
    PlaguBot.start(args).join()
}
