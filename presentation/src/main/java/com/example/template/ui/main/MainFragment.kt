package com.example.template.ui.main

import com.example.core.interactors.TestUseCase
import com.example.template.R
import com.example.template.databinding.FragmentMainBinding
import com.example.template.ui.bases.BaseViewModel
import com.example.template.ui.bases.ViewModelFragment
import javax.inject.Inject

class MainFragment : ViewModelFragment<MainViewModel, FragmentMainBinding>() {

    override val layoutId: Int get() = R.layout.fragment_main
    override val viewModelClass  = MainViewModel::class.java

    fun getRequest(){
        viewModel.onRefresh()
    }
}



class MainViewModel @Inject constructor(
    private val testUseCase: TestUseCase
) : BaseViewModel(){


    fun onRefresh() {
        testUseCase.execute("", MarkAsMainObserver())
    }

    private inner class MarkAsMainObserver : BaseCompletableObserver() {

        override fun onComplete() {
            super.onComplete()
        }
    }

}