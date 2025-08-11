package net.dankito.web.client.serialization

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import net.codinux.log.logger
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

open class JacksonJsonSerializer : Serializer {

    protected val objectMapper = ObjectMapper().apply {
        findAndRegisterModules()

        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }

    protected val typeFactory = objectMapper.typeFactory

    protected val log by logger()


    override fun serialize(obj: Any): String =
        objectMapper.writeValueAsString(obj)

    override fun <T : Any> deserialize(serializedObject: String, typeClass: KClass<T>, genericType1: KClass<*>?, genericType2: KClass<*>?): T =
        try {
            val javaType = if (typeClass.isSubclassOf(Set::class) && genericType1 != null) {
                typeFactory.constructCollectionType(Set::class.java, genericType1.java)
            } else if ((typeClass.isSubclassOf(List::class) || typeClass.isSubclassOf(Collection::class)) && genericType1 != null) {
                typeFactory.constructCollectionType(List::class.java, genericType1.java)
            } else {
                typeFactory.constructType(typeClass.java)
            }

            objectMapper.readValue(serializedObject, javaType)
        } catch (e: Throwable) {
            log.error(e) { "Could not map JSON to $typeClass:\n$serializedObject" }
            throw e
        }

}