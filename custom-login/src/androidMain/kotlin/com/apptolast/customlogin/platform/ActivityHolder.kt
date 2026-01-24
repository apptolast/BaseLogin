package com.apptolast.customlogin.platform

import android.app.Activity
import com.apptolast.customlogin.platform.ActivityHolder.clearActivity
import com.apptolast.customlogin.platform.ActivityHolder.setActivity
import java.lang.ref.WeakReference

/**
 * Singleton holder for the current Activity reference.
 * Uses WeakReference to prevent memory leaks.
 *
 * The hosting app must call [setActivity] in Activity.onCreate()
 * and [clearActivity] in Activity.onDestroy().
 */
object ActivityHolder {
    private var activityRef: WeakReference<Activity>? = null

    /**
     * Sets the current activity reference.
     * Call this in your Activity's onCreate().
     *
     * @param activity The current activity.
     */
    fun setActivity(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    /**
     * Clears the activity reference if it matches the provided activity.
     * Call this in your Activity's onDestroy().
     *
     * @param activity The activity being destroyed.
     */
    fun clearActivity(activity: Activity) {
        if (activityRef?.get() === activity) {
            activityRef = null
        }
    }

    /**
     * Gets the current activity, or null if not available.
     *
     * @return The current Activity or null.
     */
    fun getCurrentActivity(): Activity? = activityRef?.get()

    /**
     * Gets the current activity or throws an exception if not available.
     *
     * @return The current Activity.
     * @throws IllegalStateException if no activity is available.
     */
    fun requireActivity(): Activity {
        return getCurrentActivity()
            ?: throw IllegalStateException(
                "No Activity available. Make sure to call ActivityHolder.setActivity() in your Activity's onCreate()."
            )
    }
}
