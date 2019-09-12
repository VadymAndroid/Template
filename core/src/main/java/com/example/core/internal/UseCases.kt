package com.example.core.internal

import io.reactivex.*
import io.reactivex.disposables.Disposables
import io.reactivex.observers.*
import javax.inject.Inject
import kotlin.reflect.KMutableProperty1

sealed class BaseUseCase {

    @Inject
    lateinit var networkFacade: NetworkFacade
    @Inject
    lateinit var localStorage: LocalStorage

    @Inject
    lateinit var schedulers: Map<SchedulerType, Scheduler>

    protected var disposable = Disposables.empty()

    fun cancel() {
        disposable.dispose()
    }


    enum class SchedulerType {
        WORK,
        WORK_RESULT
    }

    enum class ReloadCriteria {
        IF_EXPIRED, FORCED
    }

    protected fun <T> lazyRepo(
        cache: KMutableProperty1<LocalStorage, LocalStorage.ExpirableValue<T>>,
        update: (NetworkFacade) -> Single<T>
    ) = LazyRepo(
        localStorage,
        networkFacade,
        cache,
        update
    )
}


sealed class UseCase<in P, R> : BaseUseCase() {

    protected abstract fun buildObservable(params: P): Observable<R>

    protected fun doExecute(params: P, o: DisposableObserver<R>) {
        cancel()
        disposable = o
        buildObservable(params)
            .subscribeOn(schedulers[SchedulerType.WORK])
            .observeOn(schedulers[SchedulerType.WORK_RESULT])
            .subscribe(o)
    }

    abstract class ParametrizedUseCase<in P : Any, R> : UseCase<P, R>() {

        fun execute(params: P, o: DisposableObserver<R>) {
            doExecute(params, o)
        }
    }

    abstract class SimpleUseCase<R> : UseCase<Nothing?, R>() {

        fun execute(o: DisposableObserver<R>) {
            doExecute(null, o)
        }
    }
}

sealed class SingleUseCase<in P, R> : BaseUseCase() {

    protected abstract fun buildSingle(params: P): Single<R>

    protected fun doExecute(params: P, o: DisposableSingleObserver<R>) {
        cancel()
        disposable = o
        buildSingle(params)
            .subscribeOn(schedulers[SchedulerType.WORK])
            .observeOn(schedulers[SchedulerType.WORK_RESULT])
            .subscribe(o)
    }

    abstract class ParametrizedUseCase<in P : Any, R> : SingleUseCase<P, R>() {

        fun execute(params: P, o: DisposableSingleObserver<R>) {
            doExecute(params, o)
        }
    }

    abstract class SimpleUseCase<R> : SingleUseCase<Nothing?, R>() {

        fun execute(o: DisposableSingleObserver<R>) {
            doExecute(null, o)
        }
    }
}

sealed class CompletableUseCase<in P> : BaseUseCase() {

    protected abstract fun buildCompletable(params: P): Completable

    protected fun doExecute(params: P, o: DisposableCompletableObserver) {
        cancel()
        disposable = o
        buildCompletable(params)
            .subscribeOn(schedulers[SchedulerType.WORK])
            .observeOn(schedulers[SchedulerType.WORK_RESULT])
            .subscribe(o)
    }

    abstract class ParametrizedUseCase<in P : Any> : CompletableUseCase<P>() {

        fun execute(params: P, o: DisposableCompletableObserver) {
            doExecute(params, o)
        }
    }

    abstract class SimpleUseCase : CompletableUseCase<Nothing?>() {

        fun execute(o: DisposableCompletableObserver) {
            doExecute(null, o)
        }
    }
}

sealed class MaybeUseCase<in P, R> : BaseUseCase() {

    protected abstract fun buildMaybe(params: P): Maybe<R>

    protected fun doExecute(params: P, o: DisposableMaybeObserver<R>) {
        cancel()
        disposable = o
        buildMaybe(params)
            .subscribeOn(schedulers[SchedulerType.WORK])
            .observeOn(schedulers[SchedulerType.WORK_RESULT])
            .compose(::onMainThread)
            .subscribe(o)
    }

    protected open fun onMainThread(o: Maybe<R>): Maybe<R> = o

    abstract class ParametrizedUseCase<in P : Any, R> : MaybeUseCase<P, R>() {

        fun execute(params: P, o: DisposableMaybeObserver<R>) {
            doExecute(params, o)
        }
    }

    abstract class SimpleUseCase<R> : MaybeUseCase<Nothing?, R>() {

        fun execute(o: DisposableMaybeObserver<R>) {
            doExecute(null, o)
        }
    }
}
