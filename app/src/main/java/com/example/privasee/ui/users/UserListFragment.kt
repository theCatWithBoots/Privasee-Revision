package com.example.privasee.ui.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.privasee.R
import com.example.privasee.database.viewmodel.UserViewModel
import com.example.privasee.databinding.FragmentUserListBinding
import com.example.privasee.utils.CheckPermissionUtils

class UserListFragment : Fragment() {

    private var _binding: FragmentUserListBinding? = null
    private val binding get() = _binding!!
    private lateinit var mUserViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentUserListBinding.inflate(inflater, container, false)

        CheckPermissionUtils.checkAccessibilityPermission(requireContext())

        // Recyclerview Adapter
        val adapter = UserListAdapter(requireContext())
        binding.rvUser.adapter = adapter
        binding.rvUser.layoutManager = LinearLayoutManager(requireContext())

        // Database Queries
        mUserViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        mUserViewModel.getAllDataLive.observe(viewLifecycleOwner, Observer {
            adapter.setData(it)
        })

        binding.btnAddUser.setOnClickListener {
            findNavController().navigate(R.id.action_userFragment_to_addUserFragment)
        }

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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}