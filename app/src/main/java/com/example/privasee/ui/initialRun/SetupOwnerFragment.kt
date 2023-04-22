package com.example.privasee.ui.initialRun

import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.Dialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.example.privasee.Constants.TAG
import com.example.privasee.R
import com.example.privasee.database.model.App
import com.example.privasee.database.model.User
import com.example.privasee.database.viewmodel.AppViewModel
import com.example.privasee.database.viewmodel.UserViewModel
import com.example.privasee.databinding.FragmentSetupOwnerBinding
import com.example.privasee.ui.controlAccess.MyAdmin
import com.example.privasee.ui.users.addUser.AddUserCapturePhoto
import com.example.privasee.utils.CheckPermissionUtils
import kotlinx.android.synthetic.main.activity_capture_reference_image.*
import kotlinx.android.synthetic.main.capture_reference_instructions.*
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

    private val STORAGE_PERMISSION_CODE = 100

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

        binding.StoragePermission.setOnClickListener {
                requestStoragePermission()
        }

        binding.btnOwnerRegisterFace.setOnClickListener {
           if(checkPermission()){
               val dialogBinding = layoutInflater.inflate(R.layout.capture_reference_instructions, null)

               val myDialog = Dialog(requireContext())
               myDialog.setContentView(dialogBinding)

               myDialog.setCancelable(false)
               myDialog.imgInstruction.setImageResource(R.drawable.front)
               myDialog.instruction.text = "Position your face in the front."
               //myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
               myDialog.show()

               myDialog.btnOk.setOnClickListener {
                   myDialog.dismiss()
                   val intent = Intent(requireContext(), AddUserCapturePhoto::class.java)
                   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                   startActivity(intent)
               }
           }else{
               Toast.makeText(
                   requireActivity(), "You need to enable storage permission first.",
                   Toast.LENGTH_SHORT
               ).show()
           }
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

    private fun requestStoragePermission() {
       if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
           //Android 11 or above
           try{
               Log.d(TAG, "requestPermission: Try")
               val intent  = Intent()
               intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
               val uri = Uri.fromParts("package", requireContext().packageName, null)
               intent.data = uri
               storageActivityResultLauncher.launch(intent)

           }catch (e: Exception){
               Log.d(TAG, "requestPermission: ", e)
               val intent = Intent()
               intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
               storageActivityResultLauncher.launch(intent)
           }

       }else{
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE),STORAGE_PERMISSION_CODE)
       }
    }

    private val storageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        Log.d(TAG, "storageActivityResultLauncher")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            if(Environment.isExternalStorageManager()){
                Log.d(TAG, "storageActivityResultLauncher: ")
            }else{
                Log.d(TAG, "storageActivityResultLauncher: Manage External Permission Denied")
            }
        }else{
            //android is below 11
        }
    }

    private fun checkPermission(): Boolean{
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            Environment.isExternalStorageManager()
        }else{
            val write = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty()){
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if(write && read){
                    //success
                }else{
                    //denied
                }
            }
        }
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