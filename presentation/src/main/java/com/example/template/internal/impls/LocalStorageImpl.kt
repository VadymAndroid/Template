package com.example.template.internal.impls

import com.example.core.internal.LocalStorage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStorageImpl @Inject constructor() : LocalStorage {

    override var onBoardingShown by storageProperty(false)
}