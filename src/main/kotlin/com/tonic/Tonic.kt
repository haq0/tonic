package com.tonic

import com.tonic.lua.Loader
import com.tonic.lua.ModuleLoader
import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object Tonic : ModInitializer {

    val logger = LoggerFactory.getLogger("tonic")

	override fun onInitialize() {
		logger.info("Hello Fabric world!")
		var loader = Loader
		Loader.startModules()
	}
}