package com.tonic.lua

import com.akuleshov7.ktoml.Toml
import com.tonic.Tonic
import kotlinx.serialization.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream
import kotlin.streams.toList

object ModuleLoader {
    fun getDirs() {
        val configPath = Loader.loadConfigPath()
        Tonic.logger.info("Config path: $configPath")
        val dirs = Files.walk(Paths.get(configPath.toString()), 1)
            .filter { Files.isDirectory(it) }
            .toList()

        dirs.forEach {
            val pwd = Paths.get(it.toString(), "metadata.toml")
            if (Files.exists(pwd)) {
                val conf = Toml.decodeFromString<Module>(Files.readAllBytes(Paths.get(pwd.toString())).toString())
                Tonic.logger.info(conf.toString())
            } else {
                Tonic.logger.error("metadata.toml not found in directory: ${it.toString()}")
            }
        }
    }
}

class Module {
    @Serializable
    data class Metadata(
        val author: String,
        val description: String,
        val version: String,
        val dependencies: List<String>?,
        val entry: String
    )
}