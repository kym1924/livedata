package com.kimym.translator.util

import android.animation.ObjectAnimator
import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.rotationAnimation() {
    ObjectAnimator.ofFloat(this, "rotationY", 0f, 360f).start()
}

fun View.showSnackBar(msg: String) {
    Snackbar.make(this, msg, Snackbar.LENGTH_SHORT).show()
}
