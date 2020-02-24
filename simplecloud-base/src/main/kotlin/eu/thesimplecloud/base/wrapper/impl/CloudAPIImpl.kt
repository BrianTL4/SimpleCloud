package eu.thesimplecloud.base.wrapper.impl

import eu.thesimplecloud.base.wrapper.startup.Wrapper
import eu.thesimplecloud.api.CloudAPI
import eu.thesimplecloud.api.eventapi.IEventManager
import eu.thesimplecloud.api.player.ICloudPlayerManager
import eu.thesimplecloud.api.screen.ICommandExecuteManager
import eu.thesimplecloud.api.service.ICloudServiceManager
import eu.thesimplecloud.api.servicegroup.ICloudServiceGroupManager
import eu.thesimplecloud.api.syncobject.ISynchronizedObjectManager
import eu.thesimplecloud.client.impl.SynchronizedObjectManagerImpl

class CloudAPIImpl : CloudAPI() {

    private val cloudServiceGroupManager = CloudServiceGroupManagerImpl()
    private val cloudServiceManager = CloudServiceManagerImpl()
    private val commandExecuteManager = CommandExecuteManagerImpl()
    private val cloudPlayerManager = CloudPlayerManagerImpl()
    private val eventManager = EventManagerImpl()
    private val synchronizedObjectManager = SynchronizedObjectManagerImpl(Wrapper.instance.communicationClient)

    override fun getCloudServiceGroupManager(): ICloudServiceGroupManager = this.cloudServiceGroupManager

    override fun getCloudServiceManager(): ICloudServiceManager = this.cloudServiceManager

    override fun getCloudPlayerManager(): ICloudPlayerManager = this.cloudPlayerManager

    override fun getEventManager(): IEventManager = this.eventManager

    override fun getCommandExecuteManager(): ICommandExecuteManager = this.commandExecuteManager

    override fun getSynchronizedObjectManager(): ISynchronizedObjectManager = this.synchronizedObjectManager

    override fun getThisSidesName(): String = Wrapper.instance.getThisWrapper().getName()
}