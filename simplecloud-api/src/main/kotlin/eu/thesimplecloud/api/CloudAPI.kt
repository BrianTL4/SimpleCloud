package eu.thesimplecloud.api

import eu.thesimplecloud.api.template.ITemplateManager
import eu.thesimplecloud.api.template.impl.DefaultTemplateManager
import eu.thesimplecloud.api.wrapper.IWrapperManager
import eu.thesimplecloud.api.wrapper.impl.DefaultWrapperManager

abstract class CloudAPI : ICloudAPI {

    private val wrapperManager: IWrapperManager = DefaultWrapperManager()
    private val templateManager: ITemplateManager = DefaultTemplateManager()

    init {
        instance = this
    }



    override fun getWrapperManager(): IWrapperManager = this.wrapperManager

    override fun getTemplateManager(): ITemplateManager = this.templateManager

    companion object {
        @JvmStatic
        lateinit var instance: ICloudAPI
            private set
    }
}