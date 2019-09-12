package com.example.template.ui.bases

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.core.internal.LocalStorage
import com.example.template.AppEvents
import com.example.template.R
import com.example.template.ui.BackButtonListener
import com.example.template.ui.bindViewModel
import dagger.android.support.DaggerAppCompatActivity
import org.jetbrains.anko.contentView
import org.jetbrains.anko.*
import javax.inject.Inject

abstract class BaseActivity : DaggerAppCompatActivity() {

    @Inject lateinit var storage: LocalStorage
    @Inject lateinit var events: AppEvents

    protected open val layoutId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)

        if (layoutId != 0) {
            setContentView(layoutId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onBackPressed() {
        val fragment = currentFragment()

        if (fragment !is BackButtonListener || !fragment.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)

    }

    protected open fun currentFragment(): Fragment? = supportFragmentManager.findFragmentById(R.id.fragment_container)

    protected inline fun <reified T> currentFragmentParametrized(): T = currentFragment() as T

    protected inline fun <reified T> currentFragment(action: T.() -> Unit) {
        (currentFragment() as? T)?.let(action)
    }

}

abstract class SelfBindingActivity<out VDB : ViewDataBinding> : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val binding: VDB by lazy { DataBindingUtil.bind<VDB>(contentView!!)!! }

    protected open val executesPendingBinding get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.bindViewModel(this)

        if (executesPendingBinding) {
            binding.executePendingBindings()
        }
    }
}

