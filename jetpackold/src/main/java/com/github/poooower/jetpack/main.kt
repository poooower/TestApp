package com.github.poooower.jetpack

import android.animation.Animator
import android.animation.AnimatorInflater
import android.app.Application
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation.findNavController
import androidx.navigation.navOptions
import com.github.poooower.common.*
import com.github.poooower.common.App
import com.github.poooower.jetpack.databinding.FragmentHomeBinding
import com.github.poooower.jetpack.databinding.FragmentMainBinding
import com.github.poooower.jetpack.databinding.FragmentUserListBinding


class App : App()

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        oneShotPreDraw(window.decorView) {
            window.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
    }
}

class MainFragment : Fragment() {
    val fragments: Array<Class<out Fragment>> = arrayOf(HomeFragment::class.java, DiscoverFragment::class.java, MeFragment::class.java)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = FragmentMainBinding.inflate(inflater, container, false).let {
        it.mainFragment = this
        it.executePendingBindings()
        return it.root
    }
}

class HomeFragment : Fragment() {
    companion object {
        const val COUNT = 3
    }

    val titles: Array<String> = Array(COUNT) {
        "Title$it"
    }

    val arguments: Array<Bundle> = Array(COUNT) {
        val args = Bundle()
        args.putString("pageIndex", "$it")
        args
    }

    val fragments: Array<Class<out Fragment>> = Array(COUNT) {
        HomePageFragment::class.java
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = FragmentHomeBinding.inflate(inflater, container, false).let {
        it.homeFragment = this
        it.executePendingBindings()
        return it.root
    }
}

class HomePageFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_main, rootKey)
        for (i in 0 until preferenceScreen.preferenceCount) {
            val preference = preferenceScreen.getPreference(i)
            preference.title = "$i.${preference.title}${arguments?.get("pageIndex") ?: ""}"
            preference.extras.putInt("actionId", preference.widgetLayoutResource)
            preference.widgetLayoutResource = 0
        }
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        val actionId = preference?.extras?.getInt("actionId", 0)
                ?: 0
        return when (actionId) {
            0 -> super.onPreferenceTreeClick(preference)
            else -> view?.let {
                val time = System.currentTimeMillis()
                activity?.findViewById<NavContainer>(R.id.main_nav_host_fragment)?.navigating = true
                findNavController(it).navigate(actionId, null, navOptions {
                    anim {
                        enter = R.anim.slide_in_right
                        exit = R.animator.stay
                        popEnter = R.animator.stay
                        popExit = R.anim.slide_out_right
                    }
                })
                activity?.findViewById<NavContainer>(R.id.main_nav_host_fragment)?.navigating = false
                println("XXXX${System.currentTimeMillis() - time}")
                true
            } ?: true
        }
    }
}

class UserListFragment : Fragment() {
    val userViewModel: UserViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProviders.of(this).get(UserViewModel::class.java)
    }
    val swipeListener = object : Function1<Int, Unit> {
        override fun invoke(p1: Int) {
            delUser(p1)
        }
    }

    val onRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        userViewModel.refresh()
    }

    val moreClicker = View.OnClickListener {
        userViewModel.loadMore()
    }

    val itemBinder = object : ItemBinder {
        override fun itemLayout(pos: Int) = if (pos % 2 == 0) R.layout.item_for_user_list else R.layout.item_for_user_list_1
        override fun itemBR(pos: Int) = BR.item
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(LifeObserver(activity!!))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentUserListBinding.inflate(inflater, container, false)
        binding.fragment = this
        binding.setLifecycleOwner(this)
        return binding.root
    }

    fun addUser(view: View) {
        userViewModel.addUser()
        Toast.makeText(view.context, "addUser ~~~", Toast.LENGTH_SHORT).show()
    }

    fun delUser(pos: Int) {
        userViewModel.deleteUser(pos)
        Toast.makeText(activity, "delUser ~~~", Toast.LENGTH_SHORT).show()
    }
}

class LifeObserver(private val context: Context) : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        Toast.makeText(context, "start now ~~~", Toast.LENGTH_SHORT).show()
    }
}

class DiscoverFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_discover, container, false)
    }
}

class MeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_me, container, false)
    }
}


