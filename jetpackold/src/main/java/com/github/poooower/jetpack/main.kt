package com.github.poooower.jetpack

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation.findNavController
import com.github.poooower.jetpack.databinding.FragmentDatabindingBinding

class MainActivity : AppCompatActivity() {

    private val mDoubleBack = DoubleBackController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onBackPressed() {
        mDoubleBack.onBackPressed(this)
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

class DataBindingFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentDatabindingBinding.inflate(inflater, container, false)
        binding.user = User("First", "Last")
        return binding.root
    }

}

class LifecycleFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_lifecycle, container, false)
    }

}
