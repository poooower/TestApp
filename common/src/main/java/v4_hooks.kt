package android.support.v4.app

import android.view.View

internal fun oneShotPreDraw(view: View, task: () -> Unit) {
    OneShotPreDrawListener.add(view, task)
}