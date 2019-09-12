package com.example.template

import androidx.multidex.MultiDexApplication
import com.example.template.internal.di.DaggerAppComponent
import com.orhanobut.hawk.Hawk
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.reactivex.plugins.RxJavaPlugins
import javax.inject.Inject

class Application : MultiDexApplication(), HasAndroidInjector {


    @Inject
    lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Any>

    override fun onCreate() {
        super.onCreate()

        DaggerAppComponent.builder()
            .context(this)
            .build()
            .inject(this)

        Hawk.init(this)
            .setLogInterceptor {
                check(!it.contains("Converter failed")) { "Hawk issues: ${it}" }
            }
            .build()

        RxJavaPlugins.setErrorHandler {}
    }


    // override fun activityInjector(): AndroidInjector<Activity> = dispatchingActivityInjector

    override fun androidInjector(): AndroidInjector<Any> = dispatchingActivityInjector
}
