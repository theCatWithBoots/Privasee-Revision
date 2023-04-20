package com.example.privasee

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Bitmap
import android.util.Base64
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import com.example.privasee.database.model.Record
import com.example.privasee.database.viewmodel.AppViewModel
import com.example.privasee.database.viewmodel.RecordViewModel
import com.example.privasee.database.viewmodel.RestrictionViewModel
import com.example.privasee.database.viewmodel.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.*

class DbQueryIntentService : IntentService("TestIntentService") {

    private lateinit var mRecordViewModel: RecordViewModel
    private lateinit var mRestrictionViewModel: RestrictionViewModel
    private lateinit var mUserViewModel: UserViewModel
    private lateinit var mAppViewModel: AppViewModel

    private val ioScope = CoroutineScope(Dispatchers.IO)

    override fun onHandleIntent(intent: Intent?) {

        // Initialize foreground task
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID) // No notifs
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
        // Start foreground
        startForeground(NOTIFICATION_ID, notification)

        // Take the intent extra 'query'
        val query = intent?.getStringExtra("query")

        if(query == "insertRecord") {

            val calendar = Calendar.getInstance()
            calendar.time = Date()
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH) + 1 // month is 0 based so + 1
            val year = calendar.get(Calendar.YEAR)
            val time = calendar.timeInMillis

            val appName = intent.getStringExtra("appName")
            val fileLocation = intent.getStringExtra("image").toString() //file location

            mRecordViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(RecordViewModel::class.java)
            val record = appName?.let { Record(0, day, month, year, time, fileLocation, it) }
            if (record != null)
                mRecordViewModel.addRecord(record)
        }

        // This is used for initializing the monitored app in the app access service
        // every phone reboot
        // Or when the app has been force closed
        if(query == "getMonitoredApps") {
            mRestrictionViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(RestrictionViewModel::class.java)
            mAppViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(AppViewModel::class.java)
            mUserViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(UserViewModel::class.java)

            ioScope.launch {
                val ownerId = mUserViewModel.getOwnerId()
                val restrictionList = mRestrictionViewModel.getAllMonitoredAppList(ownerId)
                val monitoredList: MutableList<String> = mutableListOf()
                for(restriction in restrictionList) {
                    val packageName = mAppViewModel.getPackageName(restriction.appName)
                    monitoredList.add(packageName)
                }

                val intent = Intent(this@DbQueryIntentService, AppAccessService::class.java)
                intent.putExtra("action", "addMonitor" )
                intent.putStringArrayListExtra("packageNames", ArrayList(monitoredList))
                this@DbQueryIntentService.startService(intent)
            }
        }

        stopSelf() // Staph
    }


    companion object {
        private const val CHANNEL_ID = "ServiceChannel"
        private const val NOTIFICATION_ID = 1
    }
}