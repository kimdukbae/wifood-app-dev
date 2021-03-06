package com.example.wifood.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wifood.R
import com.example.wifood.entity.Group

class GroupSelectAdapter(private val context: Context): RecyclerView.Adapter<GroupSelectAdapter.FoodGroupViewHolder>() {
    private var groupList = mutableListOf<Group>()

    fun setListData(data:MutableList<Group>) {
        groupList = data
    }

    fun setListDataClear() {
        groupList.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodGroupViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.group_select, parent, false)
        return FoodGroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupSelectAdapter.FoodGroupViewHolder, position: Int) {
        val group : Group = groupList[position]
        holder.group_name.text = group.name
        holder.group_name.setOnClickListener {
            groupClickListener.onClick(it, position, group)
        }
    }

    override fun getItemCount(): Int {
        return groupList.size
    }

    inner class FoodGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val group_name : TextView = itemView.findViewById(R.id.group_name)
    }

    interface GroupClickListener {
        fun onClick(view: View, position: Int, group: Group)
    }
    private lateinit var groupClickListener: GroupClickListener
    fun setGroupClickListener(groupClickListener: GroupClickListener) {
        this.groupClickListener = groupClickListener
    }
}