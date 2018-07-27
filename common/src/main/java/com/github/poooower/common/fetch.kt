package com.github.poooower.common

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.DataSource
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.arch.paging.PagedListAdapter
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.annotation.WorkerThread
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg

class State(val state: Int = OK, val msg: String = "", val msgRes: Int = 0) {

    companion object {
        const val OK = 0
        const val EMPTY = 1
        const val LOADING = 2
        const val LOAD_ERR = 3
        const val LOADING_MORE = 4
        const val LOAD_MORE_ERR = 5
        const val LOAD_MORE_COMPLETE = 6
    }

    val isOK get() = state == OK
    val isEmpty get() = state == EMPTY
    val isLoading get() = state == LOADING
    val isLoadErr get() = state == LOAD_ERR
    val isLoadingMore get() = state == LOADING_MORE
    val isLoadMoreErr get() = state == LOAD_MORE_ERR
    val isLoadMoreComplete get() = state == LOAD_MORE_COMPLETE
    val isShowContent get() = !isEmpty && !isLoadErr && !isLoading
    val isShowMore get() = isLoadingMore || isLoadMoreComplete || isLoadMoreErr
    val message
        get() = {
            if (!TextUtils.isEmpty(msg)) {
                msg
            } else if (msgRes != 0) {
                app.getString(msgRes)
            } else if (isEmpty) {
                app.getString(R.string.poco_empty)
            } else if (isLoadErr || isLoadMoreErr) {
                app.getString(R.string.poco_error)
            } else if (isLoadingMore) {
                app.getString(R.string.poco_loading_more)
            } else if (isLoadMoreComplete) {
                app.getString(R.string.poco_no_more)
            } else if (isLoading) {
                app.getString(R.string.poco_loading)
            } else {
                msg
            }
        }

    fun checkLoading(func: () -> Unit) {
        if (isLoading) {
            return
        }
        func()
    }

    fun checkLoadingMore(func: () -> Unit) {
        if (isLoadingMore) {
            return
        }
        func()
    }

    fun checkSame(another: State?, func: () -> Unit) {
        if (another === this) {
            func()
        }
    }
}

abstract class FetchViewModel<T> : BaseViewModel() {
    val state: MutableLiveData<State> = MutableLiveData<State>().also {
        it.value = State(state = State.OK)
//        it.observeForever {
//            println("XXXX${it?.state}")
//        }
    }

    val data: LiveData<T> by lazy {
        Transformations.map(createLiveData()) {
            if (it == null) {
                fetchInternal()
            }
            return@map it
        }
    }

    private fun fetchInternal() {
        val lastState = state.value
        async(UI) {
            try {
                val t = fetch()
                if (!cleared && state.value == lastState) {
                    afterFetch(t)
                    state.value = State(state = State.OK)
                }
            } catch (e: Exception) {
                if (!cleared && state.value == lastState) {
                    state.value = State(state = State.LOAD_ERR)
                }
            }
        }
    }

    abstract fun createLiveData(): LiveData<T>

    @WorkerThread
    abstract suspend fun fetch(): T

    @WorkerThread
    abstract suspend fun afterFetch(t: T)
}

abstract class FetchWithPagedListViewModel<T> : BaseViewModel() {
    val state: MutableLiveData<State> = MutableLiveData<State>().also {
        it.value = State(state = State.OK)
//        it.observeForever {
//            println("XXXX${it?.state}")
//        }
    }

    private var itemAtEnd: T? = null

    fun refresh() {
        state.value?.checkLoading {
            state.value = State(state = State.LOADING)
            fetchInternal(null)
        }
    }

    fun loadMore() {
        itemAtEnd?.let {
            state.value?.checkLoadingMore {
                state.value = State(state = State.LOADING_MORE)
                fetchInternal(it)
            }
        }
    }

    val list: LiveData<PagedList<T>> by lazy {
        val dataSource = createDataSource()
        LivePagedListBuilder(dataSource, createPagedListConfig())
                .setBoundaryCallback(object : PagedList.BoundaryCallback<T>() {
                    override fun onZeroItemsLoaded() {
                        refresh()
                    }

                    override fun onItemAtEndLoaded(itemAtEnd: T) {
                        this@FetchWithPagedListViewModel.itemAtEnd = itemAtEnd
                        if (!state.value!!.isLoadMoreErr && !state.value!!.isLoadMoreComplete) {
                            loadMore()
                        }
                    }
                })
                .build()
    }

    fun fetchInternal(lastItem: T?) {
        val loadingMore = lastItem != null
        val lastState = state.value
        bg {
            try {
                val l = fetch(lastItem)
                ifActive {
                    state.value?.checkSame(lastState) {
                        afterFetch(loadingMore, l)
                        val newState = if (loadingMore) {
                            if (l.isEmpty()) State.LOAD_MORE_COMPLETE else State.OK
                        } else {
                            if (l.isEmpty()) State.EMPTY else State.OK
                        }
                        state.postValue(State(state = newState))
                    }
                }
            } catch (e: Exception) {
                ifActive {
                    state.value?.checkSame(lastState) {
                        state.postValue(State(state = (if (loadingMore) State.LOAD_MORE_ERR else State.LOAD_ERR)))
                    }
                }
            }
        }
    }

    open fun createPagedListConfig(): PagedList.Config = PagedList.Config.Builder()
            .setPageSize(20)
            .setPrefetchDistance(5)
            .setEnablePlaceholders(true)
            .build()

    abstract fun createDataSource(): DataSource.Factory<*, T>

    @WorkerThread
    abstract fun fetch(lastItem: T?): List<T>

    @WorkerThread
    abstract fun afterFetch(loadingMore: Boolean, list: List<T>)


}

internal class FetchPagedListMoreViewHolder(view: View) : RecyclerView.ViewHolder(view)

abstract class FetchPagedListAdapter<T : Id<*>>(val itemBinder: ItemBinder, val moreLayout: Int) : PagedListAdapter<T, RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean =
            oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean =
            oldItem == newItem
}) {

    companion object {
        private const val MORE_VIEW_TYPE = -1;
    }


    var state: State? = null
        set(value) {
            field = value
            notifyItemChanged(itemCount - 1)
        }

    var moreClicker: View.OnClickListener? = null

    private fun isMorePosition(pos: Int): Boolean = realItemCount.let { if (it == 0) false else pos == it }

    override fun getItemCount(): Int {
        return realItemCount.let { if (it == 0) 0 else it + 1 }
    }

    private val realItemCount
        get() = super.getItemCount()

    override fun getItemViewType(position: Int): Int = if (isMorePosition(position)) MORE_VIEW_TYPE else itemBinder.itemLayout(position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ViewDataBinding = when (viewType) {
            MORE_VIEW_TYPE -> DataBindingUtil.inflate(LayoutInflater.from(parent.context), moreLayout, parent, false)
            else -> DataBindingUtil.inflate(LayoutInflater.from(parent.context), viewType, parent, false)
        }
        return when (viewType) {
            MORE_VIEW_TYPE -> FetchPagedListMoreViewHolder(binding.root)
            else -> object : RecyclerView.ViewHolder(binding.root) {}
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = DataBindingUtil.getBinding<ViewDataBinding>(holder.itemView)
        if (!isMorePosition(position)) {
            val item = getItem(position)
            binding?.setVariable(itemBinder.itemBR(position), item)
        } else {
            binding?.setVariable(BR.state, state)
            binding?.setVariable(BR.moreClicker, moreClicker)
        }
        binding?.executePendingBindings()
    }

}

interface Id<I> {
    val id: I?
}

interface ItemBinder {
    fun itemLayout(pos: Int): Int
    fun itemBR(pos: Int): Int
}