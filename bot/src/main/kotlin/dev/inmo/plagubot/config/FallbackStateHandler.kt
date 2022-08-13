package dev.inmo.plagubot.config

typealias FallbackStateHandler<T> = suspend (T, Throwable) -> T?
