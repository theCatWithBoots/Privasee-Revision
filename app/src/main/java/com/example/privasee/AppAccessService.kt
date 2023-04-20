package com.example.privasee

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import com.example.privasee.ui.monitor.MonitorService
import com.example.privasee.ui.users.userInfoUpdate.userAppControl.applock.BlockScreen


class AppAccessService : AccessibilityService() {

    private var packageNames: MutableList<String> = mutableListOf()
    private var controlledApps: MutableList<String> = mutableListOf()
    private var previousPackageName = "initial"

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if(event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            val currentlyOpenedApp = event.packageName.toString()

            val pm = applicationContext.packageManager
            val appInfo = pm.getPackageInfo(currentlyOpenedApp, PackageManager.GET_META_DATA)
            val appName = appInfo.applicationInfo.loadLabel(pm).toString()

            if(packageNames.size > 0) {
                // Triggering only once, for repeated opens
                if (previousPackageName == currentlyOpenedApp) {
                    previousPackageName = currentlyOpenedApp
                } else {
                    for(packageName in packageNames){
                        if(packageName == currentlyOpenedApp){
                            previousPackageName = currentlyOpenedApp
                            // start intent service, start verifying etc
                            val intent = Intent(this, MonitorService::class.java)
                            intent.putExtra("appName", appName)
                            startService(intent)
                        }
                    }
                }
            }

            if(controlledApps.size > 0) {
                for(packageName in controlledApps) {
                    if(packageName == currentlyOpenedApp) {
                        val intent = Intent(this, BlockScreen::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                }
            }
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val metadata = AccessibilityServiceInfo()
        metadata.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED

        val action = intent?.getStringExtra("action")
        val packageNames = intent?.getStringArrayListExtra("packageNames")

        if (packageNames != null) {

            if(action == "addMonitor") {
                for(packageName in packageNames)
                    this.packageNames.add(packageName)
            }

            if (action == "removeMonitor") {
                for(packageName in packageNames)
                    this.packageNames.remove(packageName)
            }

            if (action == "addLock") {
                for(packageName in packageNames) {
                    this.controlledApps.add(packageName)
                }
            }

            if (action == "removeLock") {
                for(packageName in packageNames) {
                    this.controlledApps.remove(packageName)
                }
            }

            metadata.packageNames = packageNames.toTypedArray()
            serviceInfo = metadata
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onInterrupt() {
        TODO("Not yet implemented")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val intent = Intent(this, DbQueryIntentService::class.java)
        intent.putExtra("query", "getMonitoredApps")
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        ContextCompat.startForegroundService(this, intent)
    }

}