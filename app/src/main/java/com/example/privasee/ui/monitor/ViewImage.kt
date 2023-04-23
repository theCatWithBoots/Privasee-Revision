package com.example.privasee.ui.monitor

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.privasee.R
import com.example.privasee.database.viewmodel.RecordViewModel
import com.example.privasee.databinding.RecyclerItemMonitorViewImageBinding
import kotlinx.android.synthetic.main.recycler_item_monitor_view_image.*

class ViewImage : Fragment() {

    private var _binding: RecyclerItemMonitorViewImageBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<ViewImageArgs>()
    private lateinit var mRecordViewModel: RecordViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RecyclerItemMonitorViewImageBinding.inflate(inflater, container, false)

        mRecordViewModel = ViewModelProvider(this)[RecordViewModel::class.java]

        val imageString = args.currentRecord.image

        val bitmap = BitmapFactory.decodeFile(imageString)
        binding.ViewSnapshotImage.setImageBitmap(bitmap)

        binding.btnDeleteRecord.setOnClickListener {
            deleteRecord()
        }

        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_ViewImage_to_AccessRecords)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback (callback)



        return binding.root
    }

    private fun deleteRecord() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            mRecordViewModel.deleteRecord(args.currentRecord)
            Toast.makeText(
                requireContext(),
                "Record successfully removed",
                Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_ViewImage_to_AccessRecords)
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete this record?")
        builder.setMessage("Are you sure you want to delete this record?")
        builder.create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



