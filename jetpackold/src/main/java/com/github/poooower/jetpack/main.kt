package com.github.poooower.jetpack

import android.arch.lifecycle.*
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import com.github.poooower.jetpack.databinding.FragmentUserListBinding
import me.tatarka.bindingcollectionadapter2.ItemBinding

class DoubleBackController {
    private var backPressed = false
    private val resetExitTask: Runnable by lazy(LazyThreadSafetyMode.NONE) { Runnable { backPressed = false } }
    private val handler by lazy { Handler(Looper.getMainLooper()) }

    fun onBackPressed(activity: MainActivity) {
        val controller = Navigation.findNavController(activity, R.id.main_nav_host_fragment)
        if (controller.currentDestination.id == R.id.mainFragment) {
            if (!backPressed) {
                backPressed = true;
                handler.removeCallbacks(resetExitTask);
                handler.postDelayed(resetExitTask, 1000);
                Toast.makeText(activity, R.string.double_backpress_tip, Toast.LENGTH_SHORT).show();
            } else {
                activity.onSuperBackPressed()
            }
            return
        }
        activity.onSuperBackPressed()
    }
}

class MainActivity : AppCompatActivity() {

    private val doubleBack = DoubleBackController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onBackPressed() {
        doubleBack.onBackPressed(this)
    }

    fun onSuperBackPressed() {
        super.onBackPressed()
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
    @Suppress("MemberVisibilityCanBePrivate")
    lateinit var userViewModel: UserViewModel
    @Suppress("MemberVisibilityCanBePrivate")
    val itemBinding: ItemBinding<User> = ItemBinding.of { itemBinding, position, _ ->
        if (position % 2 == 0) {
            itemBinding.set(BR.item, R.layout.item_for_user_list)
        } else {
            itemBinding.set(BR.item, R.layout.item_for_user_list_1)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(LifeObserver(activity!!))

        userViewModel = ViewModelProviders.of(this).get(UserViewModel::class.java)
        userViewModel.init()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentUserListBinding.inflate(inflater, container, false)
        binding.let {
            it.fragment = this
            it.itemBinding = itemBinding
            it.userViewModel = userViewModel
            it.setLifecycleOwner(this)
        }
        return binding.root
    }

    fun addUser(view: View) {
        userViewModel.addUser()
        Toast.makeText(view.context, "addUser ~~~", Toast.LENGTH_SHORT).show()
    }

    fun delUser() {
        userViewModel.deleteUser()
        Toast.makeText(activity, "delUser ~~~", Toast.LENGTH_SHORT).show()
    }
}

class LifeObserver(private val context: Context) : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        Toast.makeText(context, "start now ~~~", Toast.LENGTH_SHORT).show()
    }
}


