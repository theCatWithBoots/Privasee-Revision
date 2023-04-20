package com.example.privasee.ui.monitor

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.example.privasee.Constants
import com.example.privasee.R
import com.example.privasee.databinding.FragmentMonitorBinding
import kotlinx.android.synthetic.main.fragment_monitor.*


class MonitorFragment : Fragment() {

    private var _binding: FragmentMonitorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMonitorBinding.inflate(inflater, container, false)

        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
               val eBuilder = AlertDialog.Builder(requireContext())

                eBuilder.setTitle("Exit")

                eBuilder.setIcon(R.drawable.ic_action_name)

                eBuilder.setMessage("Are you sure you want to Exit?")
                eBuilder.setPositiveButton("Yes"){
                    Dialog, which ->
                    activity?.finish()
                }
                eBuilder.setNegativeButton("No"){
                    Dialog,which->

                }

                val createBuild = eBuilder.create()
                createBuild.show()

            }
        }

        requireActivity().onBackPressedDispatcher.addCallback (callback)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        btnAccessRecords.setOnClickListener {
            findNavController().navigate(R.id.action_monitorFragment_to_AccessRecords)
        }

        btnEnableCamera.setOnClickListener {
            checkForPermissions(android.Manifest.permission.CAMERA, "Camera", Constants.REQUEST_CODE_PERMISSIONS)
        }

        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        editThreshold.inputType = InputType.TYPE_CLASS_NUMBER
        editThreshold.setText(sp.getInt("threshold", 8000).toString())

       setSnapshotThreshold.setOnClickListener {

           val threshold = editThreshold.text.toString()

           val editor = sp.edit()

           if(threshold.isNotEmpty()){

               editor.apply(){
                   putInt("threshold", threshold.toInt())
               }.apply()
                val thresholdText = "Current Threshold is $threshold"
               Toast.makeText(requireContext(), "$thresholdText", Toast.LENGTH_SHORT).show()
        //       setThreshold.text = thresholdText

           }else{
               val thresholdText = "Please Input Threshold"
            //   setThreshold.text = thresholdText
               Toast.makeText(requireContext(), "$thresholdText", Toast.LENGTH_SHORT).show()
           }
       }

    }
    private fun checkForPermissions(permission: String, name: String, requestCode: Int){ //if not granted, it asks for permission
        when {

            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                Toast.makeText(requireContext(), "$name permission granted", Toast.LENGTH_SHORT).show()
            }
            shouldShowRequestPermissionRationale(permission) -> showDialog(permission, name, requestCode) //explains why permission is needed after they rejected it the first time

            else -> {
                goToSettings()
            }
        }
    }

    private fun goToSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        requireContext().startActivity(intent)
    }

    private fun showDialog (permission: String, name: String, requestCode: Int){
        val builder = AlertDialog.Builder(requireContext())

        builder.apply {
            setMessage("Permission to access your $name is required to use this app. If you deny this again, you will have to manually add permission via settings.")
            setTitle("Permission required")
            setPositiveButton("ok") { dialog, which ->
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), requestCode)
            }
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

}