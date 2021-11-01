package com.kimym.translator.util

import android.view.View
import androidx.databinding.BindingAdapter

object BindingAdapter {
    @BindingAdapter("hideIf")
    @JvmStatic
    fun setHideIf(view: View, visible: Boolean) {
        view.visibility = when (visible) {
            true -> View.VISIBLE
            false -> View.INVISIBLE
        }
    }
}
