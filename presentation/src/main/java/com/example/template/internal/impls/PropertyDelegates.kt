package com.example.template.internal.impls

import androidx.annotation.CallSuper
import com.orhanobut.hawk.Hawk
import kotlin.reflect.KProperty

@Suppress("DEPRECATION")
inline fun <reified T> storageProperty(defaultValue: T? = null)
        : StorageProperty<T> {
    checkNotSealed<T>()
    return StorageProperty(defaultValue)
}


inline fun <reified T> checkNotSealed() {
    if (T::class.isSealed) {
        throw UnsupportedOperationException(
            "Sealed classes (${T::class.qualifiedName}) are not supported by Hawk"
        )
    }
}

open class StorageProperty<T> @Deprecated("use storageProperty() method") constructor(
    private val defaultValue: T? = null
) {

    open operator fun getValue(thisRef: Any, property: KProperty<*>): T =
        Hawk.get<T>(property.name, defaultValue)

    @CallSuper
    open operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        if (!Hawk.put(property.name, value)) {
            throw IllegalStateException()
        }
    }
}