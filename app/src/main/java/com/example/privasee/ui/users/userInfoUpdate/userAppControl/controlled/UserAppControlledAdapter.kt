package com.example.privasee.ui.userList.userInfoUpdate.userAppControl.controlled

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.privasee.database.model.Restriction
import com.example.privasee.databinding.RecyclerItemAppCbBinding

class UserAppControlledAdapter(): RecyclerView.Adapter<UserAppControlledAdapter.AppViewHolder>() {

    inner class AppViewHolder(val binding: RecyclerItemAppCbBinding): RecyclerView.ViewHolder(binding.root)

    private var controlledList = emptyList<Restriction>()
    private val checkedApps = mutableListOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RecyclerItemAppCbBinding.inflate(layoutInflater, parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val appName = controlledList[position].appName

        holder.binding.apply {
            tvAppName.text = appName
            cbRestrict.isChecked = false
        }

        holder.binding.cbRestrict.setOnCheckedChangeListener { _, isChecked ->
            val restrictionId = this.controlledList[position].id
            if (isChecked)
                checkedApps.add(restrictionId)
        }
    }

    override fun getItemCount(): Int {
        return controlledList.size
    }

    fun setData(data: List<Restriction>) {
        this.controlledList = data
        notifyItemInserted(controlledList.size-1)
    }

    fun getCheckedApps(): List<Int> {
        return this.checkedApps
    }
}