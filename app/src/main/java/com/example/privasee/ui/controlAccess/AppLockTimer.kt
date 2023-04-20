package com.example.privasee.ui.controlAccess

import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.example.privasee.AppAccessService
import java.util.concurrent.TimeUnit


class AppLockTimer :  LifecycleService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val timer = (intent?.getStringExtra("Timer"))?.toInt()
        val packageNames = (intent?.getStringArrayListExtra("controlledAppPackageNames"))

        val sp = PreferenceManager.getDefaultSharedPreferences(this@AppLockTimer)
        val editor = sp.edit()

        if(!(sp.getBoolean("IS_TIMER_RUNNING", false))) {

            editor.apply() {
                putBoolean("IS_TIMER_RUNNING", true)
            }.apply()

            startTimer(timer!!, packageNames)
        }


        return super.onStartCommand(intent, flags, startId)
    }

    private fun startTimer(timer: Int, packageNames: ArrayList<String>?) {

        val startTimeInMillis = timer.toLong() //sp.getLong("theTime", 0)
        var mCountDownTimer: CountDownTimer? = null
        var mTimeLeftInMillis : Long = TimeUnit.MINUTES.toMillis(startTimeInMillis!!)

        mCountDownTimer = object : CountDownTimer(mTimeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                val sp = PreferenceManager.getDefaultSharedPreferences(this@AppLockTimer)
                val editor = sp.edit()

                if(sp.getBoolean("IS_APPLOCK_TIMER_RUNNING", false)){
                    intent.putExtra("countdown",millisUntilFinished)
                    sendBroadcastMessage(intent)
                }else{
                    editor.apply() {
                        putBoolean("IS_TIMER_RUNNING", false)
                    }.apply()

                    mCountDownTimer?.cancel() //stop timer
                }

            }

            override fun onFinish() {

                val sp = PreferenceManager.getDefaultSharedPreferences(this@AppLockTimer)
                val editor = sp.edit()

                // Make the applock timer false
                editor.apply() {
                    putBoolean("IS_TIMER_RUNNING", false)
                }.apply()

                // To make the screen time limit accessible
                editor.apply() {
                    putBoolean("IS_APPLOCK_TIMER_RUNNING", false)
                }.apply()

                // Remove lock from controlled app lists
                val intent = Intent(this@AppLockTimer, AppAccessService::class.java)
                intent.putStringArrayListExtra("removeLock", packageNames)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                this@AppLockTimer.startService(intent)

                // Close the screen
                ControlAccessFragmentScreenAppLock.devicePolicyManager!!.lockNow()
            }

        }.start()

    }

    private fun sendBroadcastMessage(intent: Intent) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onDestroy() {
        this.stopSelf()
        super.onDestroy()

    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    companion object {
        val COUNTDOWN_BR = "com.example.privasee.ui.monitor"
        var intent = Intent(COUNTDOWN_BR)
    }

}