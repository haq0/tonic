package com.tonic.lua.api

import com.tonic.Tonic
import org.luaj.vm2.*
import org.luaj.vm2.lib.*

object Chat : VarArgFunction() {
    override fun call(modname: LuaValue?, env: LuaValue?): LuaValue {
        val chat = LuaTable()

        chat.set("print", object : VarArgFunction() {
            override fun invoke(args: Varargs): Varargs {
                val self = args.arg1()
                val text = args.arg(2).checkjstring()
                Tonic.logger.info(text)
                return LuaValue.NIL
            }
        })

        env?.set("chat", chat)
        env?.get("package")?.get("loaded")?.set("chat", chat)
        return chat
    }

}