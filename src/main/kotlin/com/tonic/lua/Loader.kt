package com.tonic.lua

import com.tonic.Tonic
import com.tonic.lua.api.Chat
import net.fabricmc.loader.api.FabricLoader
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.JsePlatform
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

object Loader {
    fun loadConfigPath(): Path {
        val tonicDir = FabricLoader.getInstance().configDir.resolve("tonic")
        val modulesDir = tonicDir.resolve("modules")
        modulesDir.toFile().mkdirs()
        return modulesDir
    }

    fun startModules() {
        val modules = ModuleLoader.getModules()

        modules.forEach { module ->
            val moduleDir = module.path
            val entryFile = moduleDir.resolve(module.metadata.entry)

            if (!Files.exists(entryFile)) {
                Tonic.logger.error("Entry file not found: $entryFile")
                return@forEach
            }

            Tonic.logger.info("Starting module: ${module.metadata.name} at $entryFile")

            try {
                val globals = JsePlatform.standardGlobals()
                val packagePath = listOf(
                    "${moduleDir.toAbsolutePath()}/?.lua",
                    "${moduleDir.toAbsolutePath()}/?/init.lua"
                ).joinToString(";")

                globals.get("package").set("path", packagePath)
                Chat.call(LuaValue.valueOf("chat"), globals)

                val chunk = globals.load(Files.readString(entryFile), entryFile.toString())
                chunk.call()

            } catch (e: Exception) {
                Tonic.logger.error("Error loading module ${module.metadata.name}: ${e.message}")
            }
        }
    }
}