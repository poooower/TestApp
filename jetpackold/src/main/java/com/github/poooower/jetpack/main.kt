package com.github.poooower.jetpack

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation.findNavController
import com.github.poooower.jetpack.databinding.FragmentUserListBinding


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
    val itemBR = BR.item

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


