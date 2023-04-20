package com.example.privasee.ui.monitor

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.privasee.R
import com.example.privasee.databinding.RecyclerItemMonitorViewImageBinding

class ViewImage : Fragment() {

    private var _binding: RecyclerItemMonitorViewImageBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<ViewImageArgs>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RecyclerItemMonitorViewImageBinding.inflate(inflater, container, false)


        val imageString = args.currentRecord.image

        val bitmap = BitmapFactory.decodeFile(imageString)
        binding.ViewSnapshotImage.setImageBitmap(bitmap)

        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_ViewImage_to_AccessRecords)
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



