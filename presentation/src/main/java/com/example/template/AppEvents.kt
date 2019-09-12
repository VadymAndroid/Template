package com.example.template

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppEvents @Inject constructor() {

    private val listeners: MutableList<Listener> = mutableListOf()

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun notifyListeners(notification: (Listener) -> Unit) {
        listeners.forEach(notification)
    }

    interface Listener {
    }
}
