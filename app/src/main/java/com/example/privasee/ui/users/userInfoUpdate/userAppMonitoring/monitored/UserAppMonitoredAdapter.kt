package com.example.privasee.ui.users.userInfoUpdate.userAppMonitoring.monitored

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.privasee.database.model.Restriction
import com.example.privasee.databinding.RecyclerItemAppCbBinding
import com.example.privasee.ui.users.userInfoUpdate.userAppMonitoring.monitored.UserAppMonitoredAdapter

class UserAppMonitoredAdapter: RecyclerView.Adapter<UserAppMonitoredAdapter.AppViewHolder>() {

    inner class AppViewHolder(val binding: RecyclerItemAppCbBinding): RecyclerView.ViewHolder(binding.root)

    private var monitoredList = emptyList<Restriction>()
    private val checkedApps = mutableListOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RecyclerItemAppCbBinding.inflate(layoutInflater, parent, false)

        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val appName = monitoredList[position].appName

        holder.binding.apply {
            tvAppName.text = appName
            cbRestrict.isChecked = false
        }

        holder.binding.cbRestrict.setOnCheckedChangeListener { _, isChecked ->
            val restrictionId = this.monitoredList[position].id
            if (isChecked) {
                checkedApps.add(restrictionId)
            }
        }
    }

    override fun getItemCount(): Int {
        return monitoredList.size
    }

    fun setData(data: List<Restriction>) {
        this.monitoredList = data
        notifyItemInserted(monitoredList.size-1)
    }

    fun getCheckedApps(): List<Int> {
        return this.checkedApps
    }

}