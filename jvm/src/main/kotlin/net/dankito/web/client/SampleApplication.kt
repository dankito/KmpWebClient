package net.dankito.web.client

import kotlinx.coroutines.runBlocking
import net.codinux.log.logger
import net.codinux.util.Stopwatch
import net.dankito.web.client.KtorWebClient
import net.dankito.web.client.WebClientResponse
import net.dankito.web.client.getAsync

class SampleApplication {

    private val log by logger()

    fun runSample() {
        runBlocking {
            val response: WebClientResponse<String> = Stopwatch.logDuration("Requesting nytimes.com") {
                KtorWebClient().getAsync("https://www.nytimes.com")
            }

            log.info { "Response has been $response" }

            if (response.successful) {

            }
        }
    }
}