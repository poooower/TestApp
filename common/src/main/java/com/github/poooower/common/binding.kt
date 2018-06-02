package com.github.poooower.common

import android.annotation.SuppressLint
import android.arch.paging.PagedList
import android.databinding.BindingAdapter
import android.databinding.adapters.TextViewBindingAdapter
import android.support.v4.view.ViewCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.widget.TextView

@BindingAdapter("visibilityWithFade")
fun setVisibilityWithFade(view: View, visibility: Int) {
    val lastVisibility = view.visibility
    if (lastVisibility == visibility) {
        return
    }

    if (visibility == View.VISIBLE) {
        view.visibility = visibility
        view.alpha = 0f
        ViewCompat.animate(view).withLayer().alpha(1f).setDuration(300).start()
    } else if (visibility != View.VISIBLE) {
        view.visibility = visibility
        ViewCompat.animate(view).withLayer().alpha(0f).setDuration(300).start()
    }
}

@BindingAdapter(value = ["itemBinder", "itemBR", "itemLayout", "layoutManager", "list", "state", "moreClicker", "moreLayout"], requireAll = false)
fun <T : Id<*>> setPagedList(recyclerView: RecyclerView, itemBinder: ItemBinder?, itemBR: Int, itemLayout: Int, layoutManager: RecyclerView.LayoutManager?, list: PagedList<T>?, state: State?, moreClicker: View.OnClickListener, moreLayout: Int) {
    (recyclerView.adapter
            ?: (object : FetchPagedListAdapter<T>(itemBinder ?: object : ItemBinder {
                override fun itemLayout(pos: Int): Int = itemLayout
                override fun itemBR(pos: Int): Int = itemBR
            }, if (moreLayout == 0) R.layout.poco_loadmore_state_layout else moreLayout) {

            }.also {
                recyclerView.layoutManager = layoutManager ?: LinearLayoutManager(recyclerView.context)
                recyclerView.adapter = it
            })
            ).also {
        with(recyclerView.adapter as FetchPagedListAdapter<T>) {
            submitList(list)
            this.state = state
            this.moreClicker = moreClicker
        }
    }
}

@BindingAdapter(value = ["swipeListener"], requireAll = false)
fun setSwipeDeleteFunc(recyclerView: RecyclerView, swipeListener: Function1<Int, Unit>?) {
    swipeListener?.let { sl ->
        ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(recyclerView: RecyclerView,
                                          viewHolder: RecyclerView.ViewHolder): Int =
                    makeMovementFlags(0, when (viewHolder) {
                        is FetchPagedListMoreViewHolder -> 0
                        else -> ItemTouchHelper.RIGHT
                    })

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(holder: RecyclerView.ViewHolder?, direction: Int) {
                holder?.let { sl.invoke(it.adapterPosition) }
            }
        }).attachToRecyclerView(recyclerView)
    }
}

@SuppressLint("RestrictedApi")
@BindingAdapter(value = ["android:text"], requireAll = true)
fun setText(textView: TextView, text: Function0<CharSequence>?) {
    TextViewBindingAdapter.setText(textView, text?.invoke())
}

@BindingAdapter(value = ["isRefreshing"], requireAll = true)
fun setIsRefreshing(refreshLayout: SwipeRefreshLayout, isRefreshing: Boolean) {
    if (refreshLayout.isRefreshing != isRefreshing) {
        refreshLayout.isRefreshing = isRefreshing
    }
}