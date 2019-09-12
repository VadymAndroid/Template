package com.example.data.net

import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET

interface AppServer {
    @GET("$API/GetAll")
    fun getAll(): Single<Response<Any>>

    companion object {
        const val API = "Api"
    }
}