package com.swarmnyc.fulton.android

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Gson has problem for generic type like ApiResult<T>,
 * this class will generate type like ApiResult<News>
 * */
class JsonGenericType(private val mainType: Type, private vararg val genericTypes: Type) : ParameterizedType {
    override fun getRawType(): Type {
        return mainType
    }

    override fun getOwnerType(): Type {
        return mainType
    }

    override fun getActualTypeArguments(): Array<out Type> {
        return genericTypes
    }

}