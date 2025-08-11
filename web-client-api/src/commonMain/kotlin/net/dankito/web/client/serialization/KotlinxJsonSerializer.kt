package net.dankito.web.client.serialization

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.SetSerializer
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
    override fun <T : Any> deserialize(serializedObject: String, typeClass: KClass<T>, genericType1: KClass<*>?, genericType2: KClass<*>?): T =
        try {
            @Suppress("UNCHECKED_CAST")
            val serializer = if ((typeClass == List::class || typeClass == Collection::class) && genericType1 != null) {
                ListSerializer(genericType1.serializer()) as KSerializer<T>
            } else if (typeClass == Set::class && genericType1 != null) {
                SetSerializer(genericType1.serializer()) as KSerializer<T>
            } else if (typeClass == Map::class && genericType1 != null && genericType2 != null) {
                MapSerializer(genericType1.serializer(), genericType2.serializer()) as KSerializer<T>
            } else {
                typeClass.serializer()
            }

            this.json.decodeFromString(serializer, serializedObject)
        } catch (e: Throwable) {
            log.error(e) { "Could not map JSON to $typeClass:\n$serializedObject" }
            throw e
        }

}