package com.example.privasee.ui.users.userInfoUpdate.userAppControl


import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.privasee.R
import com.example.privasee.database.model.App
import com.example.privasee.database.model.Restriction
import com.example.privasee.database.viewmodel.AppViewModel
import com.example.privasee.database.viewmodel.RestrictionViewModel
import com.example.privasee.databinding.ActivityUserAppControllingBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class UserAppControllingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserAppControllingBinding
    private lateinit var navController: NavController

    private lateinit var mRestrictionViewModel: RestrictionViewModel
    private lateinit var mAppViewModel: AppViewModel

    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        binding = ActivityUserAppControllingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fcvControlling) as NavHostFragment
        navController = navHostFragment.navController
//        setupActionBarWithNavController(navController)

        val userId = intent.extras?.getInt("userId")
        val bundle = Bundle()

        if (userId != null) {
            bundle.putInt("userId", userId)
            initializeRestrictionList(userId)
            updateInstalledApps(userId)
        }

        navController.setGraph(R.navigation.controlling_nav, bundle)
    }


    private fun initializeRestrictionList(userId: Int) {
        mRestrictionViewModel = ViewModelProvider(this)[RestrictionViewModel::class.java]
        mAppViewModel = ViewModelProvider(this)[AppViewModel::class.java]

        // Initializes restriction list for this user
        job = lifecycleScope.launch(Dispatchers.IO) {
            val userRestrictionCount = mRestrictionViewModel.getUserRestrictionCount(userId)
            if (userRestrictionCount < 1) {
                val appList = mAppViewModel.getAllData()
                for (app in appList) {
                    val restriction = Restriction(0, app.appName, monitored = false, controlled = false, userId)
                    mRestrictionViewModel.addRestriction(restriction)
                }
            }
        }
    }

    private fun updateInstalledApps(userId: Int) {
        mAppViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val packageManager = this.packageManager
        val resolveInfoList = packageManager?.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        // Check for newly installed/uninstalled apps
        if (resolveInfoList != null) {

            job = lifecycleScope.launch(Dispatchers.IO) {
                val appsInDb = mAppViewModel.getAllAppName()
                val currentlyInstalledAppName: MutableList<String> = mutableListOf()
                val currentlyInstalledAppPackageName: MutableList<String> = mutableListOf()

                // Check if apps in the system does not exist in the database
                for (resolveInfo in resolveInfoList) {
                    val packageName = resolveInfo.activityInfo.packageName
                    val appName = packageManager.getApplicationLabel(resolveInfo.activityInfo.applicationInfo).toString()
                    currentlyInstalledAppPackageName.add(packageName)
                    currentlyInstalledAppName.add(appName)
                    if(!appsInDb.contains(appName)) { // Add new app
                        mAppViewModel.addApp(App(packageName = packageName, appName = appName))
                        mRestrictionViewModel.addRestriction(Restriction(appName = appName, userId = userId))
                    }
                }

                // Check if apps in the database does not exist in the system
                for(appName in appsInDb) {
                    if(!currentlyInstalledAppName.contains(appName)) {
                        val appToDelete = mAppViewModel.getAppData(appName)
                        mAppViewModel.deleteApp(appToDelete)
                        mRestrictionViewModel.deleteRestriction(Restriction(appName = appName, userId = userId))
                    }
                }

            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
        finish()
    }




}