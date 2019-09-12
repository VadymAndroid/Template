package com.example.data.net

import com.orhanobut.hawk.Hawk

internal enum class HeaderHolder(val key: String, private val hardCodeValue: String? = null) {
    TOKEN("token");

    var header: String
        inline get() = hardCodeValue ?: Hawk.get<String>(name, "")
        inline set(value) {
            require(hardCodeValue == null) { "Tring to re-set value of const Header." }
            Hawk.put(name, value)
        }
}
