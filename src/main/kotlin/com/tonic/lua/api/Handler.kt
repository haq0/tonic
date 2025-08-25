package com.tonic.lua.api

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.Item


class Handler : ClientModInitializer {
    override fun onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: MinecraftClient? -> loop(client) })
    }

    companion object {
        private var previousScreen: Screen? = null

        fun parseInventory(screen: Screen?) {
            if (screen !is GenericContainerScreen) {
                return
            }
            val inventory = screen.screenHandler.inventory
            if (inventory.isEmpty) {
                ChatLocal.chat("Inventory is empty")
                return
            }
            val inventorySize = inventory.size()
            for (i in 0..<inventorySize) {
                val currentItem: Item? = inventory.getStack(i).item
                ChatLocal.chat("Slot $i: ${currentItem?.name}")
            }
        }

        fun loop(client: MinecraftClient?) {
            // same logic as above
            val screen: Screen? = MinecraftClient.getInstance().currentScreen
            parseInventory(screen)
            previousScreen = screen
        }
    }
}