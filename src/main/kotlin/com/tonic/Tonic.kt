package com.tonic

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.tonic.lua.Loader
import com.tonic.lua.api.Handler
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import org.slf4j.LoggerFactory

object Tonic : ModInitializer {
    val loader = Loader
    val logger = LoggerFactory.getLogger("tonic")

    override fun onInitialize() {
        Loader.startModules()
        ClientCommandRegistrationCallback.EVENT.register(::registerCommands)
    }

    private fun registerCommands(
        dispatcher: CommandDispatcher<FabricClientCommandSource>,
        registryAccess: net.minecraft.command.CommandRegistryAccess
    ) {
        val tonicCmd = literal("tonic")
            .then(
                literal("reload").executes {
                    MinecraftClient.getInstance().inGameHud.chatHud.addMessage(Text.literal("Reloading modules..."))
                    Loader.startModules()
                    1
                }
            )
            .then(
                literal("set").then(
                    argument("value", IntegerArgumentType.integer(0))
                        .executes {
                            val value = IntegerArgumentType.getInteger(it, "value")
                            MinecraftClient.getInstance().inGameHud.chatHud.addMessage(Text.literal("Set value to $value"))
                            1
                        }
                )
            )
            .then(
                literal("flag").then(
                    argument("enabled", BoolArgumentType.bool())
                        .executes {
                            val enabled = BoolArgumentType.getBool(it, "enabled")
                            MinecraftClient.getInstance().inGameHud.chatHud.addMessage(Text.literal("Flag set to $enabled"))
                            1
                        }
                )
            )

        dispatcher.register(tonicCmd)
    }
}