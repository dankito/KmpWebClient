package net.dankito.web.client

import kotlin.random.Random
import kotlin.time.TimeSource

object UserAgent {

    const val Chrome_MacOS_138 = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36"

    const val Chrome_Windows_138 = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36"

    const val Chrome_Linux_137 = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36"

    const val Firefox_MacOS_139 = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:139.0) Gecko/20100101 Firefox/139.0"

    const val Firefox_Windows_139 = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:139.0) Gecko/20100101 Firefox/139.0"

    const val Firefox_Linux_139 = "Mozilla/5.0 (X11; Linux x86_64; rv:139.0) Gecko/20100101 Firefox/139.0"

    const val Safari_18 = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.5 Safari/605.1.15"

    const val Edge_137 = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36 Edg/137.0.0.0"

    const val iPhone_16 = "Mozilla/5.0 (iPhone17,3; CPU iPhone OS 18_3_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 FireKeepers/1.6.1"

    const val Android = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"

    val All = listOf(Android, iPhone_16,
        Chrome_Linux_137, Chrome_Windows_138, Chrome_MacOS_138, Firefox_Linux_139, Firefox_Windows_139, Firefox_MacOS_139,
        Edge_137, Safari_18
    )


    private val random by lazy {
        val seed = TimeSource.Monotonic.markNow().elapsedNow().inWholeNanoseconds
        Random(seed)
    }

    fun random(): String = All[random.nextInt(All.size)]

    fun latest(mobile: Boolean = false) =
        if (mobile) {
            Android
        } else {
            Chrome_Windows_138
        }

}