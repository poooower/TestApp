package com.github.poooower.jetpack

import android.os.AsyncTask

open class Singleton<out T, in A>(private val creator: (A) -> T) {
    @Volatile
    private var instance: T? = null

    fun get(arg: A): T = instance ?: synchronized(this) {
        instance ?: creator(arg).also { instance = it }
    }
}

enum class State { OK, FAIL, LOADING }

data class StateData<D>(val data: D? = null, val state: State = State.OK, val msg: String = "") {
    fun isOK() = state == State.OK
    fun isFail() = state == State.FAIL
    fun isLoading() = state == State.LOADING
}

class InternalAsyncTask<R>(val builder: TaskBuilder<R>) : AsyncTask<Void, Void, R>() {

    override fun onPreExecute() {
        super.onPreExecute()
        builder.tag?.let { builder.holder?.cancelByTag(builder.tag!!) }
        builder.holder?.onStart(this)
    }

    override fun doInBackground(vararg params: Void?) = builder.background?.invoke()

    override fun onCancelled() {
        super.onCancelled()
        builder.holder?.onCancel(this)
    }

    override fun onPostExecute(result: R) {
        super.onPostExecute(result)
        builder.ui?.invoke(result)
        builder.holder?.onFinish(this)
    }

}

class TaskHolder {
    private val tasks: MutableSet<InternalAsyncTask<*>> = mutableSetOf()
    fun onStart(task: InternalAsyncTask<*>) {
        tasks.add(task)
    }

    fun onFinish(task: InternalAsyncTask<*>) {
        tasks.remove(task)
    }

    fun onCancel(task: InternalAsyncTask<*>) {
        tasks.remove(task)
    }

    fun cancelByTag(tag: String) {
        tasks.firstOrNull() { task ->
            tag == task.builder.tag
        }?.let {
            it.cancel(true)
        }
    }

    fun cancelAll() {
        val list = tasks.toList()
        for (task in list) {
            task.cancel(true)
        }
    }
}

class TaskBuilder<R> {
    var tag: String? = null
    var holder: TaskHolder? = null
    var background: (() -> R)? = null
    var ui: ((r: R) -> Unit)? = null

    fun background(background: () -> R): TaskBuilder<R> {
        this.background = background
        return this
    }

    fun ui(ui: (r: R) -> Unit): TaskBuilder<R> {
        this.ui = ui
        return this
    }

    fun start(holder: TaskHolder? = null, tag: String? = null): AsyncTask<Void, Void, R> {
        this.holder = holder
        this.tag = tag
        return InternalAsyncTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }
}

fun <R> background(background: () -> R): TaskBuilder<R> = TaskBuilder<R>().background(background)

