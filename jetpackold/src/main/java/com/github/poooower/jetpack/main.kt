package com.github.poooower.jetpack

import android.app.Application
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.databinding.DataBindingUtil
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
import com.github.poooower.common.ItemBinder
import com.github.poooower.common.app
import com.github.poooower.jetpack.databinding.ActivityMainBinding
import com.github.poooower.jetpack.databinding.FragmentUserListBinding
import java.text.SimpleDateFormat
import java.util.*


class App : Application() {
    override fun onCreate() {
        super.onCreate()
        app = this
    }
}

class MainActivity : AppCompatActivity() {

    val fragments: Array<Class<out Fragment>> = arrayOf(MainFragment::class.java, DiscoverFragment::class.java, MeFragment::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        binding.mainActivity = this
        setSupportActionBar(findViewById(R.id.toolbar))
    }
}

class MainFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_main, rootKey)
        for (i in 0 until preferenceScreen.preferenceCount) {
            val preference = preferenceScreen.getPreference(i)
            preference.extras.putInt("actionId", preference.widgetLayoutResource)
            preference.widgetLayoutResource = 0
        }
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        val actionId = preference?.extras?.getInt("actionId", 0)
        actionId?.let {
            if (actionId != 0) {
                findNavController(this.view!!).navigate(actionId)
                return true
            }
        }
        return super.onPreferenceTreeClick(preference)
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
    var timeStr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        timeStr = savedInstanceState?.getString("time") ?: SimpleDateFormat("yyyy-MM-dd hh:mm::ss").format(Calendar.getInstance().time)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        println("DiscoverFragment XXXXX$timeStr")
        return inflater.inflate(R.layout.fragment_discover, container, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("time",timeStr )
    }
}

class MeFragment : Fragment() {
    var timeStr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        timeStr = savedInstanceState?.getString("time") ?: SimpleDateFormat("yyyy-MM-dd hh:mm::ss").format(Calendar.getInstance().time)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        println("MeFragment XXXXX$timeStr")
        return inflater.inflate(R.layout.fragment_me, container, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("time",timeStr )
    }
}


