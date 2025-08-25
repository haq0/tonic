package com.tonic

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.tonic.lua.Loader
import com.tonic.lua.api.ChatLocal
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
    var modules : HashMap <String, String> = HashMap<String, String>()
    val logger = LoggerFactory.getLogger("tonic")

    override fun onInitialize() {
        Loader.startModules()
        ClientCommandRegistrationCallback.EVENT.register(::registerCommands)
    }

    private fun registerCommands(
        dispatcher: CommandDispatcher<FabricClientCommandSource>,
        registryAccess: net.minecraft.command.CommandRegistryAccess
    ) {
        val bindings: HashMap<String, String> = HashMap()
        val tonicCmd = literal("tonic")
            .then(
                literal("reload").executes {
                    MinecraftClient.getInstance().inGameHud.chatHud.addMessage(Text.literal("Reloading modules..."))
                    Loader.startModules()
                    1
                }
            ).then(
                literal("bind").then(
                    argument("input", StringArgumentType.greedyString()).executes {
                        val input = StringArgumentType.getString(it, "input")

                        val splitIndex = input.indexOf(':')
                        if (splitIndex == -1) {
                            ChatLocal.chat("Invalid format! Use slot:string")
                            return@executes 0
                        }

                        val slotNum = input.substring(0, splitIndex).toIntOrNull()
                        val value = input.substring(splitIndex + 1).trim()

                        if (slotNum == null) {
                            ChatLocal.chat("Invalid slot number")
                            return@executes 0
                        }

                        val itemName = Handler.slotItemMap[slotNum]
                        if (itemName == null) {
                            ChatLocal.chat("No item found in slot $slotNum")
                            return@executes 0
                        }

                        bindings[itemName] = value
                        ChatLocal.chat("Bound '$value' to '$itemName'")
                        1
                    }
                )
            )
            .then(
                literal("dump").executes {
                    if (bindings.isEmpty()) {
                        ChatLocal.chat("No bindings")
                    } else {
                        for ((item, value) in bindings) {
                            ChatLocal.chat("$item : $value")
                        }
                    }
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
            ).then(
                literal("clear").executes {
                    bindings.clear()
                    ChatLocal.chat("All bindings cleared")
                    1
                }
            )


        dispatcher.register(tonicCmd)
    }
}