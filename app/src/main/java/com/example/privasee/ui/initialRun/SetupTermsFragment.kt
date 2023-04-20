package com.example.privasee.ui.initialRun

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.privasee.R
import com.example.privasee.databinding.FragmentSetupTermsBinding
import kotlinx.android.synthetic.main.fragment_setup_terms.*

class SetupTermsFragment : Fragment() {

    private var _binding: FragmentSetupTermsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetupTermsBinding.inflate(inflater, container, false)

        binding.btnTermsNext.setOnClickListener {
            if(termsCheckBox.isChecked){
                findNavController().navigate(R.id.action_setupTermsFragment_to_setupOwnerFragment)
            }else{
                Toast.makeText(requireContext(), "Please Agree with the Terms and Conditions first.", Toast.LENGTH_LONG).show()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}