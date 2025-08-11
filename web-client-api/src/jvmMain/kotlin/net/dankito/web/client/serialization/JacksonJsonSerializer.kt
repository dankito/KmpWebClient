package net.dankito.web.client.serialization

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import net.codinux.log.logger
import kotlin.reflect.KClass

open class JacksonJsonSerializer : Serializer {

    protected val objectMapper = ObjectMapper().apply {
        findAndRegisterModules()

        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }

    protected val log by logger()


    override fun serialize(obj: Any): String =
        objectMapper.writeValueAsString(obj)

    override fun <T : Any> deserialize(serializedObject: String, typeClass: KClass<T>): T =
        try {
            objectMapper.readValue(serializedObject, typeClass.java)
        } catch (e: Throwable) {
            log.error(e) { "Could not map JSON to $typeClass:\n$serializedObject" }
            throw e
        }

}