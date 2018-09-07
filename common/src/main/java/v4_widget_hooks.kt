package android.support.v4.widget

import android.content.Context
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import org.jetbrains.anko.firstChildOrNull
import org.jetbrains.anko.forEachChild


class ScrollSwipeRefreshLayout(context: Context, attrs: AttributeSet?) : SwipeRefreshLayout(context, attrs) {
    constructor(context: Context) : this(context, null)


    private var offsetTopBottom: Int = 0


    override fun setTargetOffsetTopAndBottom(offset: Int) {
        super.setTargetOffsetTopAndBottom(offset)
//
//        forEachChild {
//            if (it != mCircleView) {
////                ViewCompat.offsetTopAndBottom(it, offset)
////                offsetTopBottom = it.top
//                it.translationY += offset.toFloat()
//            }
//        }
    }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
//        forEachChild {
//            if (it != mCircleView) {
//                ViewCompat.offsetTopAndBottom(it, offsetTopBottom)
//            }
//        }
    }
}