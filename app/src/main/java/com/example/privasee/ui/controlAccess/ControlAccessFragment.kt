package com.example.privasee.ui.controlAccess

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.example.privasee.R
import com.example.privasee.databinding.FragmentControlAccessBinding
import kotlinx.android.synthetic.main.fragment_control_access.*

class ControlAccessFragment : Fragment() {

    private var _binding: FragmentControlAccessBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentControlAccessBinding.inflate(inflater, container, false)

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

        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if(sp.getBoolean("IS_APPLOCK_TIMER_RUNNING", false)){
            binding.scrnTimeLimit.isEnabled = false
        }else if(sp.getBoolean("IS_ACTIVITY_RUNNING", false)){
            binding.applock.isEnabled = false
        }else{
            binding.scrnTimeLimit.isEnabled = true
            binding.applock.isEnabled = true
        }

        applock.setOnClickListener {
            findNavController().navigate(R.id.action_controlAccessFragment_to_appLock)
        }

        scrnTimeLimit.setOnClickListener {
            findNavController().navigate(R.id.action_controlAccessFragment_to_screenTimeLimit)
        }

    }

}