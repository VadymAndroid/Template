package com.example.template.internal.di

import android.content.Context
import com.example.data.di.NetworkModule
import com.example.template.Application
import com.example.template.internal.di.modules.*
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    ActivitiesModule::class,
    FragmentsModule::class,
    ViewModelModule::class,
    ImplModule::class,
    SchedulersModule::class,
    NetworkModule::class
])
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance fun context(context: Context): Builder

        fun build(): AppComponent
    }

    fun inject(app: Application)
}