package com.github.poooower.common

import android.content.Context
import android.graphics.Canvas
import android.os.Parcelable
import android.support.v4.app.FragmentTabHost
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

/**
 * 解决disappear 绘制顺序问题
 */
class NavContainer : FrameLayout {
    constructor(context: Context) : super(context)

    private val disappearViews = mutableListOf<View>()

    private var dispatchDrawing = false
    private var firstDispatchDraw = false
    var navigating = false

    override fun startViewTransition(view: View?) {
        super.startViewTransition(view)
        if (navigating) {
            view?.let { disappearViews.add(it) }
        }
    }

    override fun endViewTransition(view: View?) {
        super.endViewTransition(view)
        view?.let { disappearViews.remove(it) }
    }

    override fun dispatchDraw(canvas: Canvas?) {
        dispatchDrawing = true
        firstDispatchDraw = true
        super.dispatchDraw(canvas)
        dispatchDrawing = false
    }

    override fun drawChild(canvas: Canvas?, child: View?, drawingTime: Long): Boolean {
        if (dispatchDrawing) {
            if (firstDispatchDraw) {
                firstDispatchDraw = false
                disappearViews.forEach {
                    super.drawChild(canvas, it, drawingTime)
                }

            }

            if (disappearViews.contains(child)) {
                return true
            }
        }
        return super.drawChild(canvas, child, drawingTime)
    }
}