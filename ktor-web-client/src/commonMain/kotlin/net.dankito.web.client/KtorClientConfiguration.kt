package net.dankito.web.client

import net.codinux.log.logger

object KtorClientConfiguration {

    var logUsedEngine = false

    private val log by logger()


    fun getFirstOfSupportedHttpClient(): KtorEngine? {
        val supportedEngines = Platform.preferredEngines

        return Platform.availableEngines.firstOrNull { supportedEngines.contains(it) }.also {
            if (logUsedEngine) {
                log.info { "Using HTTP Client Engine ${it?.engineName}" }
            } else {
                log.debug { "Using HTTP Client Engine ${it?.engineName}" }
            }
        }
    }

}