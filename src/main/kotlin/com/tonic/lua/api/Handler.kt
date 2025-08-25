package com.tonic.lua.api

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.Item
import net.minecraft.item.tooltip.TooltipType


class Handler : ClientModInitializer {
    override fun onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: MinecraftClient? -> loop(client) })
    }

    companion object {
        private var previousScreen: Screen? = null

        fun parseInventory(screen: Screen?) {
            if (screen !is GenericContainerScreen) return

            val mc = MinecraftClient.getInstance()
            val player = mc.player ?: return
            val inventory = screen.screenHandler.inventory

            for (i in 0 until inventory.size()) {
                val stack = inventory.getStack(i)
                if (stack.isEmpty) continue

                val tooltip = stack.getTooltip(
                    Item.TooltipContext.DEFAULT,
                    player,
                    TooltipType.BASIC
                )

                if (tooltip.isNotEmpty()) {
                    val displayName = tooltip[0].string
                    val lastTwo = tooltip.takeLast(2).map { it.string }

                    ChatLocal.chat("Slot $i: $displayName")
                    lastTwo.forEach { line ->
                        ChatLocal.chat("   -> $line")
                    }
                }
            }
        }

        fun loop(client: MinecraftClient?) {
            val screen: Screen? = MinecraftClient.getInstance().currentScreen
            parseInventory(screen)
            previousScreen = screen
        }
    }
}