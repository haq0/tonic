package com.tonic.lua.api

import com.tonic.Tonic
import net.minecraft.client.MinecraftClient
import org.luaj.vm2.*
import org.luaj.vm2.lib.*

object Player : VarArgFunction() {
    override fun call(modname: LuaValue?, env: LuaValue?): LuaValue {
        val player = LuaTable()

        player.set("print", object : VarArgFunction() {
            override fun invoke(args: Varargs): Varargs {
                val self = args.arg1()
                val text = args.arg(2).checkjstring()
                Tonic.logger.info(text)
                return LuaValue.NIL
            }
        })

        env?.set("player", player)
        env?.get("package")?.get("loaded")?.set("player", player)
        return player
    }

}