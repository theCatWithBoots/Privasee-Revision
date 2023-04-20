package com.example.privasee.ui.initialRun

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.privasee.R
import com.example.privasee.databinding.ActivitySetupBinding

class SetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupBinding
    private lateinit var setupNavController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

//         Make this activity the nav host fragment for the navgraph for the initial run fragments
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fcvSetup) as NavHostFragment
        setupNavController = navHostFragment.navController

        // Light theme for no reason does not have an action bar set.
//        setupActionBarWithNavController(setupNavController)

    }

    // Enable action bar's back button
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fcvSetup)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    // Disable back pressed
    override fun onBackPressed() {}
}
