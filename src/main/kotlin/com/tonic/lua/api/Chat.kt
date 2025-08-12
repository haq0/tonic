package com.tonic.lua.api

import com.mojang.authlib.GameProfile
import com.tonic.Tonic
import com.tonic.lua.EventBus
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.network.message.MessageType
import net.minecraft.network.message.SignedMessage
import net.minecraft.text.Text
import org.luaj.vm2.*
import org.luaj.vm2.lib.*
import java.time.Instant

object Chat : VarArgFunction() {
    val eventBus: EventBus = EventBus()
    override fun call(modname: LuaValue?, env: LuaValue?): LuaValue {

        // Initialize the events here.
        ClientReceiveMessageEvents.ALLOW_GAME.register(ChatEvents::onChatMessage);



        val chat = LuaTable()


        chat.set("print", object : VarArgFunction() {
            override fun invoke(args: Varargs): Varargs {
                val client = MinecraftClient.getInstance()
                client.inGameHud.chatHud.addMessage(Text.literal(args.arg(2).checkjstring()))
                return LuaValue.NIL
            }
        })

        chat.set("clear", object : VarArgFunction() {
            override fun invoke(args: Varargs): Varargs {
                val fmtRegex = "§.".toRegex()
                return LuaValue.valueOf(fmtRegex.replace(args.arg(2).checkjstring(), ""))
            }

        })


       // Events




        env?.set("chat", chat)
        env?.get("package")?.get("loaded")?.set("chat", chat)
        return chat
    }



}

object ChatEvents {
    // Events Register.
    fun onChatMessage(text: Text, overlay: Boolean): Boolean {
        Chat.eventBus.fire("on_chat", LuaValue.valueOf(text.string))
        return true
    }


}