package dev.inmo.plagubot

import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.Koin
import org.koin.core.scope.Scope

val Scope.database: Database
    get() = get()

val Koin.database: Database
    get() = get()
