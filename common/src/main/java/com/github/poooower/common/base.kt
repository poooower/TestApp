package com.github.poooower.common

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.NavHostFragment

lateinit var app: Application

open class App : Application() {
    override fun onCreate() {
        app = this
        super.onCreate()
    }
}

open class BaseViewModel : ViewModel() {
    @Volatile
    var cleared: Boolean = false

    override fun onCleared() {
        super.onCleared()
        cleared = true
    }

    fun ifActive(func: () -> Unit) {
        if (!cleared) {
            func()
        }
    }
}

class NavFragment : NavHostFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val frameLayout = NavContainer(inflater.context)
        frameLayout.id = id
        return frameLayout
    }

    override fun createFragmentNavigator(): Navigator<androidx.navigation.fragment.FragmentNavigator.Destination> {
        return FragmentNavigator(context!!, childFragmentManager, id)
    }

    @Navigator.Name("fragment")
    class FragmentNavigator(private val context: Context, private val fragmentManager: FragmentManager, private val containerId: Int) : Navigator<androidx.navigation.fragment.FragmentNavigator.Destination>() {
        private var mBackStackCount: Int = 0
        private val onBackStackChangedListener = FragmentManager.OnBackStackChangedListener {
            val newCount = fragmentManager.backStackEntryCount
            val backStackEffect: Int
            if (newCount < mBackStackCount) {
                backStackEffect = Navigator.BACK_STACK_DESTINATION_POPPED
            } else if (newCount > mBackStackCount) {
                backStackEffect = Navigator.BACK_STACK_DESTINATION_ADDED
            } else {
                backStackEffect = Navigator.BACK_STACK_UNCHANGED
            }
            mBackStackCount = newCount

            var destId = 0
            val state = getState()
            if (state != null) {
                destId = state!!.mCurrentDestId
            }
            dispatchOnNavigatorNavigated(destId, backStackEffect)
        }

        init {
            mBackStackCount = fragmentManager.backStackEntryCount
            fragmentManager.addOnBackStackChangedListener(onBackStackChangedListener)
        }

        override fun popBackStack(): Boolean {
            return fragmentManager.popBackStackImmediate()
        }

        override fun createDestination(): androidx.navigation.fragment.FragmentNavigator.Destination {
            return androidx.navigation.fragment.FragmentNavigator.Destination(this)
        }

        private fun getBackStackName(@IdRes destinationId: Int): String {
            // This gives us the resource name if it exists,
            // or just the destinationId if it doesn't exist
            try {
                return context.resources.getResourceName(destinationId)
            } catch (e: Resources.NotFoundException) {
                return Integer.toString(destinationId)
            }

        }

        override fun navigate(destination: androidx.navigation.fragment.FragmentNavigator.Destination, args: Bundle?,
                              navOptions: NavOptions?) {
            val frag = destination.createFragment(args)
            val ft = fragmentManager.beginTransaction()

            var enterAnim = navOptions?.enterAnim ?: -1
            var exitAnim = navOptions?.exitAnim ?: -1
            var popEnterAnim = navOptions?.popEnterAnim ?: -1
            var popExitAnim = navOptions?.popExitAnim ?: -1
            if (enterAnim != -1 || exitAnim != -1 || popEnterAnim != -1 || popExitAnim != -1) {
                enterAnim = if (enterAnim != -1) enterAnim else 0
                exitAnim = if (exitAnim != -1) exitAnim else 0
                popEnterAnim = if (popEnterAnim != -1) popEnterAnim else 0
                popExitAnim = if (popExitAnim != -1) popExitAnim else 0
                ft.setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim)
            }


            fragmentManager.fragments.filter { it !is StateFragment && !it.isHidden }.forEach { ft.hide(it) }
            ft.add(containerId, frag)
//            ft.replace(containerId, frag)


            val oldState = getState()
            if (oldState != null) {
                ft.remove(oldState)
            }

            @IdRes val destId = destination.id
            val newState = StateFragment()
            newState.mCurrentDestId = destId
            ft.add(newState, StateFragment.FRAGMENT_TAG)

            val initialNavigation = fragmentManager.fragments.isEmpty()
            val isClearTask = navOptions != null && navOptions.shouldClearTask()
            // TODO Build first class singleTop behavior for fragments
            val isSingleTopReplacement = (navOptions != null && oldState != null
                    && navOptions.shouldLaunchSingleTop()
                    && oldState!!.mCurrentDestId == destId)
            if (!initialNavigation && !isClearTask && !isSingleTopReplacement) {
                ft.addToBackStack(getBackStackName(destId))
            } else {
                ft.runOnCommit {
                    dispatchOnNavigatorNavigated(destId, if (isSingleTopReplacement)
                        Navigator.BACK_STACK_UNCHANGED
                    else
                        Navigator.BACK_STACK_DESTINATION_ADDED)
                }
            }
            ft.commit()
            fragmentManager.executePendingTransactions()
        }

        private fun getState(): StateFragment? {
            return fragmentManager.findFragmentByTag(StateFragment.FRAGMENT_TAG) as StateFragment?
        }
    }

    class StateFragment : Fragment() {

        internal var mCurrentDestId: Int = 0

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            if (savedInstanceState != null) {
                mCurrentDestId = savedInstanceState.getInt(KEY_CURRENT_DEST_ID)
            }
        }

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            outState.putInt(KEY_CURRENT_DEST_ID, mCurrentDestId)
        }

        companion object {
            internal val FRAGMENT_TAG = "android-support-nav:FragmentNavigator.StateFragment"

            private val KEY_CURRENT_DEST_ID = "currentDestId"
        }
    }


}

