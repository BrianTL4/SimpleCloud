package eu.thesimplecloud.module.proxy.service.velocity.listener

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.event.proxy.ProxyPingEvent
import com.velocitypowered.api.proxy.server.ServerPing
import eu.thesimplecloud.api.player.text.CloudText
import eu.thesimplecloud.module.proxy.extensions.mapToLowerCase
import eu.thesimplecloud.module.proxy.service.velocity.VelocityPluginMain
import eu.thesimplecloud.plugin.proxy.velocity.text.CloudTextBuilder
import eu.thesimplecloud.plugin.startup.CloudPlugin
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: Philipp.Eistrach
 * Date: 16.05.2020
 * Time: 23:48
 */
class VelocityListener(val plugin: VelocityPluginMain) {

    @Subscribe(order = PostOrder.LAST)
    fun handle(event: ServerPreConnectEvent) {
        val player = event.player

        if (!plugin.tablistStarted) {
            plugin.startTablist()
        }

        val config = plugin.proxyHandler.configHolder.obj
        val proxyConfiguration = plugin.proxyHandler.getProxyConfiguration() ?: return

        if (CloudPlugin.instance.thisService().getServiceGroup().isInMaintenance()) {
            if (!player.hasPermission(plugin.proxyHandler.JOIN_MAINTENANCE_PERMISSION) &&
                    !proxyConfiguration.whitelist.mapToLowerCase().contains(player.username.toLowerCase())) {
                player.disconnect(CloudTextBuilder().build(CloudText(config.maintenanceKickMessage)))
                event.result = ServerPreConnectEvent.ServerResult.denied()
            }

            return
        }


        val maxPlayers = CloudPlugin.instance.thisService().getMaxPlayers()

        if (plugin.proxyHandler.getOnlinePlayers() >= maxPlayers) {
            if (!player.hasPermission(plugin.proxyHandler.JOIN_FULL_PERMISSION) &&
                    !proxyConfiguration.whitelist.mapToLowerCase().contains(player.username.toLowerCase())) {
                player.disconnect(CloudTextBuilder().build(CloudText(config.fullProxyKickMessage)))
                event.result = ServerPreConnectEvent.ServerResult.denied()
            }
        }

        val tablistConfiguration = plugin.proxyHandler.getTablistConfiguration() ?: return

        val headerAndFooter = plugin.getCurrentHeaderAndFooter(tablistConfiguration)
        plugin.sendHeaderAndFooter(player, headerAndFooter.first, headerAndFooter.second)
    }

    @Subscribe
    fun handle(event: ProxyPingEvent) {
        val proxyConfiguration = plugin.proxyHandler.getProxyConfiguration() ?: return
        val motdConfiguration = if (CloudPlugin.instance.thisService().getServiceGroup().isInMaintenance())
            proxyConfiguration.maintenanceMotds else proxyConfiguration.motds

        val line1 = motdConfiguration.firstLines.random()
        val line2 = motdConfiguration.secondLines.random()

        val motd = CloudTextBuilder()
                .build(CloudText(plugin.proxyHandler.replaceString(line1 + "\n" + line2)))

        val ping = event.ping
        var protocol: ServerPing.Version = ping.version
        var players: ServerPing.Players? = if (ping.players.isPresent) ping.players.get() else null
        val favicon = if (ping.favicon.isPresent) ping.favicon.get() else null
        val modinfo = if (ping.modinfo.isPresent) ping.modinfo.get() else null

        val playerInfo = motdConfiguration.playerInfo
        var onlinePlayers = plugin.proxyHandler.getOnlinePlayers()

        val versionName = motdConfiguration.versionName
        if (versionName != null && versionName.isNotEmpty()) {
            protocol = ServerPing.Version(-1, plugin.proxyHandler.replaceString(versionName))
        }

        val maxPlayers = CloudPlugin.instance.thisService().getMaxPlayers()

        if (playerInfo != null && playerInfo.isNotEmpty()) {
            val playerInfoString = plugin.proxyHandler.replaceString(playerInfo.joinToString("\n"))
            val sample = listOf(ServerPing.SamplePlayer(playerInfoString, UUID.randomUUID()))
            players = ServerPing.Players(onlinePlayers, maxPlayers, sample)
        }

        event.ping = ServerPing(protocol, players, motd, favicon, modinfo)
    }

}