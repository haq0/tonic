package com.tonic.lua.api

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.registry.Registries
import net.minecraft.text.Text

class Handler : ClientModInitializer {
    override fun onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: MinecraftClient? ->
            loop(client)
        })
    }

    companion object {
        private var previousScreen: Screen? = null
        private var delayTicks: Int = 0

        fun parseInventory(screen: Screen?) {
            if (screen !is GenericContainerScreen) return

            val mc = MinecraftClient.getInstance()
            val player = mc.player ?: return
            val handler = screen.screenHandler

            for ((i, slot) in handler.slots.withIndex()) {
                val stack = slot.stack
                if (stack.isEmpty) continue

                val itemName = stack.name.string
                val itemId = Registries.ITEM.getId(stack.item).toString()

                ChatLocal.chat("=== Slot $i ===")
                ChatLocal.chat("Name: $itemName")
                ChatLocal.chat("Item ID: $itemId")

                val tooltip = stack.getTooltip(
                    Item.TooltipContext.DEFAULT,
                    player,
                    TooltipType.BASIC
                )

                tooltip.take(4).forEachIndexed { index, line ->
                    val rawString = line.string
                    val fullRaw = line.toString()
                    val font = line.style?.font?.toString() ?: "default"
                    val siblings = line.siblings.joinToString(", ") { it.string }

                    ChatLocal.chat("Line $index: $rawString")
                    ChatLocal.chat(" - Raw: $fullRaw")
                    ChatLocal.chat(" - Font: $font")
                    if (siblings.isNotEmpty()) {
                        ChatLocal.chat(" - Siblings: $siblings")
                    }
                }

                ChatLocal.chat("=================")
            }
        }

        fun loop(client: MinecraftClient?) {
            val screen = MinecraftClient.getInstance().currentScreen

            if (screen != null && screen !== previousScreen) {
                delayTicks = 1
            }

            if (delayTicks > 0) {
                delayTicks--
                if (delayTicks == 0) {
                    parseInventory(screen)
                }
            }

            previousScreen = screen
        }
    }
}
