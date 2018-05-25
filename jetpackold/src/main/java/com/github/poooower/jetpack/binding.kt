package com.github.poooower.jetpack

import android.databinding.BindingAdapter
import android.support.v4.view.ViewCompat
import android.view.View

@BindingAdapter("visibilityWithFade")
fun setVisibilityWithFade(view: View, visibility: Int) {
    val lastVisibility = view.visibility
    if (lastVisibility == visibility) {
        return
    }

    if (lastVisibility != View.VISIBLE && visibility == View.VISIBLE) {
        val alpha = view.alpha
        view.visibility = visibility
        view.alpha = 0f
        ViewCompat.animate(view).withLayer().alpha(alpha).setDuration(300).start()
    } else if (lastVisibility == View.VISIBLE && visibility != View.VISIBLE) {
        val alpha = view.alpha
        ViewCompat.animate(view).withLayer().alpha(0f).setDuration(300).withEndAction {
            view.visibility = visibility
            view.alpha = alpha
        }.start()
    }
}