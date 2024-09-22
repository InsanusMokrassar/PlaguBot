package dev.inmo.plagubot

import org.koin.core.Koin
import org.koin.core.definition.Definition
import org.koin.core.module.Module
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.StringQualifier
import org.koin.core.scope.Scope


val Scope.plagubot: PlaguBot
    get() = get()

val Koin.plagubot: PlaguBot
    get() = get()

private val pluginsQualifier = StringQualifier("plagubotPlugins")
internal fun Module.singlePlugins(
    createdAtStart: Boolean = false,
    definition: Definition<List<Plugin>>
) = single(pluginsQualifier, createdAtStart, definition)
val Scope.plugins: List<Plugin>
    get() = get(pluginsQualifier)

val Koin.plugins: List<Plugin>
    get() = get(pluginsQualifier)


