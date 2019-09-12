package com.example.template.internal.di.modules

import com.example.template.ui.main.MainFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface FragmentsModule {

    @ContributesAndroidInjector
    fun mainFragmentInjector(): MainFragment

}