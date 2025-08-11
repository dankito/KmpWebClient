package net.dankito.web.client.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import net.codinux.log.logger
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

open class JacksonJsonSerializer : Serializer {

    protected val objectMapper = ObjectMapper().apply {
        findAndRegisterModules()

        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

        setDefaultPrettyPrinter(CustomPrettyPrinter())
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
            } else if (typeClass.isSubclassOf(Map::class) && genericType1 != null && genericType2 != null) {
                typeFactory.constructMapType(Map::class.java, genericType1.java, genericType2.java)
            } else {
                typeFactory.constructType(typeClass.java)
            }

            objectMapper.readValue(serializedObject, javaType)
        } catch (e: Throwable) {
            log.error(e) { "Could not map JSON to $typeClass:\n$${prettyPrint(serializedObject)}" }
            throw e
        }


    protected open fun prettyPrint(json: String): String {
        val jsonNode = objectMapper.readTree(json)

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode)
    }


    class CustomPrettyPrinter : DefaultPrettyPrinter() {
        init {
            // Remove space before colon
            _objectFieldValueSeparatorWithSpaces = ": "

            //_objectEntrySeparator = ",\n"
        }

        override fun createInstance(): DefaultPrettyPrinter {
            return CustomPrettyPrinter()
        }

        override fun writeStartArray(g: JsonGenerator) {
            g.writeRaw("[\n")
            _nesting++
            _objectIndenter.writeIndentation(g, _nesting)
        }

        override fun writeEndArray(g: JsonGenerator, nrOfValues: Int) {
            _nesting--
            if (nrOfValues > 0) {
                g.writeRaw('\n')
                _objectIndenter.writeIndentation(g, _nesting)
            }
            g.writeRaw("]")
        }

//        override fun writeStartObject(g: JsonGenerator) {
//            g.writeRaw("{")
//        }

//        override fun writeEndObject(g: JsonGenerator, nrOfEntries: Int) {
//            g.writeRaw("}")
//        }

//        override fun writeObjectEntrySeparator(g: JsonGenerator) {
//            g.writeRaw(",\n")
//
////            g.writeRaw(_objectEntrySeparator)
////            _objectIndenter.writeIndentation(g, _nesting)
//        }

//        override fun writeArrayValueSeparator(g: JsonGenerator) {
//            g.writeRaw(",\n")
//        }

//        override fun writeObjectFieldValueSeparator(g: JsonGenerator) {
//            g.writeRaw(": ")
//        }

//        override fun writeRootValueSeparator(g: JsonGenerator) {
//            g.writeRaw("\n")
//        }
    }


}