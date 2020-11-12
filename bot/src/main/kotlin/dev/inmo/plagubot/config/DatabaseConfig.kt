package dev.inmo.plagubot.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transactionManager
import org.sqlite.JDBC
import java.sql.Connection

@Serializable
data class DatabaseConfig(
    val url: String = "jdbc:sqlite:file:test?mode=memory&cache=shared",
    val driver: String = JDBC::class.qualifiedName!!,
    val username: String = "",
    val password: String = "",
    val initAutomatically: Boolean = true
) {
    @Transient
    private lateinit var _database: Database
    val database: Database
        get() = try {
            _database
        } catch (e: UninitializedPropertyAccessException) {
            Database.connect(
                url,
                driver,
                username,
                password
            ).also {
                _database = it
                it.transactionManager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE // Or Connection.TRANSACTION_READ_UNCOMMITTED
            }
        }

    init {
        if (initAutomatically) {
            database // init database
        }
    }
}
