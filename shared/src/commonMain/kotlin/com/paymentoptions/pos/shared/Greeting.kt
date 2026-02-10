package com.paymentoptions.pos.shared

class Greeting {
    private val platform = platform()

    fun greet(): String {
        return "Hello, ${platform}!"
    }
}