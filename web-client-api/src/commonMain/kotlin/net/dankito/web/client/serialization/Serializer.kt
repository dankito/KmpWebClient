package net.dankito.web.client.serialization

import kotlin.reflect.KClass

interface Serializer {

    fun serialize(obj: Any): String

    fun <T : Any> deserialize(serializedObject: String, typeClass: KClass<T>, genericType1: KClass<*>? = null, genericType2: KClass<*>? = null): T

}