package dev.inmo.plagubot.config

typealias StateHandlingErrorHandler<T> = suspend (T, Throwable) -> T?
