package dev.datlag.sekret.gradle.common

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.findByType
import org.gradle.plugin.use.PluginDependency
import kotlin.jvm.optionals.getOrNull

internal val Project.versionCatalogs: Iterable<VersionCatalog>
    get() = this.extensions.findByType<VersionCatalogsExtension>()?.filterNotNull().orEmpty()

internal val Project.sekretVersionCatalog: VersionCatalog?
    get() = versionCatalogs.firstOrNull { it.findPluginById("dev.datlag.sekret") != null }

internal val Project.hasVersionCatalogs: Boolean
    get() = versionCatalogs.toList().isNotEmpty()

internal val Project.hasSekretVersionCatalog: Boolean
    get() = sekretVersionCatalog != null

internal val VersionCatalog.sekretLibraryAlias: String?
    get() = findLibraryBy(group = "dev.datlag.sekret", name = "sekret")?.name?.ifBlank { null }

internal fun VersionCatalog.findPluginById(id: String, ignoreCase: Boolean = false): PluginDependency? {
    return this.pluginAliases.mapNotNull {
        this.findPlugin(it).getOrNull()?.orNull
    }.firstOrNull {
        it.pluginId.trim().equals(id, ignoreCase = ignoreCase)
    }
}

internal fun VersionCatalog.findLibraryBy(group: String, name: String, ignoreCase: Boolean = false): MinimalExternalModuleDependency? {
    return this.libraryAliases.mapNotNull {
        this.findLibrary(it).getOrNull()?.orNull
    }.firstOrNull {
        it.module.group.trim().equals(group, ignoreCase) && it.module.name.trim().equals(name, ignoreCase)
    }
}