package com.example.privasee.ui.controlAccess

import android.app.Activity
import android.app.ActivityManager
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.app.admin.DevicePolicyManager
import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.example.privasee.R
import com.example.privasee.databinding.FragmentControlAccessScreentimelimitBinding
import kotlinx.android.synthetic.main.fragment_control_access_screentimelimit.*
import java.util.*


class ControlAccessFragmentScreenTimeLimit : Fragment() {

    private var _binding: FragmentControlAccessScreentimelimitBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        devicePolicyManager = requireActivity().getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        activityManager = requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        compName = ComponentName(requireContext(), MyAdmin::class.java)

        _binding = FragmentControlAccessScreentimelimitBinding.inflate(inflater, container, false)

        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_screenTimeLimit_to_controlAccessFragment)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback (callback)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val editor = sp.edit()

        setTimer.setOnClickListener {
            val active = devicePolicyManager!!.isAdminActive(compName!!)
            val timerString = timeButton.text.toString()

            if (active && !( timerString.equals("select time", ignoreCase = true))) {

                editor.apply() {
                    putBoolean("IS_ACTIVITY_RUNNING", true)
                }.apply()

                Toast.makeText(requireContext(), "Timer has been started", Toast.LENGTH_LONG).show()

                val units = timerString.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray() //will break the string up into an array
                val hour = units[0].toInt() //first element
                val minutes = units[1].toInt() //second element
                val duration = 60 * hour + minutes //add up our values
                val timerInt = duration.toLong()
                requireActivity().startForegroundService(
                    Intent(context, MyForegroundServices::class.java)
                        .putExtra("screenTimer",timerInt))

            } else if(active && ( timerString.equals("select time", ignoreCase = true))){
                Toast.makeText(
                    requireContext(),
                    "You need to set Timer first",
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                Toast.makeText(
                    requireContext(),
                    "You need to enable the Admin Device Features",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        cancelTimer.setOnClickListener {

            editor.apply() {
                putBoolean("IS_ACTIVITY_RUNNING", false)
            }.apply()
            val tempString = "Timer is not set"
            remainingTime.text = tempString

            Toast.makeText(requireContext(), "Timer has been stopped", Toast.LENGTH_LONG).show()

            requireActivity().stopService(
                Intent(context, MyForegroundServices::class.java))
        }

        givePermission.setOnClickListener {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
            intent.putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Additional text explaining why we need this permission"
            )
            startActivityForResult(intent, RESULT_ENABLE)
        }

        disablePermission.setOnClickListener{
            devicePolicyManager!!.removeActiveAdmin(compName!!)
            disablePermission.setVisibility(View.GONE)
            givePermission.setVisibility(View.VISIBLE)
        }

        timeButton.setOnClickListener {
            popTimePicker()
        }

    }

    fun popTimePicker() {
        // var timeButton: Button? = null
        var hour = 0
        var minute = 0

        val onTimeSetListener =
            OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                hour = selectedHour
                minute = selectedMinute
                timeButton!!.text =
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
        Log.i("OnPause", "Unregistered broadcast receiver")
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

            remainingTime.text = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        }
    }

    override fun onResume() {
        super.onResume()

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver, IntentFilter(
                MyForegroundServices.COUNTDOWN_BR))

        val isActive = devicePolicyManager!!.isAdminActive(compName!!)
        disablePermission.visibility = if (isActive) View.VISIBLE else View.GONE
        givePermission.visibility = if (isActive) View.GONE else View.VISIBLE

    }

     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RESULT_ENABLE -> if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(
                    requireContext(),
                    "You have enabled the Admin Device features",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Problem to enable the Admin Device features",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val RESULT_ENABLE = 11
        var devicePolicyManager: DevicePolicyManager? = null
        private var activityManager: ActivityManager? = null
        private var compName: ComponentName? = null    }
}