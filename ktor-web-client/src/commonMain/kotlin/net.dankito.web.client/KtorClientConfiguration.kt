package net.dankito.web.client

import net.codinux.log.logger

object KtorClientConfiguration {

    private val log by logger()


    fun getFirstOfSupportedHttpClient(): KtorEngine? {
        val supportedEngines = Platform.preferredEngines

        return Platform.availableEngines.firstOrNull { supportedEngines.contains(it) }.also {
            log.debug { "Using HTTP Client Engine ${it?.engineName}" }
        }
    }

}