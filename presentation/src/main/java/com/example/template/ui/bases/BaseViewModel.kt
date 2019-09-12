package com.example.template.ui.bases

import androidx.annotation.CallSuper
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import com.example.core.internal.BaseUseCase
import com.example.core.internal.LocalStorage
import com.example.template.AppEvents
import com.example.template.ui.bases.action.Refreshable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import javax.inject.Inject
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

abstract class BaseViewModel : ViewModel(), AppEvents.Listener {

    @Inject lateinit var events: AppEvents

    @Inject lateinit var storage: LocalStorage

    protected val disposables = CompositeDisposable()

    @Inject
    fun bindToAppEvents() {
        events.addListener(this)
    }

    @CallSuper
    override fun onCleared() {
        disposables.dispose()
        events.removeListener(this)
    }


    protected inner class StorageProperty<T>(
        private val property: KMutableProperty1<LocalStorage, T>,
        private val changeCallback: ((T) -> Unit)? = null) {

        operator fun getValue(thisRef: Any, p: KProperty<*>): T =
            property.get(storage)

        operator fun setValue(thisRef: Any, p: KProperty<*>, value: T) {
            property.set(storage, value)
            changeCallback?.invoke(value)
        }
    }


    open class ProgressMode(
        val processStart: BaseViewModel.() -> Unit,
        val processTerminate: BaseViewModel.() -> Unit
    ) {
        object PRE_LOADER : ProgressMode(
            processStart = {  },
            processTerminate = {}
        )

        object DIALOG : ProgressMode(
            processStart = {  },
            processTerminate = {}
        )

        object EMPTY : ProgressMode({}, {})
    }

    fun ObservableBoolean.asProgressMode() = ProgressMode({ set(true) }, { set(false) })


    private interface ObserverBehavior : Disposable {

        val progressMode: ProgressMode get() = ProgressMode.PRE_LOADER

        val messageAfterCompletion get() = 0
        val exitAfterCompletion get() = false

        val owner: BaseViewModel

        fun onMapError(e: Throwable) = 0

        /**
         * if true alert will not be shown
         */
        fun onHandleError(e: Throwable) = false

        /* not overridable */
        fun ObserverBehavior.processStart() {
            owner.disposables.add(this)
            progressMode.processStart.invoke(owner)
        }

        /* not overridable */
        fun ObserverBehavior.processError(e: Throwable) {
            processTerminate()
            showAlertIfNotHandled(e)
        }

        fun ObserverBehavior.processCompletion() {
            processTerminate()
        }

        fun ObserverBehavior.processTerminate() {
            progressMode.processTerminate(owner)
        }

        /* not overridable */
        fun ObserverBehavior.showAlertIfNotHandled(e: Throwable) {
            when {
                !onHandleError(e) -> {
                }
            }
        }
    }


    protected abstract inner class BaseObserver<T>(
        override val messageAfterCompletion: Int = 0,
        override val exitAfterCompletion: Boolean = false
    ) : DisposableObserver<T>(),
        ObserverBehavior {

        final override val owner get() = this@BaseViewModel

        @CallSuper
        override fun onStart() {
            processStart()
        }

        @CallSuper
        override fun onComplete() {
            processCompletion()
        }

        @CallSuper
        override fun onError(e: Throwable) {
            showAlertIfNotHandled(e)
        }
    }


    protected open inner class BaseSingleObserver<T>(
        override val messageAfterCompletion: Int = 0,
        override val exitAfterCompletion: Boolean = false
    ) : DisposableSingleObserver<T>(),
        ObserverBehavior {

        final override val owner get() = this@BaseViewModel

        @CallSuper
        override fun onStart() {
            processStart()
        }

        @CallSuper
        override fun onSuccess(t: T) {
            processCompletion()
        }

        @CallSuper
        override fun onError(e: Throwable) {
            processError(e)
        }
    }


    protected open inner class BaseMaybeObserver<T>(
        override val messageAfterCompletion: Int = 0,
        override val exitAfterCompletion: Boolean = false
    ) : DisposableMaybeObserver<T>(),
        ObserverBehavior {

        final override val owner get() = this@BaseViewModel

        @CallSuper
        override fun onStart() {
            processStart()
        }

        @CallSuper
        override fun onSuccess(t: T) {
            processCompletion()
        }

        @CallSuper
        override fun onComplete() {
            processCompletion()
        }

        @CallSuper
        override fun onError(e: Throwable) {
            processError(e)
        }
    }

    protected open inner class BaseCompletableObserver(
        override val messageAfterCompletion: Int = 0,
        override val exitAfterCompletion: Boolean = false
    ) : DisposableCompletableObserver(),
        ObserverBehavior {

        final override val owner get() = this@BaseViewModel

        @CallSuper
        override fun onStart() {
            processStart()
        }

        @CallSuper
        override fun onComplete() {
            processCompletion()
        }

        @CallSuper
        override fun onError(e: Throwable) {
            processError(e)
        }
    }


    protected open inner class NoResponseFromServerCompletableObserver(
    ) : DisposableCompletableObserver(){

        @CallSuper
        override fun onStart() {
        }

        @CallSuper
        override fun onComplete() {
        }

        @CallSuper
        override fun onError(e: Throwable) {
        }
    }


    protected abstract inner class LazyRefreshable<R> : Refreshable() {

        private var criteria: BaseUseCase.ReloadCriteria = BaseUseCase.ReloadCriteria.IF_EXPIRED

        protected abstract fun doExecute(criteria: BaseUseCase.ReloadCriteria, o: DisposableObserver<R>)
        protected abstract fun onValue(value: R)

        protected abstract fun onError(e: Throwable)

        final override fun onRefresh() {
            doExecute(criteria, ObserverImpl())
            criteria = BaseUseCase.ReloadCriteria.FORCED
        }

        private inner class ObserverImpl : RefreshableObserver<R>(this) {

            override fun onNext(t: R) {
                onValue(t)
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                this@LazyRefreshable.onError(e)
            }
        }
    }


    protected class RefreshableProgressMode(refreshable: Refreshable)
        : ProgressMode(
        {},
        { refreshable.notifyRefreshTerminated() }
    )


    protected abstract inner class RefreshableObserver<R>(refreshable: Refreshable)
        : BaseObserver<R>() {

        override val progressMode = RefreshableProgressMode(refreshable)
    }


    protected abstract inner class RefreshableSingleObserver<R>(
        refreshable: Refreshable
    ) : BaseSingleObserver<R>() {

        override val progressMode = RefreshableProgressMode(refreshable)
    }
}
