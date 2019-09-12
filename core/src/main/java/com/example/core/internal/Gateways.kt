package com.example.core.internal

import io.reactivex.Completable

interface Logouter {
    val loggedIn: Boolean
    fun logout()
}

interface LocalStorage {

    var onBoardingShown: Boolean

    data class ExpirableValue<T>(
        val value: T,
        val expired: Boolean = true
    )
}


interface NetworkFacade {

    fun getAll(): Completable
}
