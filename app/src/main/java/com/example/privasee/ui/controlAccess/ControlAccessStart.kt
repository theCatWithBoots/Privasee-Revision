package com.example.privasee.ui.controlAccess

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.privasee.R
import com.example.privasee.databinding.FragmentControlAccessStartBinding

class ControlAccessStart : Fragment() {

    private var _binding: FragmentControlAccessStartBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentControlAccessStartBinding.inflate(inflater, container, false)

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
        super.onViewCreated(view, savedInstanceState)

        val navHostFragmentControlAccess = childFragmentManager.findFragmentById(R.id.control_access_start) as NavHostFragment
        navController = navHostFragmentControlAccess.navController
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}