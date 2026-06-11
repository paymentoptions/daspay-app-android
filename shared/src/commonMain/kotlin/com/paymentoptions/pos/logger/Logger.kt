package com.paymentoptions.pos.logger

expect object AppLogger {
    fun debug(vararg objects: Any)
    fun info(vararg objects: Any)
    fun warn(vararg objects: Any)
    fun error(vararg objects: Any)
    
    fun d(tag: String, vararg arguments: String)
    fun i(tag: String, vararg arguments: String)
    fun e(tag: String, vararg arguments: String)
}
