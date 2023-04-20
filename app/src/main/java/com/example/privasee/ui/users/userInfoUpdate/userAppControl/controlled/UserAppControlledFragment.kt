package com.example.privasee.ui.users.userInfoUpdate.userAppControl.controlled

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.privasee.R
import com.example.privasee.database.viewmodel.RestrictionViewModel
import com.example.privasee.database.viewmodel.UserViewModel
import com.example.privasee.databinding.FragmentUserAppControlledBinding
import com.example.privasee.ui.userList.userInfoUpdate.userAppControl.controlled.UserAppControlledAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserAppControlledFragment : Fragment() {

    private var _binding: FragmentUserAppControlledBinding? = null
    private val binding get() = _binding!!

    private lateinit var mUserViewModel: UserViewModel
    private lateinit var mRestrictionViewModel: RestrictionViewModel

    private val args: UserAppControlledFragmentArgs by navArgs()

    private var job1: Job? = null
    private var job2: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserAppControlledBinding.inflate(inflater, container, false)

        // Database view models
        mUserViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        mRestrictionViewModel = ViewModelProvider(this)[RestrictionViewModel::class.java]

        // Nav args, take user id then put it in an extra bundle for navigating
        val userId = args.userId
        val bundle = Bundle()
        bundle.putInt("userId", userId)

        // Setting Recyclerview Adapter
        val adapter = UserAppControlledAdapter()
        binding.rvAppControlled.adapter = adapter
        binding.rvAppControlled.layoutManager = LinearLayoutManager(requireContext())

        // Display list of controlled apps on recyclerview
        job1 = lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                mRestrictionViewModel.getAllControlledApps(userId).observe(viewLifecycleOwner, Observer {
                    adapter.setData(it)
                })
            }
        }

        binding.btnUncontrolledList.setOnClickListener {
            findNavController().navigate(R.id.action_userAppControlledFragment_to_userAppUncontrolledFragment, bundle)
        }

        // Update new list of uncontrolled apps
        binding.btnApplyControlled.setOnClickListener {
            val newUncontrolledList = adapter.getCheckedApps()

            if (newUncontrolledList.isNotEmpty()) {
                job2 = lifecycleScope.launch(Dispatchers.IO) {
                    for (restrictionId in newUncontrolledList)
                        mRestrictionViewModel.updateControlledApps(restrictionId, false)
                }
                findNavController().navigate(R.id.action_userAppControlledFragment_to_userAppUncontrolledFragment, bundle)
            } else {
                Toast.makeText(requireContext(), "Please select apps to remove from app locking or just press back to return", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job1?.cancel()
        job2?.cancel()
        _binding = null
    }

}