package dev.inmo.plagubot.config

import dev.inmo.kslog.common.e
import dev.inmo.kslog.common.logger
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transactionManager
import org.koin.core.scope.Scope
import org.sqlite.JDBC
import java.lang.Exception
import java.sql.Connection

inline val Scope.database: Database?
    get() = getOrNull<Database>()

@Serializable
data class DatabaseConfig(
    val url: String = "jdbc:sqlite:file:test?mode=memory&cache=shared",
    val driver: String = JDBC::class.qualifiedName!!,
    val username: String = "",
    val password: String = "",
    val reconnectOptions: DBConnectOptions? = DBConnectOptions()
) {
    @Transient
    val database: Database = (0 until (reconnectOptions ?.attempts ?: 1)).firstNotNullOfOrNull {
        runCatching {
            Database.connect(
                url,
                driver,
                username,
                password
            ).also {
                it.transactionManager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE // Or Connection.TRANSACTION_READ_UNCOMMITTED
            }
        }.onFailure {
            logger.e(it)
            Thread.sleep(reconnectOptions ?.delay ?: return@onFailure)
        }.getOrNull()
    } ?: error("Unable to create database")
}
