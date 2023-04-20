package com.example.privasee.ui.users

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.example.privasee.R
import com.example.privasee.database.model.User
import com.example.privasee.databinding.RecyclerItemUserBinding


class UserListAdapter(val applicationContext: Context) : RecyclerView.Adapter<UserListAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: RecyclerItemUserBinding): RecyclerView.ViewHolder(binding.root)
    private var userList = emptyList<User>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RecyclerItemUserBinding.inflate(layoutInflater, parent, false)

        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]

        holder.binding.apply {
            tvUserName.text = currentUser.name
            val owner = "Owner"

            val sp = PreferenceManager.getDefaultSharedPreferences(applicationContext)

            // var path = getOutputDirectory()
            var pathFd = (sp.getString("ownerPic", "none"))
            val bitmap = BitmapFactory.decodeFile(pathFd)

            if (currentUser.isOwner){
                tvIsOwner.text = owner
                imageView.setImageBitmap(bitmap)
            }
            else{
                tvIsOwner.isVisible = false
                imageView.setBackgroundResource(R.drawable.img_1)
            }

            recyclerItemUser.setOnClickListener {
                val action = UserListFragmentDirections.actionUserFragmentToUpdateUserFragment(currentUser)
                recyclerItemUser.findNavController().navigate(action)
            }
        }

    }

    override fun getItemCount(): Int {
        return userList.count()
    }

    fun setData(user: List<User>) {
        this.userList = user
        notifyItemInserted(userList.size-1)
    }

}