package com.example.privasee

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.example.privasee.database.viewmodel.AppViewModel
import com.example.privasee.database.viewmodel.RestrictionViewModel
import com.example.privasee.database.viewmodel.UserViewModel
import com.example.privasee.ui.controlAccess.AppLockTimer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.ArrayList
import kotlin.concurrent.timer

class BootCompletedReceiver: BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == Intent.ACTION_BOOT_COMPLETED ||  intent?.action == Intent.ACTION_USER_PRESENT) {
            val appContext = context!!.applicationContext

            val sp = PreferenceManager.getDefaultSharedPreferences(appContext)
            val timerLeft = sp.getLong("timerLeftForAppBlock", 0)

            Toast.makeText(appContext, "Privasee Started. $timerLeft", Toast.LENGTH_LONG).show()

            if( timerLeft > 0){

                val sp = PreferenceManager.getDefaultSharedPreferences(appContext)
                val editor = sp.edit()

                val jsonString = sp.getString("CurrentControlledApps","")

                // Convert the string back to a JSONArray
                val jsonArray = JSONArray(jsonString)

                // Convert the JSONArray to an array of strings
                val arrayOfControlledApps = Array(jsonArray.length()) { i -> jsonArray.getString(i) }
                val packageNames = ArrayList(arrayOfControlledApps.toList())

                editor.apply() {
                    putBoolean("IS_APPLOCK_TIMER_RUNNING", true)
                }.apply()

                editor.apply() {
                    putBoolean("IS_TIMER_RUNNING", false)
                }.apply()

                Toast.makeText(
                    appContext,
                    "App Block has started",
                    Toast.LENGTH_SHORT
                ).show()

                // Add Lock
                val intent = Intent(appContext, AppAccessService::class.java)
                intent.putExtra("action", "addLock")
                intent.putStringArrayListExtra("packageNames", ArrayList(packageNames))
                appContext.startService(intent)

                appContext.startService(
                    Intent(
                        context,
                        AppLockTimer::class.java
                    ).putExtra("Timer",timerLeft.toString())
                        .putStringArrayListExtra("controlledAppPackageNames", packageNames)
                )
            }
        }
    }
}
