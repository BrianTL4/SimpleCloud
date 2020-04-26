package eu.thesimplecloud.plugin.startup

import eu.thesimplecloud.api.CloudAPI
import eu.thesimplecloud.api.client.CloudClientType
import eu.thesimplecloud.api.external.ICloudModule
import eu.thesimplecloud.api.network.packets.service.PacketIOUpdateCloudService
import eu.thesimplecloud.api.service.ICloudService
import eu.thesimplecloud.api.service.ServiceState
import eu.thesimplecloud.client.packets.PacketOutCloudClientLogin
import eu.thesimplecloud.clientserverapi.client.INettyClient
import eu.thesimplecloud.clientserverapi.client.NettyClient
import eu.thesimplecloud.clientserverapi.lib.json.JsonData
import eu.thesimplecloud.plugin.ICloudServicePlugin
import eu.thesimplecloud.plugin.impl.CloudAPIImpl
import java.io.File
import kotlin.concurrent.thread


class CloudPlugin(val cloudServicePlugin: ICloudServicePlugin) : ICloudModule {

    companion object {
        @JvmStatic
        lateinit var instance: CloudPlugin
            private set
    }

    @Volatile
    private var thisService: ICloudService? = null
    private var updateState = true
    lateinit var communicationClient: INettyClient
        private set
    lateinit var thisServiceName: String
        private set
    private var nettyThread: Thread

    init {
        println("<---------- Starting SimpleCloud-Plugin ---------->")
        instance = this
        if (!loadConfig())
            cloudServicePlugin.shutdown()
        println("<---------- Service-Name: $thisServiceName ---------->")
        CloudAPIImpl()

        this.communicationClient.addPacketsByPackage("eu.thesimplecloud.plugin.network.packets")
        this.communicationClient.addPacketsByPackage("eu.thesimplecloud.client.packets")
        this.communicationClient.addPacketsByPackage("eu.thesimplecloud.api.network.packets")
        this.communicationClient.setPacketSearchClassLoader(this::class.java.classLoader)

        nettyThread = thread(true, isDaemon = false, contextClassLoader = this::class.java.classLoader) {
            println("<------Starting cloud client----------->")
            this.communicationClient.start().then {
                println("<-------- Connection is now set up -------->")
                this.communicationClient.sendUnitQuery(PacketOutCloudClientLogin(CloudClientType.SERVICE, thisServiceName))
            }.addFailureListener { println("<-------- Failed to connect to server -------->") }.addFailureListener { throw it }
        }

        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                this.communicationClient.shutdown()
            } catch (e: Exception) {
            }

        })
    }

    /**
     * Returns whether the config was loaded successful
     */
    fun loadConfig(): Boolean {
        val jsonData = JsonData.fromJsonFile(File("SIMPLE-CLOUD.json")) ?: return false
        thisServiceName = jsonData.getString("serviceName") ?: return false
        val host = jsonData.getString("managerHost") ?: return false
        val port = jsonData.getInt("managerPort") ?: return false
        this.communicationClient = NettyClient(host, port, ConnectionHandlerImpl())
        return true
    }

    @Synchronized
    fun thisService(): ICloudService {
        if (this.thisService == null) this.thisService = CloudAPI.instance.getCloudServiceManager().getCloudServiceByName(thisServiceName)
        while (this.thisService == null || !this.thisService!!.isAuthenticated()) {
            Thread.sleep(10)
            if (this.thisService == null)
                this.thisService = CloudAPI.instance.getCloudServiceManager().getCloudServiceByName(thisServiceName)
        }
        return this.thisService!!
    }

    override fun onEnable() {
        if (this.updateState && thisService().getState() == ServiceState.STARTING) {
            thisService().setState(ServiceState.VISIBLE)
            updateThisService()
        }
    }

    override fun onDisable() {
    }

    @Synchronized
    fun updateThisService() {
        this.communicationClient.sendUnitQuery(PacketIOUpdateCloudService(thisService()))
    }

    /**
     * Prevents the service from updating its state by itself.
     */
    @Synchronized
    fun disableStateUpdating() {
        this.updateState = false
    }

    fun getGroupName(): String {
        val array = this.thisServiceName.split("-".toRegex())
        array.dropLast(1)
        return array.joinToString("-")
    }


}