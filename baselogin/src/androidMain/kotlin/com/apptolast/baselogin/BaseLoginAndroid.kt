package com.apptolast.baselogin

import android.app.Activity
import android.content.Context
import com.apptolast.baselogin.platform.ActivityHolder

/**
 * Android integration helper for platform dependencies required by social and phone auth.
 *
 * Call [initialize] from `Application.onCreate()` and [attachActivity]/[detachActivity] from the
 * activity that can launch Google, OAuth, and phone auth flows.
 */
object BaseLoginAndroid {

    fun initialize(context: Context, activity: Activity? = null) {
        setApplicationContext(context)
        activity?.let(::attachActivity)
    }

    fun setApplicationContext(context: Context) {
        appContext = context.applicationContext
    }

    fun attachActivity(activity: Activity) {
        ActivityHolder.setActivity(activity)
    }

    fun detachActivity(activity: Activity) {
        ActivityHolder.clearActivity(activity)
    }
}
