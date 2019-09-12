package com.example.core.internal

import io.reactivex.Observable
import io.reactivex.Single
import kotlin.reflect.KMutableProperty1

class LazyRepo<T>(
    private val storage: LocalStorage,
    private val facade: NetworkFacade,
    private val cache: KMutableProperty1<LocalStorage, LocalStorage.ExpirableValue<T>>,
    private val update: (NetworkFacade) -> Single<T>
) {

    fun get(criteria: BaseUseCase.ReloadCriteria): Observable<T> =
        cache.get(storage).let { cached ->
            when {
                criteria == BaseUseCase.ReloadCriteria.FORCED || cached.expired ->
                    update(facade)
                        .doOnSuccess { cache.set(storage, LocalStorage.ExpirableValue(it)) }
                        .toObservable()
                        .startWith(cached.value)

                else -> Observable.just(cached.value)
            }
        }
}
