package com.example.proyectopenka

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.example.proyectopenka.data.Api
import com.example.proyectopenka.R

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Api.init(this)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            private val drawables = mutableMapOf<Activity, ScrollingBackgroundDrawable>()

            override fun onActivityCreated(a: Activity, s: Bundle?) {
                a.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, R.anim.fade_in, R.anim.fade_out)
                a.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, R.anim.fade_in, R.anim.fade_out)
            }

            override fun onActivityStarted(a: Activity) {}

            override fun onActivityResumed(activity: Activity) {
                val bg = drawables.getOrPut(activity) { ScrollingBackgroundDrawable(this@App) }
                activity.window.setBackgroundDrawable(bg)
                bg.start()
            }

            override fun onActivityPaused(activity: Activity) {
                drawables[activity]?.stop()
            }

            override fun onActivityStopped(a: Activity) {}

            override fun onActivitySaveInstanceState(a: Activity, s: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {
                drawables.remove(activity)
            }
        })
    }
}
