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
 * @param default Will be used if [field] is absent as an alternative way of config allocation. If null passed, error
 * will be thrown
 */
inline fun <reified T> Module.registerConfig(configSerializer: KSerializer<T>, field: String?, noinline default: (Scope.(JsonObject) -> T?)? = null) {
    single {
        val fieldValue = get<JsonObject>().let {
            if (field == null) {
                it
            } else {
                it[field] ?: default ?.let { _ ->
                    return@single default(it)
                } ?: error("Unable to take field $field from config")
            }
        }
        get<Json>().decodeFromJsonElement(configSerializer, fieldValue)
    }
}

/**
 * Using [single] to register config with getting of [serializer] from [kClass]
 *
 * @param default Will be used if [field] is absent as an alternative way of config allocation. If null passed, error
 * will be thrown
 */
@OptIn(InternalSerializationApi::class)
inline fun <reified T : Any> Module.registerConfig(kClass: KClass<T>, field: String?, noinline default: (Scope.(JsonObject) -> T?)? = null) = registerConfig(kClass.serializer(), field, default)

/**
 * Using [single] to register config with getting of [serializer] from [kClass]
 *
 * @param default Will be used if [field] is absent as an alternative way of config allocation. If null passed, error
 * will be thrown
 */
inline fun <reified T : Any> Module.registerConfig(field: String?, noinline default: (Scope.(JsonObject) -> T?)? = null) = registerConfig(T::class, field, default)

inline fun <reified T : Any> Scope.config() = get<T>()
inline fun <reified T : Any> Koin.config() = get<T>()
inline fun <reified T : Any> Scope.configOrNull() = getOrNull<T>()
inline fun <reified T : Any> Koin.configOrNull() = getOrNull<T>()
