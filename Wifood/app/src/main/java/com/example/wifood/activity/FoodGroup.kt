package com.example.wifood.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wifood.adapter.GroupAdapter
import com.example.wifood.databinding.ActivityFoodGroupBinding
import com.example.wifood.entity.Group
import com.example.wifood.viewmodel.FoodGroupViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FoodGroup : AppCompatActivity() {
    lateinit var binding : ActivityFoodGroupBinding
    private lateinit var foodGroupAdapter: GroupAdapter
    lateinit var foodGroupViewModel : FoodGroupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "맛집 그룹"

        // Connecting RecyclerView and Adapter
        foodGroupViewModel = ViewModelProvider(this).get(FoodGroupViewModel::class.java)
        foodGroupAdapter = GroupAdapter(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = foodGroupAdapter

        // Automatically change bindings when data changes
        foodGroupViewModel.foodGroupList.observe(this) {
            foodGroupAdapter.setListData(it)
            foodGroupAdapter.notifyDataSetChanged()
        }

        // group add btn
        binding.groupAddButton.setOnClickListener {
            val intent = Intent(this@FoodGroup, EditFoodGroup::class.java).apply {
                putExtra("type", "ADD")
            }
            requestActivity.launch(intent)
        }

        // group del btn
        binding.groupDeleteButton.setOnClickListener {
            val intent = Intent(this@FoodGroup, DeleteFoodGroup::class.java).apply {
                putExtra("groupName", ArrayList(foodGroupAdapter.getGroupNameList()))
                putExtra("groupId", ArrayList(foodGroupAdapter.getGroupIdList()))
                putExtra("groupColor", ArrayList(foodGroupAdapter.getGroupColorList()))
            }
            requestActivity.launch(intent)
        }

        // group edit btn
        foodGroupAdapter.setGroupEditClickListener(object: GroupAdapter.GroupEditClickListener {
            override fun onClick(view: View, position: Int, groupId: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    val group: Group = foodGroupViewModel.getGroup(position)
                    val intent = Intent(this@FoodGroup, EditFoodGroup::class.java).apply {
                        putExtra("type", "EDIT")
                        putExtra("groupId", group.id)
                        putExtra("groupName", group.name)
                        putExtra("groupColor", group.color)
                    }
                    requestActivity.launch(intent)
                }
            }
        })
    }

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            // type 0 : add, 1 : edit, 2 : delete
            when(it.data?.getIntExtra("type", -1)) {
                0 -> {
                    val maxId = foodGroupAdapter.getGroupIdList().maxOrNull() ?: 0
                    // create a group to add using the value received from EditFoodGroup Activity
                    val group = Group(maxId + 1, it.data?.getSerializableExtra("name") as String,
                        it.data?.getSerializableExtra("color") as String)
                    CoroutineScope(Dispatchers.IO).launch {
                        foodGroupViewModel.groupInsert(group)
                    }
                }
                1 -> {
                    val group = Group(it.data?.getSerializableExtra("id") as Int, it.data?.getSerializableExtra("name") as String,
                        it.data?.getSerializableExtra("color") as String)
                    CoroutineScope(Dispatchers.IO).launch {
                        foodGroupViewModel.groupInsert(group)
                    }
                }
                2 -> {
                    val groupId = it.data?.getIntegerArrayListExtra("id")
                    CoroutineScope(Dispatchers.IO).launch {
                        if (groupId != null)
                            foodGroupViewModel.groupDelete(groupId)
                    }
                }
            }
        }
    }
}
