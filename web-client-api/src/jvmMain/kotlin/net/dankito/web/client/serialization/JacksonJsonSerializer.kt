package net.dankito.web.client.serialization

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.reflect.KClass

open class JacksonJsonSerializer : Serializer {

    protected val objectMapper = ObjectMapper().apply {
        findAndRegisterModules()

        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }


    override fun serialize(obj: Any): String =
        objectMapper.writeValueAsString(obj)

    override fun <T : Any> deserialize(serializedObject: String, typeClass: KClass<T>): T =
        objectMapper.readValue(serializedObject, typeClass.java)

}