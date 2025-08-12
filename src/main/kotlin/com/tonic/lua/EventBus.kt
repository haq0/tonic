package com.tonic.lua

import org.luaj.vm2.LuaFunction
import org.luaj.vm2.LuaValue


class EventBus {
    private val handlers: MutableMap<String?, MutableList<LuaFunction>?> = HashMap<String?, MutableList<LuaFunction>?>()

    fun register(eventName: String?, func: LuaFunction?) {
        handlers.computeIfAbsent(eventName) { k: String? -> ArrayList<LuaFunction?>() as MutableList<LuaFunction>? }!!.add(func!!)
    }

    fun fire(eventName: String, vararg args: LuaValue) {
        handlers[eventName]?.forEach { handler ->
            handler.invoke(LuaValue.varargsOf(args))
        }
    }

}