package com.github.poooower.jetpack

import android.widget.Toast
import androidx.navigation.Navigation

class DoubleBackController {
    private var mBackPressed = false
    private val mResetExitTask: Runnable by lazy(LazyThreadSafetyMode.NONE) {
        Runnable {
            mBackPressed = false
        }
    }

    fun onBackPressed(activity: MainActivity) {
        val controller = Navigation.findNavController(activity, R.id.main_nav_host_fragment)
        if (controller.currentDestination.id == R.id.mainFragment) {
            if (!mBackPressed) {
                mBackPressed = true;
                activity.mHandler.removeCallbacks(mResetExitTask);
                activity.mHandler.postDelayed(mResetExitTask, 1000);
                Toast.makeText(activity, R.string.double_backpress_tip, Toast.LENGTH_SHORT).show();
            } else {
                activity.onSuperBackPressed()
            }
            return
        }
        activity.onSuperBackPressed()
    }
}