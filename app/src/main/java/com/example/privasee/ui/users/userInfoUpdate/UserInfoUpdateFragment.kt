package com.example.privasee.ui.users.userInfoUpdate


import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.privasee.R
import com.example.privasee.database.model.User
import com.example.privasee.database.viewmodel.RestrictionViewModel
import com.example.privasee.database.viewmodel.UserViewModel
import com.example.privasee.databinding.FragmentUserInfoUpdateBinding
import com.example.privasee.ui.users.addUser.AddUserCapturePhoto
import com.example.privasee.ui.users.userInfoUpdate.userAppControl.UserAppControllingActivity
import com.example.privasee.ui.users.userInfoUpdate.userAppMonitoring.UserAppMonitoringActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

class UserInfoUpdateFragment : Fragment(), MenuProvider {

    private var _binding: FragmentUserInfoUpdateBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<UserInfoUpdateFragmentArgs>()

    private lateinit var mUserViewModel: UserViewModel
    private lateinit var mRestrictionViewModel: RestrictionViewModel

    private var job1: Job? = null
    private var job2: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserInfoUpdateBinding.inflate(inflater, container, false)

        mUserViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        mRestrictionViewModel = ViewModelProvider(this)[RestrictionViewModel::class.java]

        val userName = args.currentUser.name
        val userId = args.currentUser.id
        val userIsOwner = args.currentUser.isOwner

        // Recyclerview Adapter
        val adapter = UserInfoUpdateAdapter()
        binding.rvAppList.adapter = adapter
        binding.rvAppList.layoutManager = LinearLayoutManager(requireContext())

        if (userIsOwner) { // hide controlled apps for owner and send monitored app list
            val restrictionList = "Monitored App List"
            binding.reEnroll.isVisible = true
            binding.tvRestrictionType.text = restrictionList
            binding.btnUserSetControlled.isVisible = false
            job1 = lifecycleScope.launch(Dispatchers.Main) {
                mRestrictionViewModel.getAllMonitoredApps(userId).observe(viewLifecycleOwner) {
                    adapter.setData(it)
                }
            }
        }
        else { // hide monitored apps for non-owner and send controlled app list
            val restrictionList = "Controlled App List"
            binding.tvRestrictionType.text = restrictionList
            binding.reEnroll.isVisible = false
            binding.btnUserSetMonitored.isVisible = false
            job2 = lifecycleScope.launch(Dispatchers.Main) {
                mRestrictionViewModel.getAllControlledApps(userId).observe(viewLifecycleOwner) {
                    adapter.setData(it)
                }
            }
        }

        binding.updateName.setText(userName)

        binding.btnDeleteUser.setOnClickListener {
            deleteUser()
        }

        binding.btnUserSetMonitored.setOnClickListener {
            Intent(requireContext(), UserAppMonitoringActivity::class.java).also { intent ->
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
        }

        binding.btnUserSetControlled.setOnClickListener {
            Intent(requireContext(), UserAppControllingActivity::class.java).also { intent ->
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
        }

        binding.reEnroll.setOnClickListener {
            val intent = Intent(requireContext(), AddUserCapturePhoto::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        binding.btnUserUpdateSave.setOnClickListener {
            updateItem()
            findNavController().navigate(R.id.action_updateUserFragment_to_userFragment)
        }

        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_updateUserFragment_to_userFragment)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback (callback)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }


    private fun deleteUser() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            mUserViewModel.deleteUser(args.currentUser)
            Toast.makeText(
                requireContext(),
                "Successfully removed: ${args.currentUser.name}",
                Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_updateUserFragment_to_userFragment)
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete ${args.currentUser.name}?")
        builder.setMessage("Are you sure you want to delete ${args.currentUser.name}?")
        builder.create().show()
    }

    private fun updateItem(){
        val name = binding.updateName.text.toString()
        if(checkInput(name)) {
            val updatedUser = User(args.currentUser.id, name, args.currentUser.isOwner)
            mUserViewModel.updateUser(updatedUser)
            Toast.makeText(requireContext(), "Updated $name", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkInput(name: String): Boolean {
        return name.isNotEmpty()
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }

    override fun onPause() {
        super.onPause()
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job1?.cancel()
        job2?.cancel()
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }

}