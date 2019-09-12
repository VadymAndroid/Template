package com.example.template.ui

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.databinding.*
import androidx.databinding.Observable
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.Fragment
import com.example.template.ui.bases.BaseActivity
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import org.jetbrains.anko.connectivityManager
import org.jetbrains.anko.selector
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.json.JSONException
import org.json.JSONObject
import java.text.DecimalFormat
import org.jetbrains.anko.act
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx
import java.util.*
import io.reactivex.Observable as RxObservable

fun ViewDataBinding.bindViewModel(viewModel: Any?): View =
    apply {
        setVariable(BR.viewModel, viewModel)
    }.root

fun <T> ObservableField<T>.asObservable(emitInitial: Boolean = false): RxObservable<T> =
    ObservableOnSubscribe<T> {

        addOnPropertyChangedCallback(OnPropertyChangedCallbackImpl(this, it))

    }.let { RxObservable.create<T>(it) }
        .run { takeUnless { emitInitial } ?: startWith(get()) }

private class OnPropertyChangedCallbackImpl<T>(
    private val field: ObservableField<T>,
    private val emitter: ObservableEmitter<T>
) : Observable.OnPropertyChangedCallback() {

    override fun onPropertyChanged(sender: Observable, propertyId: Int) {
        emitter.onNext(field.notNullGet())
    }
}

fun <T> ObservableField<T>.notNullGet(): T =
    get() ?: throw IllegalStateException("Field should be initialized before get.")

fun ObservableBoolean.asObservable(): RxObservable<Boolean> =
    ObservableOnSubscribe<Boolean> {
        addOnPropertyChangedCallback(OnBooleanPropertyChangedCallbackImpl(this, it))
    }.let {
        RxObservable.create(it)
    }

private class OnBooleanPropertyChangedCallbackImpl(
    private val field: ObservableBoolean,
    private val emitter: ObservableEmitter<Boolean>
) : Observable.OnPropertyChangedCallback() {

    override fun onPropertyChanged(p0: Observable?, p1: Int) {
        emitter.onNext(field.get())
    }
}


fun <T> RxObservable<T>.previousCurrentPairs(): RxObservable<Pair<T, T>> =
    flatMap { RxObservable.just(it, it) }
        .skip(1)
        .window(2)
        .flatMapSingle(RxObservable<T>::toList)
        .map { it[0] to it[1] }

fun Context.getColorBy(@ColorRes id: Int) = ContextCompat.getColor(this, id)

fun View.getColorBy(@ColorRes id: Int) = context.getColorBy(id)

fun Context.getDrawableBy(@DrawableRes id: Int) = ContextCompat.getDrawable(this, id)
fun View.getDrawableBy(@DrawableRes id: Int) = context.getDrawableBy(id)

val Context.internetConnected get() = connectivityManager.activeNetworkInfo?.isConnected == true

fun JSONObject.getNotNullString(name: String): String =
    try {
        getString(name)
    } catch (e: JSONException) {
        ""
    }

fun Context.simpleSelector(titleRes: Int, titlesIds: Array<Int>, onTitleIdClick: (Int) -> Unit) {

    selector(titlesIds.toList(),
        getItemTitle = { it },
        onClick = onTitleIdClick,
        titleRes = titleRes)
}

fun <T> Context.selector(items: List<T>,
                         getItemTitle: (T) -> Int,
                         onClick: (T) -> Unit,
                         titleRes: Int = 0) {

    selector(if (titleRes == 0) null else getString(titleRes),
        items.map(getItemTitle).map(::getString)) { _, index ->
        onClick(items[index])
    }
}

inline fun <reified E : Enum<E>> Context.enumSelector(
    noinline getItemTitle: (E) -> Int,
    noinline onClick: (E) -> Unit,
    titleRes: Int = 0) =
    selector(enumValues<E>().toList(), getItemTitle, onClick, titleRes)

fun Context.color(id: Int): Int = ContextCompat.getColor(this, id)

val Fragment.baseActivity get() = activity as BaseActivity

fun ViewGroup.inflate(@LayoutRes resource: Int, attachToRoot: Boolean = false): View =
    LayoutInflater.from(this.context).inflate(resource, this, attachToRoot)

val ViewGroup.layoutInflater get() = LayoutInflater.from(context)

fun Context.getOptionalText(id: Int, vararg arguments: Any): String = when {
    id == 0 -> ""
    arguments.isEmpty() -> resources.getString(id)
    else -> resources.getString(id, *arguments)
}


fun ObservableInt.dec() = set(get() - 1)

fun ObservableInt.inc() = set(get() + 1)

fun LocalDate?.printByStyle(style: String): String =
    "".takeIf { this == null } ?: DateTimeFormat
        .forPattern(DateTimeFormat.patternForStyle(style, Locale.getDefault()))
        .print(this)

fun DateTime.printByStyle(style: String): String =
    DateTimeFormat
        .forPattern(DateTimeFormat.patternForStyle(style, Locale.getDefault()))
        .print(this)

fun Double.toAmountString(): String {

    val wholeAmountString = DecimalFormat.getNumberInstance().format(toInt())
    val penniesAmountString = (this - toInt()).takeIf { it > 0 }
        ?.let { "%.2f".format(it).substring(1) }
        ?: ""

    return "$wholeAmountString$penniesAmountString"
}

