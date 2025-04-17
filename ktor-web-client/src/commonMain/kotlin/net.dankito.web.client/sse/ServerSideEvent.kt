package net.dankito.web.client.sse

// copied from Ktor io.ktor.sse.ServerSentEvent

/**
 *  Server-sent event.
 *
 *  @property data data field of the event.
 *  @property event string identifying the type of event.
 *  @property id event ID.
 *  @property retry reconnection time, in milliseconds to wait before reconnecting.
 *  @property comments comment lines starting with a ':' character.
 */
class ServerSentEvent(
    val data: String? = null,
    val event: String? = null,
    val id: String? = null,
    val retry: Long? = null,
    val comments: String? = null
) {
    override fun toString(): String {
        return buildString {
            appendField("data", data)
            appendField("event", event)
            appendField("id", id)
            appendField("retry", retry)
            appendField("", comments)
        }
    }
}

private fun <T> StringBuilder.appendField(name: String, value: T?) {
    if (value != null) {
        val values = value.toString().split(END_OF_LINE_VARIANTS)
        values.forEach {
            append("$name: $it\r\n")
        }
    }
}

private val END_OF_LINE_VARIANTS: Regex = Regex("\r\n|\r|\n")
