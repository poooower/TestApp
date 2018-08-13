package com.github.poooower.common

import android.arch.lifecycle.*
import android.arch.paging.DataSource
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.arch.paging.PagedListAdapter
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.SystemClock
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class State constructor(val state: Int = STATE_OK, private val msg: String = "", private val msgRes: Int = 0) {

    companion object {
        const val STATE_OK = 0
        const val STATE_EMPTY = 1
        const val STATE_LOADING = 2
        const val STATE_LOAD_ERR = 3
        const val STATE_LOADING_MORE = 4
        const val STATE_LOAD_MORE_ERR = 5
        const val STATE_LOAD_MORE_COMPLETE = 6

        val OK = State(state = STATE_OK)
        val EMPTY = State(state = STATE_EMPTY)
        val LOADING = State(state = STATE_LOADING)
        val LOAD_ERR = State(state = STATE_LOAD_ERR)
        val LOADING_MORE = State(state = STATE_LOADING_MORE)
        val LOAD_MORE_ERR = State(state = STATE_LOAD_MORE_ERR)
        val LOAD_MORE_COMPLETE = State(state = STATE_LOAD_MORE_COMPLETE)
    }

    val isOK get() = state == STATE_OK
    val isEmpty get() = state == STATE_EMPTY
    val isLoading get() = state == STATE_LOADING
    val isLoadErr get() = state == STATE_LOAD_ERR
    val isLoadingMore get() = state == STATE_LOADING_MORE
    val isLoadMoreErr get() = state == STATE_LOAD_MORE_ERR
    val isLoadMoreComplete get() = state == STATE_LOAD_MORE_COMPLETE
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

    inline fun withMessage(msg: String = "", msgRes: Int = 0): State = State(state, msg, msgRes)

    inline fun checkLoading(crossinline func: () -> Unit) {
        if (!isLoading) func() else return
    }

    inline fun checkLoadingMore(crossinline func: () -> Unit) {
        if (!isLoadingMore) func() else return
    }
}

internal interface StateHolder {
    val state: LiveData<State>
}

internal class StateValidator(val version: Int, private val holder: StateHolder) {
    constructor(state: LiveData<State>, holder: StateHolder) : this(state.v, holder)

    val isValid
        get() = version === holder.state.v

}

abstract class FetchViewModel<T> : BaseViewModel(), Observer<State>, StateHolder {

    val data: LiveData<T> by lazy {
        Transformations.map(createLiveData()) {
            if (it == null) {
                state.value = State.LOADING
            }
            return@map it
        }
    }


    override val state = MutableLiveData<State>().apply {
        value = State.OK
        if (!cleared) {
            observeForever(this@FetchViewModel)
        }
    }

    override fun onCleared() {
        super.onCleared()
        state.removeObserver(this)
    }

    override fun onChanged(t: State?) {
        state?.let {
            onStateChanged()
        }
    }

    open fun onStateChanged() {
        val ss = state.value ?: return
        when {
            ss.isLoading -> {
                doOnLoading(StateValidator(state, this@FetchViewModel))
            }
            else -> {
            }
        }
    }

    private fun doOnLoading(sc: StateValidator) = ui {
        try {
            val t = bg { fetch() }


            if (sc.isValid) {
                bg {
                    afterFetch(t)
                    SystemClock.sleep(200)
                }
            }

            if (sc.isValid) {
                state.value = if (t is List<*> && t.isEmpty()) State.EMPTY else State.OK
            }

        } catch (e: Exception) {
            state.value = State.LOAD_ERR
        }
    }

    abstract fun createLiveData(): LiveData<T>

    abstract fun fetch(): T

    abstract fun afterFetch(t: T)
}

abstract class FetchWithPagedListViewModel<T> : FetchViewModel<List<T>>() {

    private var itemAtEnd: T? = null

    fun refresh() {
        state.value?.checkLoading {
            state.value = State.LOADING
        }
    }

    fun loadMore() {
        itemAtEnd?.let {
            state.value?.checkLoadingMore {
                state.value = State.LOADING_MORE
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

                    override fun onItemAtFrontLoaded(itemAtFront: T) {
                        super.onItemAtFrontLoaded(itemAtFront)
                    }

                    override fun onItemAtEndLoaded(itemAtEnd: T) {
                        this@FetchWithPagedListViewModel.itemAtEnd = itemAtEnd
                        if (state.value?.isLoadMoreErr != true && state.value?.isLoadMoreComplete != true) {
                            loadMore()
                        }
                    }
                })
                .build()
    }

    override fun onStateChanged() {
        val ss = state.value ?: return
        when {
            ss.isLoadingMore -> {
                itemAtEnd?.let {
                    doOnLoadingMore(it, StateValidator(state, this@FetchWithPagedListViewModel))
                }
            }
            else -> {
                super.onStateChanged()
            }
        }
    }

    private fun doOnLoadingMore(lastItem: T, sc: StateValidator) = ui {
        try {
            val t = bg { fetchMore(lastItem) }
            if (sc.isValid) {
                bg {
                    afterFetchMore(t)
                }
            }

            if (sc.isValid) {
                state.value = if (t.isEmpty()) State.LOAD_MORE_COMPLETE else State.OK
            }
        } catch (e: Exception) {
            state.value = State.LOAD_MORE_ERR
        }
    }

    open fun createPagedListConfig(): PagedList.Config = PagedList.Config.Builder()
            .setPageSize(20)
            .setPrefetchDistance(5)
            .setEnablePlaceholders(true)
            .build()

    override fun createLiveData(): LiveData<List<T>> {
        throw Exception("should not call this")
    }

    abstract fun createDataSource(): DataSource.Factory<*, T>

    override fun fetch(): List<T> {
        return fetch(null)
    }

    override fun afterFetch(list: List<T>) {
        afterFetch(false, list)
    }

    private fun fetchMore(lastItem: T): List<T> {
        return fetch(lastItem)
    }

    private fun afterFetchMore(list: List<T>) {
        afterFetch(true, list)
    }

    abstract fun fetch(lastItem: T?): List<T>

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