package com.tonic.lua

import com.tonic.Tonic
import com.tonic.lua.api.Chat
import net.fabricmc.loader.api.FabricLoader
import org.luaj.vm2.LuaFunction
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.VarArgFunction
import org.luaj.vm2.lib.jse.JsePlatform
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files

object Loader {
    data class ModuleInstance(
        val module: ModuleLoader.LoadedModule,
        var globals: org.luaj.vm2.Globals,
        val eventHandlers: MutableList<Pair<String, LuaFunction>> = mutableListOf()
    )

    private val loadedModules = mutableMapOf<String, ModuleInstance>()

    fun loadConfigPath(): Path {
        val tonicDir = FabricLoader.getInstance().configDir.resolve("tonic")
        val modulesDir = tonicDir.resolve("modules")
        modulesDir.toFile().mkdirs()
        return modulesDir
    }

    /**
     * Loads and starts all modules, skipping those already loaded.
     */
    fun startModules() {
        val modules = ModuleLoader.getModules()

        modules.forEach { loadedModule ->
            if (loadedModules.containsKey(loadedModule.metadata.name)) {
                Tonic.logger.info("Module already loaded: ${loadedModule.metadata.name}")
                return@forEach
            }

            startModule(loadedModule)
        }
    }

    /**
     * Start (load & run) a single module
     */
    fun startModule(module: ModuleLoader.LoadedModule) {
        val moduleDir = module.path
        val entryFile = moduleDir.resolve(module.metadata.entry)

        if (!Files.exists(entryFile)) {
            Tonic.logger.error("Entry file not found: $entryFile")
            return
        }

        Tonic.logger.info("Starting module: ${module.metadata.name} at $entryFile")

        try {
            val globals = JsePlatform.standardGlobals()
            val eventBus = EventBus()

            // Override 'on' function to register event handlers AND remember them per module
            globals.set("on", object : TwoArgFunction() {
                override fun call(eventName: LuaValue, handler: LuaValue): LuaValue? {
                    if (eventName.isstring() && handler.isfunction()) {
                        val eventNameStr = eventName.tojstring()
                        val luaHandler = handler as LuaFunction

                        Chat.eventBus.register(eventNameStr, luaHandler)

                        // Save the handler in module instance for unloading
                        loadedModules[module.metadata.name]?.eventHandlers?.add(eventNameStr to luaHandler)
                            ?: run {
                                // If module not yet added, we temporarily store handlers later
                                // But we add module to loadedModules now to support that
                                loadedModules[module.metadata.name] =
                                    ModuleInstance(module, globals, mutableListOf(eventNameStr to luaHandler))
                            }
                    }
                    return NIL
                }
            })

            globals.set("fire_event", object : VarArgFunction() {
                override fun invoke(args: Varargs): Varargs? {
                    val event = args.arg(1).tojstring()
                    val rest = Array<LuaValue>(args.narg() - 1) { i -> args.arg(i + 2) }
                    for (i in 2..args.narg()) {
                        rest[i - 2] = args.arg(i)
                    }
                    Chat.eventBus.fire(event, *rest)
                    return NIL
                }
            })

            val packagePath = listOf(
                "${moduleDir.toAbsolutePath()}/?.lua",
                "${moduleDir.toAbsolutePath()}/?/init.lua"
            ).joinToString(";")

            globals.get("package").set("path", packagePath)
            Chat.call(LuaValue.valueOf("chat"), globals)

            val chunk = globals.load(Files.readString(entryFile), entryFile.toString())
            chunk.call()

            // Store module instance
            if (!loadedModules.containsKey(module.metadata.name)) {
                loadedModules[module.metadata.name] = ModuleInstance(module, globals)
            } else {
                loadedModules[module.metadata.name]?.globals = globals
            }

        } catch (e: Exception) {
            Tonic.logger.error("Error loading module ${module.metadata.name}: ${e.message}")
        }
    }

    /**
     * Unload a specific module: unregister all event handlers and discard globals.
     */
    fun unloadModule(moduleName: String) {
        val instance = loadedModules[moduleName]
        if (instance == null) {
            Tonic.logger.warn("Module not loaded: $moduleName")
            return
        }

        // Unregister event handlers from event bus
        instance.eventHandlers.forEach { (eventName, handler) ->
            Chat.eventBus.unregister(eventName, handler)
        }
        instance.eventHandlers.clear()

        // Clear globals reference for GC
        instance.globals = null!!

        loadedModules.remove(moduleName)

        Tonic.logger.info("Unloaded module: $moduleName")
    }

    /**
     * Toggle module: unload if loaded, otherwise start
     */
    fun toggleModule(moduleName: String) {
        if (loadedModules.containsKey(moduleName)) {
            unloadModule(moduleName)
        } else {
            // Find module in available modules from disk
            val module = ModuleLoader.getModules().find { it.metadata.name == moduleName }
            if (module != null) {
                startModule(module)
            } else {
                Tonic.logger.warn("Module not found: $moduleName")
            }
        }
    }

    /**
     * Unload all loaded modules
     */
    fun unloadAllModules() {
        val names = loadedModules.keys.toList()
        names.forEach { unloadModule(it) }
    }
}
