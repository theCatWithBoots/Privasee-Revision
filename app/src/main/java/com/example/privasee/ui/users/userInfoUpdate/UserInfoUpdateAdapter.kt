package com.example.privasee.ui.users.userInfoUpdate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.privasee.database.model.Restriction
import com.example.privasee.databinding.RecyclerItemAppInfoBinding


class UserInfoUpdateAdapter(): RecyclerView.Adapter<UserInfoUpdateAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: RecyclerItemAppInfoBinding): RecyclerView.ViewHolder(binding.root)
    private var restrictionList = emptyList<Restriction>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RecyclerItemAppInfoBinding.inflate(layoutInflater, parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val app = restrictionList[position]

        holder.binding.apply {
            tvAppName.text = app.appName
        }
    }

    override fun getItemCount(): Int {
        return restrictionList.count()
    }

    fun setData(data: List<Restriction>) {
        this.restrictionList = data
        notifyDataSetChanged()
    }

}