package com.example.privasee.ui.controlAccess

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Color
import android.os.CountDownTimer
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.example.privasee.R
import java.util.concurrent.TimeUnit


class MyForegroundServices :  LifecycleService() {

    private var screenTimer: Long? = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        screenTimer = intent?.getLongExtra("screenTimer", 1000)
        startTimer()

        val channelId = "Foreground Service ID"
        val channelName = "Privasee is Running"

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )

        channel.apply{
            lightColor = Color.BLUE
            importance = NotificationManager.IMPORTANCE_HIGH
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }

        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val notification = Notification.Builder(this, channelId)
            .setContentText("Service is Running....")
            .setContentTitle("Privasee")
            .setSmallIcon(R.drawable.icon)
        startForeground(1001, notification.build())
        return super.onStartCommand(intent, flags, startId)

    }

    private fun startTimer() {
        var mCountDownTimer: CountDownTimer? = null
        var mTimeLeftInMillis : Long = TimeUnit.MINUTES.toMillis(screenTimer!!)

        mCountDownTimer = object : CountDownTimer(mTimeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                val sp = PreferenceManager.getDefaultSharedPreferences(this@MyForegroundServices)

                if((sp.getBoolean("IS_ACTIVITY_RUNNING", false))){ //continue broadcast
                    intent.putExtra("countdown",millisUntilFinished)
                    sendBroadcastMessage(intent)
                } else
                    mCountDownTimer?.cancel() //stop timer
            }

            override fun onFinish() {
                val sp = PreferenceManager.getDefaultSharedPreferences(this@MyForegroundServices)
                val editor = sp.edit()

                // Put false to enable app locking
                editor.apply() {
                    putBoolean("IS_ACTIVITY_RUNNING", false)
                }.apply()

                ControlAccessFragmentScreenTimeLimit.devicePolicyManager!!.lockNow()
            }
        }.start()
    }

    private fun sendBroadcastMessage(intent: Intent) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
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