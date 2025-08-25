package com.tonic.lua.api

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.registry.Registries

class Handler : ClientModInitializer {
    override fun onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: MinecraftClient? ->
            loop(client)
        })
    }

    companion object {
        private var previousScreen: Screen? = null
        private var delayTicks: Int = 0

        val slotItemMap: HashMap<Int, String> = HashMap()

        fun parseInventory(screen: Screen?) {
            if (screen !is GenericContainerScreen) return

            val mc = MinecraftClient.getInstance()
            val player = mc.player ?: return
            val handler = screen.screenHandler

            slotItemMap.clear()

            for ((i, slot) in handler.slots.withIndex()) {
                val stack = slot.stack
                if (stack.isEmpty) continue

                val itemName = stack.name.string
                slotItemMap[i] = itemName

                ChatLocal.chat("=== Slot $i ===")
                ChatLocal.chat("Name: $itemName")
                ChatLocal.chat("Item ID: ${Registries.ITEM.getId(stack.item)}")
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
