package com.example.privasee.ui.users.userInfoUpdate.userAppMonitoring.unmonitored

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.privasee.AppAccessService
import com.example.privasee.R
import com.example.privasee.database.viewmodel.AppViewModel
import com.example.privasee.database.viewmodel.RestrictionViewModel
import com.example.privasee.databinding.FragmentUserAppUnmonitoredBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class UserAppUnmonitoredFragment : Fragment() {

    private var _binding: FragmentUserAppUnmonitoredBinding? = null
    private val binding get() = _binding!!

    private lateinit var mRestrictionViewModel: RestrictionViewModel
    private lateinit var mAppViewModel: AppViewModel

    private var job1: Job? = null
    private var job2: Job? = null

    private val args: UserAppUnmonitoredFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserAppUnmonitoredBinding.inflate(inflater, container, false)

        // Database view models
        mRestrictionViewModel = ViewModelProvider(this)[RestrictionViewModel::class.java]
        mAppViewModel = ViewModelProvider(this)[AppViewModel::class.java]

        // Recyclerview adapter
        val adapter = UserAppUnmonitoredAdapter()
        binding.rvAppUnmonitored.adapter = adapter
        binding.rvAppUnmonitored.layoutManager = LinearLayoutManager(requireContext())

        // Nav args, take user id then put it in an extra bundle for navigating
        val userId = args.userId
        val bundle = Bundle()
        bundle.putInt("userId", userId)

        // Display list of controlled apps on recyclerview
        job1 = lifecycleScope.launch {
            val unmonitoredList = mRestrictionViewModel.getAllUnmonitoredApps(userId)
            withContext(Dispatchers.Main) {
                unmonitoredList.observe(viewLifecycleOwner) {
                    adapter.setData(it)
                }
            }
        }

        binding.btnMonitoredList.setOnClickListener {
            findNavController().navigate(R.id.action_appUnmonitoredFragment_to_appMonitoredFragment, bundle)
        }

        binding.btnApplyUnmonitored.setOnClickListener {

            val newRestriction = adapter.getCheckedApps()

            if (newRestriction.isNotEmpty()) {

                // Send data to Accessibility Service on monitoring
                job2 = lifecycleScope.launch(Dispatchers.IO) {

                    // Update Monitored Apps in database
                    for (restrictionId in newRestriction)
                        mRestrictionViewModel.updateMonitoredApps(restrictionId, true)

                    // Add to package names to monitored list then send it to accessibility service
                    // Take effect immediately on accessibility service's monitoring
                    val newMonitoredListPackageName: MutableList<String> = mutableListOf()
                    for (restrictionId in newRestriction) {
                        val appName = mRestrictionViewModel.getAppName(restrictionId)
                        newMonitoredListPackageName.add(mAppViewModel.getPackageName(appName))
                    }
                    val intent = Intent(requireContext(), AppAccessService::class.java)
                    intent.putExtra("action", "addMonitor" )
                    intent.putStringArrayListExtra("packageNames", ArrayList(newMonitoredListPackageName))
                    requireContext().startService(intent)
                }

                findNavController().navigate(
                    R.id.action_appUnmonitoredFragment_to_appMonitoredFragment,
                    bundle
                )

            } else {
                Toast.makeText(requireContext(), "Please select apps to monitor or just press back to return", Toast.LENGTH_SHORT).show()
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