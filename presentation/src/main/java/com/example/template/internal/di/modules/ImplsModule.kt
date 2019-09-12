package com.example.template.internal.di.modules

import androidx.lifecycle.ViewModelProvider
import com.example.core.internal.LocalStorage
import com.example.core.internal.NetworkFacade
import com.example.data.impl.NetworkFacadeImpl
import com.example.template.internal.impls.*
import dagger.Binds
import dagger.Module

@Module
interface ImplModule {

    @Binds fun provideLocalStorage(localStorage: LocalStorageImpl): LocalStorage
    @Binds fun provideNetworkFacade(networkFacade: NetworkFacadeImpl): NetworkFacade
    @Binds fun bindViewModelFactory(factory: ViewModelFactoryImpl): ViewModelProvider.Factory

}