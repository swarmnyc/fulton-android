package com.swarmnyc.fulton.android.util

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import org.joda.time.DateTime
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.text.ParseException
import java.util.*

internal class IsoDateAdapter : JsonSerializer<Date>, JsonDeserializer<Date> {
    fun serialize(src: Date): JsonElement {
        val dateFormatAsString = DateTime(src).toString()
        return JsonPrimitive(dateFormatAsString)
    }

    override fun serialize(src: Date, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return serialize(src)
    }

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Date {
        if (json !is JsonPrimitive) {
            throw JsonParseException("Incorrect date value which is $json")
        }

        try {
            return DateTime(json.asString).toDate()
        } catch (e: ParseException) {
            throw JsonSyntaxException(json.asString, e)
        }
    }
}

/**
 * the ISO DATE Adapter for GSON
 */
private val isoDateAdapter: IsoDateAdapter = IsoDateAdapter()

/**
 * the instance of GSON with IsoDateAdapter
 */
val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, isoDateAdapter)
        .create()

/**
 * a extension of any object that convert it to json
 */
fun Any.toJson(): String {
    return gson.toJson(this)
}

fun Any.toJson(writer: Appendable) {
    gson.toJson(this, writer)
}


/**
 * a extension of any object that convert it to json
 */
fun Any.toJsonBytes(): ByteArray {
    return gson.toJson(this).toByteArray()
}

/**
 * a extension of string that convert it to a given typed object
 */
inline fun <reified T> String.fromJson(): T {
    val type: Type = (object : TypeToken<T>() {}).type

    return gson.fromJson(this, type)
}

/**
 * a extension of byte array that convert it to a given typed object
 */
fun <T> ByteArray.fromJson(type: Type): T {
    val reader = InputStreamReader(ByteArrayInputStream(this))
    return gson.fromJson(reader, type)
}

/**
 * a extension of byte array that convert it to a given typed object
 */
inline fun <reified T> ByteArray.fromJson(): T {
    val type: Type = (object : TypeToken<T>() {}).type
    val reader = InputStreamReader(ByteArrayInputStream(this))
    return gson.fromJson(reader, type)
}

/**
 * a DSL for json, for example
 * ``` json
 * json {
 *      "name" to "ilkin"
 *      "age" to 37
 *      "male" to true
 *      "birth" to Date()
 *      "contact" to json {
 *          "phones" to listOf("1223", "4567")
 *          "city" to "istanbul"
 *          "email" to "xxx@yyy.com"
 *      }
 * }
 * ```
 */
fun json(build: JsonObjectBuilder.() -> Unit): JsonObject {
    return JsonObjectBuilder().json(build)
}

/**
 * the class of constructing Json Object for json DSL
 */
class JsonObjectBuilder {
    /**
     * the deque of Json Object to handle embedded json DSL
     * ``` json
     * json {
     *      "embedded json" to json {
     *      }
     * }
     * ```
     */
    private val deque: Deque<JsonObject> = ArrayDeque()

    /**
     * a function of handling json DSL
     */
    fun json(build: JsonObjectBuilder.() -> Unit): JsonObject {
        deque.push(JsonObject())
        this.build()
        return deque.pop()
    }

    /**
     * handle `to` operator with means assign, like
     * ```
     * "age" to 37
     * ```
     */
    infix fun String.to(value: Any) {
        deque.peek().add(this, toJsonElement(value))
    }

    /**
     * convert a object to Json Element
     */
    private fun toJsonElement(value: Any): JsonElement {
        return when (value) {
            is JsonObject -> value
            is Number -> JsonPrimitive(value)
            is String -> JsonPrimitive(value)
            is Boolean -> JsonPrimitive(value)
            is Date -> isoDateAdapter.serialize(value)
            is Iterable<*> -> {
                val array = JsonArray()
                value.forEach { array.add(toJsonElement(it!!)) }
                array
            }
            else -> JsonPrimitive(value.toString())
        }
    }
}