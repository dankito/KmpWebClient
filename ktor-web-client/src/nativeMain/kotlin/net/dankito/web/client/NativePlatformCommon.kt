package net.dankito.web.client

import io.ktor.client.engine.*
import io.ktor.util.*

object NativePlatformCommon {

    @OptIn(InternalAPI::class)
    val availableEngines: LinkedHashSet<KtorEngine> = engines.mapNotNull { container ->
        KtorEngine.entries.firstOrNull { name -> container.toString() == name.engineName }
    }.toCollection(LinkedHashSet())


    fun getFirstOfSupportedHttpClient(vararg supportedEngines: KtorEngine): KtorEngine? =
        availableEngines.firstOrNull { supportedEngines.contains(it) }

}