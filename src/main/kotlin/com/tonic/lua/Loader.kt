package com.tonic.lua

import net.fabricmc.loader.api.FabricLoader
import org.luaj.vm2.*
import org.luaj.vm2.lib.*
import java.nio.file.Path
import java.io.File

object Loader {
    fun loadConfigPath(): Path {
        val cfg: File = ((FabricLoader.getInstance().configDir.resolve("tonic"))).toFile()
        cfg.mkdir()
        return FabricLoader.getInstance().configDir.resolve("tonic")
    }



}