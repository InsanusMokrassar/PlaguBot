package dev.inmo.plagubot

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import kotlin.reflect.KClass

val Scope.database: Database
    get() = get()

val Koin.database: Database
    get() = get()

/**
 * Using [single] to register `T` with serializer [configSerializer]
 *
 * @param optional If passed, absence of [field] in config will lead to null config value in koin. If passed as false
 * (default), absence of [field] in config will lead to an error
 */
inline fun <reified T> Module.registerConfig(configSerializer: KSerializer<T>, field: String?, optional: Boolean = false) {
    single {
        val fieldValue = get<JsonObject>().let {
            if (field == null) {
                it
            } else {
                it[field] ?: if (optional) return@single null else error("Unable to take field $field from config")
            }
        }
        get<Json>().decodeFromJsonElement(configSerializer, fieldValue)
    }
}

/**
 * Using [single] to register config with getting of [serializer] from [kClass]
 *
 * @param optional If passed, absence of [field] in config will lead to null config value in koin. If passed as false
 * (default), absence of [field] in config will lead to an error
 */
@OptIn(InternalSerializationApi::class)
inline fun <reified T : Any> Module.registerConfig(kClass: KClass<T>, field: String?, optional: Boolean = false) = registerConfig(kClass.serializer(), field, optional)

/**
 * Using [single] to register config with getting of [serializer] from [kClass]
 *
 * @param optional If passed, absence of [field] in config will lead to null config value in koin. If passed as false
 * (default), absence of [field] in config will lead to an error
 */
inline fun <reified T : Any> Module.registerConfig(field: String?, optional: Boolean = false) = registerConfig(T::class, field, optional)

inline fun <reified T : Any> Scope.config() = get<T>()
inline fun <reified T : Any> Koin.config() = get<T>()
inline fun <reified T : Any> Scope.configOrNull() = getOrNull<T>()
inline fun <reified T : Any> Koin.configOrNull() = getOrNull<T>()
