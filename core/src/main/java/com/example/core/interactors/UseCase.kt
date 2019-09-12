package com.example.core.interactors

import com.example.core.internal.CompletableUseCase
import io.reactivex.Completable
import javax.inject.Inject


class TestUseCase @Inject constructor() : CompletableUseCase.ParametrizedUseCase<String>() {

    override fun buildCompletable(params: String): Completable =
        networkFacade.getAll()
}