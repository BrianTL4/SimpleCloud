package eu.thesimplecloud.plugin.proxy

import eu.thesimplecloud.api.CloudAPI
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.ReconnectHandler
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer

class ReconnectHandlerImpl : ReconnectHandler {
    override fun save() {
    }

    override fun getServer(player: ProxiedPlayer): ServerInfo? {
        return null
    }

    override fun setServer(player: ProxiedPlayer?) {
    }

    override fun close() {
    }
}