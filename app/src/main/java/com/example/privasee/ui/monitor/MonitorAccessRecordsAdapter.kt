package com.example.privasee.ui.monitor

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.privasee.database.model.Record
import com.example.privasee.database.model.User
import com.example.privasee.databinding.RecyclerItemMonitorRecordsBinding
import java.text.SimpleDateFormat
import java.time.Month
import java.util.*


class MonitorAccessRecordsAdapter(): RecyclerView.Adapter<MonitorAccessRecordsAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: RecyclerItemMonitorRecordsBinding): RecyclerView.ViewHolder(binding.root)
    private var recordList = emptyList<Record>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RecyclerItemMonitorRecordsBinding.inflate(layoutInflater, parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentRecord = recordList[position]

        if(currentRecord.day > 0) {
            val day = currentRecord.day
            val month = Month.of(currentRecord.month).name
            val year = currentRecord.year
            val appName = currentRecord.packageName
            val time = currentRecord.time
            val imageString = currentRecord.image
            val bitmap = BitmapFactory.decodeFile(imageString)
            val dateFormat = "$month $day $year"

            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val timeString = sdf.format(Date(time))

            holder.binding.apply {
                tvRecordsDate.text = dateFormat
                tvRecordsTime.text = timeString
                tvRecordsAppName.text = appName
                tvImageView.setImageBitmap(bitmap)
            }
        } else {
            val emptyString = "-"
            holder.binding.apply {
                tvRecordsDate.text = emptyString
                tvRecordsTime.text = emptyString
                tvRecordsStatus.text = emptyString
            }
        }

        holder.binding.apply {
            RecyclerItemMonitorRecords.setOnClickListener {
                val action = MonitoringAccessRecordsDirections.actionAccessRecordsToViewImage(currentRecord)
                RecyclerItemMonitorRecords.findNavController().navigate(action)
            }
        }
    }
    override fun getItemCount(): Int {
        return recordList.count()
    }

    fun setData(data: List<Record>) {
        this.recordList = data
        notifyDataSetChanged()
    }

}