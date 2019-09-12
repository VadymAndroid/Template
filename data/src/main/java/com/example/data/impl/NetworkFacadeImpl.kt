package com.example.data.impl

import android.content.Context
import com.example.core.internal.NetworkFacade
import com.example.data.net.AppServer
import io.reactivex.Completable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkFacadeImpl @Inject constructor(
    private val api: AppServer,
    private val context: Context
) : NetworkFacade {

    override fun getAll(): Completable =
        api.getAll()
            .ignoreElement()


}