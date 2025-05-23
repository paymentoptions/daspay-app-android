package com.paymentoptions.pos.utils

import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections

fun getDeviceIpAddress(): String {
    var ip = "unknown"

    try {
        val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
        for (intf in interfaces) {
            val addrs = Collections.list(intf.inetAddresses)
            for (addr in addrs) {
                if (!addr.isLoopbackAddress && addr is Inet4Address) {
                    ip = addr.hostAddress.toString()
                }
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
    return ip
}