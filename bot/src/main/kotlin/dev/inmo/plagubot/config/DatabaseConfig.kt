package dev.inmo.plagubot.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transactionManager
import org.koin.core.scope.Scope
import org.sqlite.JDBC
import java.sql.Connection

inline val Scope.database: Database?
    get() = getOrNull<Database>()

@Serializable
data class DatabaseConfig(
    val url: String = "jdbc:sqlite:file:test?mode=memory&cache=shared",
    val driver: String = JDBC::class.qualifiedName!!,
    val username: String = "",
    val password: String = ""
) {
    @Transient
    val database: Database = Database.connect(
        url,
        driver,
        username,
        password
    ).also {
        it.transactionManager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE // Or Connection.TRANSACTION_READ_UNCOMMITTED
    }
}
