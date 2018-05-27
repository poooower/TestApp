package com.github.poooower.jetpack

import android.arch.paging.PagedList
import android.arch.paging.PagedListAdapter
import android.databinding.BindingAdapter
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v4.view.ViewCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

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

@BindingAdapter(value = ["itemBR", "itemLayout", "list"], requireAll = false)
fun <T> setPagedList(recyclerView: RecyclerView, itemBR: Int, itemLayout: Int, list: PagedList<T>?) {
    (recyclerView.adapter
            ?: (object : PagedListAdapter<T, RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<T>() {
                override fun areItemsTheSame(oldItem: T, newItem: T): Boolean =
                        oldItem == newItem

                override fun areContentsTheSame(oldItem: T, newItem: T): Boolean =
                        oldItem == newItem
            }) {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                    val binding: ViewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), itemLayout, parent, false)
                    return object : RecyclerView.ViewHolder(binding.root) {}
                }


                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    val item = getItem(position)
                    val binding = DataBindingUtil.getBinding<ViewDataBinding>(holder.itemView)
                    binding?.setVariable(itemBR, item);
                    binding?.executePendingBindings()
                }

            }.also {
                recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
                recyclerView.adapter = it
            })).also { list?.let { (recyclerView.adapter as PagedListAdapter<T, RecyclerView.ViewHolder>).submitList(it) } }
}

@BindingAdapter(value = ["swipeListener"], requireAll = false)
fun setSwipeDeleteFunc(recyclerView: RecyclerView, swipeListener: Function1<Int, Unit>?) {
    swipeListener?.let { sl ->
        ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(recyclerView: RecyclerView,
                                          viewHolder: RecyclerView.ViewHolder): Int =
                    makeMovementFlags(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(holder: RecyclerView.ViewHolder?, direction: Int) {
                holder?.let { sl.invoke(it.adapterPosition) }
            }
        }).attachToRecyclerView(recyclerView)
    }
}
