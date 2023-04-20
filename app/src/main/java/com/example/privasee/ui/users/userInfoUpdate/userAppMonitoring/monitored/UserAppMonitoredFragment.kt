package com.example.privasee.ui.users.userInfoUpdate.userAppMonitoring.monitored

import android.content.Intent
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
import com.example.privasee.AppAccessService
import com.example.privasee.R
import com.example.privasee.database.viewmodel.AppViewModel
import com.example.privasee.database.viewmodel.RestrictionViewModel
import com.example.privasee.databinding.FragmentUserAppMonitoredBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class UserAppMonitoredFragment : Fragment() {

    private var _binding: FragmentUserAppMonitoredBinding? = null
    private val binding get() = _binding!!

    private lateinit var mRestrictionViewModel: RestrictionViewModel
    private lateinit var mAppViewModel: AppViewModel

    private val args: UserAppMonitoredFragmentArgs by navArgs()

    private var job1: Job? = null
    private var job2: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserAppMonitoredBinding.inflate(inflater, container, false)

        // Database view-models
        mRestrictionViewModel = ViewModelProvider(this)[RestrictionViewModel::class.java]
        mAppViewModel = ViewModelProvider(this)[AppViewModel::class.java]

        // Nav args, take user id then put it in an extra bundle for navigating
        val userId = args.userId
        val bundle = Bundle()
        bundle.putInt("userId", userId)

        // Setting Recyclerview Adapter
        val adapter = UserAppMonitoredAdapter()
        binding.rvAppMonitored.adapter = adapter
        binding.rvAppMonitored.layoutManager = LinearLayoutManager(requireContext())

        // Display list of controlled apps on recyclerview
        job1 = lifecycleScope.launch {
            val monitoredList = mRestrictionViewModel.getAllMonitoredApps(userId)
            withContext(Dispatchers.Main) {
                monitoredList.observe(viewLifecycleOwner, Observer {
                    adapter.setData(it)
                })
            }
        }

        binding.btnUnmonitoredList.setOnClickListener {
            findNavController().navigate(R.id.action_appMonitoredFragment_to_appUnmonitoredFragment, bundle)
        }

        // Update new list of monitored apps
        binding.btnApplyMonitored.setOnClickListener {

            val newRestriction = adapter.getCheckedApps()

            if (newRestriction.isNotEmpty()) {

                // Send data to Accessibility Service on monitoring
                job2 = lifecycleScope.launch(Dispatchers.IO) {

                    // Update Monitored Apps in database
                    for (restrictionId in newRestriction)
                        mRestrictionViewModel.updateMonitoredApps(restrictionId, false)

                    // Package names selected to be removed from monitoring in accessbility service
                    // Take effect immediately on accessibility service's monitoring
                    val newMonitoredListPackageName: MutableList<String> = mutableListOf()
                    for (restrictionId in newRestriction) {
                        val appName = mRestrictionViewModel.getAppName(restrictionId)
                        newMonitoredListPackageName.add(mAppViewModel.getPackageName(appName))
                    }
                    val intent = Intent(requireContext(), AppAccessService::class.java)
                    intent.putExtra("action", "removeMonitor" )
                    intent.putStringArrayListExtra("packageNames", ArrayList(newMonitoredListPackageName))
                    requireContext().startService(intent)
                }

                findNavController().navigate(
                    R.id.action_appMonitoredFragment_to_appUnmonitoredFragment,
                    bundle
                )

            } else {
                Toast.makeText(requireContext(), "Please select apps to remove from monitoring or just press back to return", Toast.LENGTH_SHORT).show()
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