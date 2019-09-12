package com.example.template.internal.di.modules

import com.example.template.ui.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
interface ActivitiesModule {

    @ContributesAndroidInjector
    fun mainActivityInjector(): MainActivity

}