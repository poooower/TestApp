package android.support.v4.app

import android.view.View

internal fun oneShortPreDraw(view: View, task: () -> Unit) {
    OneShotPreDrawListener.add(view, task)
}