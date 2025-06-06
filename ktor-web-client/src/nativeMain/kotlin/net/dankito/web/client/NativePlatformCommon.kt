package net.dankito.web.client

import io.ktor.client.engine.*
import io.ktor.utils.io.*

object NativePlatformCommon {

    @OptIn(InternalAPI::class)
    val availableEngines: LinkedHashSet<KtorEngine> = engines.mapNotNull { container ->
        KtorEngine.entries.firstOrNull { name -> container.toString() == name.engineName }
    }.toCollection(LinkedHashSet())

}