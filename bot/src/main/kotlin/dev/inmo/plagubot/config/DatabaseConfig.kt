package dev.inmo.plagubot.config

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
    val waitForConnection: Boolean = true
) {
    @Transient
    val database: Database by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        while (true) {
            return@lazy try {
                Database.connect(
                    url,
                    driver,
                    username,
                    password
                ).also {
                    it.transactionManager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE // Or Connection.TRANSACTION_READ_UNCOMMITTED
                }
            } catch (e: Throwable) {
                if (waitForConnection) {
                    Thread.sleep(1000L)
                    continue
                } else {
                    throw e
                }
            }
        }
        error("Unable to get database by some reason")
    }
}
