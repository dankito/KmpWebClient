package net.dankito.web.client.serialization

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import net.codinux.log.logger
import kotlin.reflect.KClass

open class KotlinxJsonSerializer : Serializer {

    companion object {
        val Instance = KotlinxJsonSerializer()
    }


    protected open val json = Json {
        ignoreUnknownKeys = true
    }

    protected val log by logger()


    override fun serialize(obj: Any): String =
        json.encodeToString(obj)

    @OptIn(InternalSerializationApi::class)
    override fun <T : Any> deserialize(serializedObject: String, typeClass: KClass<T>): T =
        try {
            this.json.decodeFromString(typeClass.serializer(), serializedObject)
        } catch (e: Throwable) {
            log.error(e) { "Could not map JSON to $typeClass:\n$serializedObject" }
            throw e
        }

}