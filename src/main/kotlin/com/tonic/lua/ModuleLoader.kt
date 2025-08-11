package com.tonic.lua

import com.akuleshov7.ktoml.Toml
import com.tonic.Tonic
import kotlinx.serialization.Serializable
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.io.path.name
import kotlinx.serialization.serializer

object ModuleLoader {
    data class LoadedModule(val path: Path, val metadata: Metadata)

    fun getModules(): List<LoadedModule> {
        val modulesRoot = Loader.loadConfigPath()
        val modules = mutableListOf<LoadedModule>()

        val moduleDirs = Files.list(modulesRoot)
            .filter { Files.isDirectory(it) }
            .collect(Collectors.toList())

        moduleDirs.forEach { dir ->
            val metadataFile = dir.resolve("metadata.toml")
            if (!Files.exists(metadataFile)) {
                Tonic.logger.warn("Skipping ${dir.name}: No metadata.toml")
                return@forEach
            }

            try {
                val module = Toml.decodeFromString(Module.serializer(), Files.readString(metadataFile))
                modules.add(LoadedModule(dir, module.metadata))
            } catch (e: Exception) {
                Tonic.logger.error("Failed to parse metadata.toml in ${dir.name}: ${e.message}")
            }
        }

        return modules
    }
}

@Serializable
data class Module(
    val metadata: Metadata
)

@Serializable
data class Metadata(
    val name: String,
    val author: String,
    val description: String,
    val version: String,
    val dependencies: List<String>? = null,
    val entry: String
)