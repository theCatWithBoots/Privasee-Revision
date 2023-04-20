package com.example.privasee.ui.initialRun

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.example.privasee.R
import com.example.privasee.database.model.App
import com.example.privasee.database.model.User
import com.example.privasee.database.viewmodel.AppViewModel
import com.example.privasee.database.viewmodel.UserViewModel
import com.example.privasee.databinding.FragmentSetupOwnerBinding
import com.example.privasee.ui.controlAccess.MyAdmin
import com.example.privasee.ui.users.addUser.AddUserCapturePhoto
import com.example.privasee.ui.users.addUser.CaptureReferenceImage
import com.example.privasee.utils.CheckPermissionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class SetupOwnerFragment : Fragment() {

    private var _binding: FragmentSetupOwnerBinding? = null
    private val binding get() = _binding!!

    private lateinit var mUserViewModel: UserViewModel
    private lateinit var mAppViewModel: AppViewModel

    private var job: Job? = null

    private var isAdminPermissionEnabled = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetupOwnerBinding.inflate(inflater, container, false)

        devicePolicyManager = requireActivity().getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        activityManager = requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        compName = ComponentName(requireContext(), MyAdmin::class.java)

        mUserViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        mAppViewModel = ViewModelProvider(this)[AppViewModel::class.java]

        binding.btnSetupOwnerFinish.setOnClickListener {

            val name = binding.etSetName.text.toString()
            val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val isPermissionGranted = CheckPermissionUtils.isPermissionGranted(requireContext())

            // Initialize the Owner information
            if(name.isNotEmpty() && isPermissionGranted && isAdminPermissionEnabled && sp.getBoolean("isEnrolled", false)) {
                val userInfo = User(0, name, isOwner = true)
                mUserViewModel.addUser(userInfo)
                saveInstalledAppsToDB()
                findNavController().navigate(R.id.action_setupOwnerFragment_to_mainActivity)
                requireActivity().finishAffinity()
            } else if(!(name.isNotEmpty())){
                Toast.makeText(requireContext(), "Please input your name", Toast.LENGTH_SHORT).show()
            } else if(!isPermissionGranted) {
                Toast.makeText(requireContext(), "Please enable Accessibility Service settings first before proceeding", Toast.LENGTH_SHORT).show()
            } else if (!isAdminPermissionEnabled) {
                Toast.makeText(requireContext(), "Please enable Admin Permission", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please enroll your face", Toast.LENGTH_SHORT).show()
            }

        }

        binding.btnEnableAccessibilityService.setOnClickListener {
            CheckPermissionUtils.openAccessibilityServiceSettings(requireContext())
        }

        binding.btnOwnerRegisterFace.setOnClickListener {
            val intent = Intent(requireContext(), CaptureReferenceImage::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        binding.btnAdminPermission.setOnClickListener {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
            intent.putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Additional text explaining why we need this permission"
            )
            startActivityForResult(intent, RESULT_ENABLE)
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RESULT_ENABLE -> if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(
                    requireContext(),
                    "You have enabled the Admin Device features",
                    Toast.LENGTH_SHORT
                ).show()
                isAdminPermissionEnabled = true

            } else {
                Toast.makeText(
                    requireContext(),
                    "Problem to enable the Admin Device features",
                    Toast.LENGTH_SHORT
                ).show()
                isAdminPermissionEnabled = false
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        const val RESULT_ENABLE = 69420
        var devicePolicyManager: DevicePolicyManager? = null
        private var activityManager: ActivityManager? = null
        private var compName: ComponentName? = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
        _binding = null
    }

    private fun saveInstalledAppsToDB() {
        mAppViewModel = ViewModelProvider(this)[AppViewModel::class.java]
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val packageManager = requireContext().packageManager
        val resolveInfoList = packageManager?.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        // Check for duplicated apps existing in the database
        if (resolveInfoList != null) {

            lifecycleScope.launch(Dispatchers.IO) {
                val appsInDb = mAppViewModel.getAllData()
                for (resolveInfo in resolveInfoList) {

                    val packageName = resolveInfo.activityInfo.packageName
                    val appName = packageManager.getApplicationLabel(resolveInfo.activityInfo.applicationInfo).toString()

                    var isDuplicate = false

                    if (appsInDb.isNotEmpty()) {
                        for(app in appsInDb) {
                            if(app.appName == appName) {
                                isDuplicate = true
                                break
                            }
                        }
                    }

                    if(!isDuplicate) {
                        val appInfo = App(packageName = packageName, appName = appName)
                        mAppViewModel.addApp(appInfo)
                    }
                }

            }
        }
    }
}