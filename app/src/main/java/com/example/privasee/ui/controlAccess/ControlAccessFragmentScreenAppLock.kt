package com.example.privasee.ui.controlAccess

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.example.privasee.AppAccessService
import com.example.privasee.R
import com.example.privasee.database.model.User
import com.example.privasee.database.viewmodel.AppViewModel
import com.example.privasee.database.viewmodel.RestrictionViewModel
import com.example.privasee.database.viewmodel.UserViewModel
import com.example.privasee.databinding.FragmentControlAccessApplockBinding
import com.example.privasee.utils.CheckPermissionUtils
import kotlinx.android.synthetic.main.fragment_control_access_applock.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class ControlAccessFragmentScreenAppLock : Fragment() {

    private var _binding: FragmentControlAccessApplockBinding? = null
    private val binding get() = _binding!!

    private lateinit var mUserViewModel: UserViewModel
    private lateinit var mRestrictionViewModel: RestrictionViewModel
    private lateinit var mAppViewModel: AppViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        devicePolicyManager = requireActivity().getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        _binding = FragmentControlAccessApplockBinding.inflate(inflater, container, false)

        mUserViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        mRestrictionViewModel = ViewModelProvider(this)[RestrictionViewModel::class.java]
        mAppViewModel = ViewModelProvider(this)[AppViewModel::class.java]

        lifecycleScope.launch(Dispatchers.Main) {

            mUserViewModel.getAllNonOwner.observe(viewLifecycleOwner) {

                val spinner = binding.root.findViewById<Spinner>(R.id.spinnerUsers)

                // Show all user using names.
                val spinnerAdapter = object : ArrayAdapter<User>(requireContext(), R.layout.spinner_item_user, it) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getView(position, convertView, parent)
                        val user = getItem(position)
                        if (user != null) {
                            (view.findViewById<TextView>(android.R.id.text1)).text = user.name
                            (view.findViewById<TextView>(android.R.id.text1)).setTextColor(Color.parseColor("#989898"))
                        }
                        return view
                    }

                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {

                        val view = if (convertView == null) {
                            val inflater = LayoutInflater.from(context)
                            inflater.inflate(R.layout.spinner_item_user, parent, false)
                        } else
                            convertView

                        val user = getItem(position)

                        if (user != null) {
                            (view.findViewById<TextView>(android.R.id.text1)).text = user.name
                            (view.findViewById<TextView>(android.R.id.text1)).setTextColor(Color.parseColor("#989898"))
                        }
                        return view
                    }
                }

                spinner.adapter = spinnerAdapter
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                        val selectedUser = parent.getItemAtPosition(position) as User

                        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
                        val editor = sp.edit()
                        editor.apply(){
                            putInt("CurrentUser", selectedUser.id)
                        }.apply()

                        lifecycleScope.launch(Dispatchers.Main) {

                            mRestrictionViewModel.getAllControlledApps(selectedUser.id).observe(viewLifecycleOwner) { controlledList ->

                                // Take package names
                                lifecycleScope.launch(Dispatchers.IO) {
                                    val controlledAppPackageNames: MutableList<String> = mutableListOf()
                                    for(restrictedApp in controlledList) {
                                        // val appId = restrictedApp.packageId
                                        val packageName = mAppViewModel.getPackageName(restrictedApp.appName)
                                        controlledAppPackageNames.add(packageName)
                                    }

                                    // Set on click listener for adding or removing app lock
                                    if (controlledAppPackageNames.size > 0) {

                                        binding.btnStartAppBlock.setOnClickListener {

                                            val timerString = timeButton2.text.toString()

                                            if (!( timerString.equals("time limit", ignoreCase = true))){
                                                val units = timerString.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                                                    .toTypedArray() //will break the string up into an array
                                                val hour = units[0].toInt() //first element
                                                val minutes = units[1].toInt() //second element
                                                val duration = 60 * hour + minutes //add up our values
                                                val timerInt = duration.toLong()

                                                val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
                                                val editor = sp.edit()

                                                editor.apply() {
                                                    putBoolean("IS_APPLOCK_TIMER_RUNNING", true)
                                                }.apply()

                                                Toast.makeText(
                                                    requireContext(),
                                                    "App Block has started",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                                // Add Lock
                                                val intent = Intent(requireContext(), AppAccessService::class.java)
                                                intent.putExtra("action", "addLock")
                                                intent.putStringArrayListExtra("packageNames", ArrayList(controlledAppPackageNames))
                                                requireContext().startService(intent)

                                                activity!!.startService(
                                                    Intent(
                                                        context,
                                                        AppLockTimer::class.java
                                                    ).putExtra("Timer",timerInt.toString())
                                                        .putStringArrayListExtra("controlledAppPackageNames", controlledAppPackageNames.toArrayList())
                                                )

                                            } else {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "You have to set Timer first",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }

                                        }

                                        binding.btnStopAppBlock.setOnClickListener {
                                            val stringTimeNotSet = "Timer is not set"
                                            remainingTime2.text = stringTimeNotSet

                                            Toast.makeText(
                                                requireContext(),
                                                "App Blocker has been Stopped",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
                                            val editor = sp.edit()

                                            editor.apply() {
                                                putBoolean("IS_APPLOCK_TIMER_RUNNING", false)
                                            }.apply()

                                            activity!!.stopService(
                                                Intent(
                                                    context,
                                                    AppLockTimer::class.java
                                                ))

                                            val intent = Intent(requireContext(), AppAccessService::class.java)
                                            intent.putExtra("action", "removeLock")
                                            intent.putStringArrayListExtra("packageNames", ArrayList(controlledAppPackageNames))
                                            requireContext().startService(intent)
                                        }

                                    } else {

                                        binding.btnStartAppBlock.setOnClickListener {
                                            Toast.makeText(
                                                requireContext(),
                                                "Please Select Apps to Block First",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        binding.btnStopAppBlock.setOnClickListener {
                                            Toast.makeText(
                                                requireContext(),
                                                "Please Select Apps to Block First",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }

                        }
                    }


                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // Do wNothing
                    }
                }

            }
        }

        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_appLock_to_controlAccessFragment)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback (callback)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        accessibility.setOnClickListener {
            CheckPermissionUtils.openAccessibilityServiceSettings(requireContext())
        }

        timeButton2.setOnClickListener {
            popTimePicker()
        }

    }

    fun <T> List<T>.toArrayList(): ArrayList<T>{
        return ArrayList(this)
    }

    fun popTimePicker() {
        // var timeButton: Button? = null
        var hour = 0
        var minute = 0

        val onTimeSetListener =
            OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                hour = selectedHour
                minute = selectedMinute
                timeButton2!!.text =
                    kotlin.String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            }

        // int style = AlertDialog.THEME_HOLO_DARK;
        val timePickerDialog =
            TimePickerDialog(requireContext(),  /*style,*/onTimeSetListener, hour, minute, true)
        timePickerDialog.setTitle("Select Time")

        timePickerDialog.show()
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //Update GUI
            updateGUI(intent)
        }
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
    }

    override fun onStop() {
        try {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)

        } catch (e: Exception) {
            // Receiver was probably already
        }
        super.onStop()
    }

    private fun updateGUI(intent: Intent) {
        if (intent.extras != null) {

            val millisUntilFinished = intent.getLongExtra("countdown", 30000)
            var seconds = (millisUntilFinished/1000)
            var minutes = (seconds/60)
            val hours = (minutes/60)
            seconds %= 60
            minutes %= 60

            remainingTime2.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds))
        }
    }

    override fun onResume() {
        super.onResume()

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver, IntentFilter(
            AppLockTimer.COUNTDOWN_BR))

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        var devicePolicyManager: DevicePolicyManager? = null
    }
}