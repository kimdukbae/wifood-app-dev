package com.example.wifood.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.GridLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wifood.R
import com.example.wifood.adapter.FoodListAdapter
import com.example.wifood.databinding.ActivityFoodListBinding
import com.example.wifood.entity.Food
import com.example.wifood.entity.Place
import com.example.wifood.entity.Search
import com.example.wifood.viewmodel.FoodListViewModel
import com.example.wifood.viewmodel.PlaceViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FoodList : AppCompatActivity() {
    lateinit var binding : ActivityFoodListBinding
    private lateinit var foodListAdapter: FoodListAdapter
    lateinit var foodListViewModel: FoodListViewModel
    lateinit var placeViewModel: PlaceViewModel
    var placeList = mutableListOf<Place>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 툴바 설정
        val toolbar: Toolbar = findViewById(R.id.main_layout_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)                       // 뒤로가기 버튼 활성화
        supportActionBar?.setDisplayShowTitleEnabled(true)                      // 툴바에 타이틀 안보이게 설정
        supportActionBar?.title = intent.getStringExtra("groupName")

        // 데이터베이스 접근을 위한 food group id정보 받아옴
        val groupId = intent.getIntExtra("groupId", 0)

        // 데이터베이스 접근을 위한 viewModel 설정, 파라미터로 groupId를 넘겨줌
        foodListViewModel = ViewModelProvider(this, FoodListViewModel.Factory(groupId)).get(FoodListViewModel::class.java)
        placeViewModel = ViewModelProvider(this).get(PlaceViewModel::class.java)

        // 데이터베이스에서 받아온 foodlist 정보를 이용해 recyclerView 설정
        foodListAdapter = FoodListAdapter(this)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = foodListAdapter
        binding.recyclerView.addItemDecoration(DividerItemDecoration(this, 1))

        foodListViewModel.foodList.observe(this) {
            if (it != null) foodListAdapter.setFoodListData(it)
            else foodListAdapter.setListDataClear()
            foodListAdapter.notifyDataSetChanged()
            setEmptyRecyclerView()
        }

        placeViewModel.placeList.observe(this) {
            if (it != null) foodListAdapter.setPlaceListData(it)
            foodListAdapter.notifyDataSetChanged()
        }

        // foodlist add btn
        binding.foodListAddButton.setOnClickListener {
            val intent = Intent(this@FoodList, AddFoodList::class.java)
            requestActivity.launch(intent)
        }

//        // foodlist delete btn
//        binding.foodListDeleteButton.setOnClickListener {
//            val intent = Intent(this@FoodList, DeleteFoodList::class.java).apply {
//                putParcelableArrayListExtra("foodlist", foodListViewModel.getFoodList())
//            }
//            requestActivity.launch(intent)
//        }
//
//        // foodlist edit btn
//        foodListAdapter.setFoodListClickListener(object: FoodListAdapter.FoodListClickListener{
//            override fun onClick(view: View, position: Int, item: Food) {
//                val intent = Intent(this@FoodList, EditFoodList::class.java).apply {
//                    putExtra("food", item)
//                }
//                requestActivity.launch(intent)
//            }
//        })

        // map btn
//        foodListAdapter.setMapButtonClickListener(object: FoodListAdapter.FoodListClickListener{
//            override fun onClick(view: View, position: Int, item: Food) {
//                val intent = Intent(this@FoodList, Map::class.java).apply {
//                    putExtra("latitude", item.latitude)
//                    putExtra("longitude", item.longitude)
//                    putExtra("name", item.name)
//                }
//                startActivity(intent)
//                finish()
//            }
//        })

        foodListAdapter.setPopupButtonClickListener(object: FoodListAdapter.FoodListClickListener{
            override fun onClick(view: View, position: Int, item: Food) {
                val popupMenu = PopupMenu(this@FoodList, view)
                popupMenu.menuInflater.inflate(R.menu.popup_food, popupMenu.menu)
                popupMenu.show()
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.delete_menu -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                foodListViewModel.deleteFood(item.id)
                            }
                        }
                        R.id.edit_menu -> {
                            val intent = Intent(this@FoodList, EditFoodList::class.java).apply {
                                putExtra("food", item)
                            }
                            requestActivity.launch(intent)
                        }
                    }
                    true
                }
            }
        })
    }

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            // type 0: add, 1: edit, 2: delete
            when(it.data?.getIntExtra("type", -1)) {
                0 -> {
                    // AddFoodListActivity에서 받은 정보를 이용해 food를 생성해 db에 추가
                    // TODO("코드 리팩토링 필요해 보임")
                    val searchResult = it.data?.getParcelableExtra<Search>("searchResult")
                    val tasteGrade = it.data?.getDoubleExtra("taste", 0.0)
                    val cleanGrade = it.data?.getDoubleExtra("clean", 0.0)
                    val kindnessGrade = it.data?.getDoubleExtra("kindness", 0.0)
                    val visited = it.data?.getIntExtra("visited", 0)
                    val memo = it.data?.getStringExtra("memo")
                    var placeId = placeViewModel.getPlaceId(searchResult!!.name)
                    var arrPlaceGrade = doubleArrayOf(tasteGrade!!, cleanGrade!!, kindnessGrade!!)
                    var visitCount = 0
                    if (placeId == 0) {
                        placeId = placeViewModel.getPlaceMaxId() + 1
                        if (visited == 1)
                            visitCount++
                    }
                    else {
                        if (visited == 1) {
                            placeViewModel.setPlaceGrade(
                                placeId,
                                tasteGrade,
                                cleanGrade,
                                kindnessGrade
                            )
                            placeViewModel.setPlaceVisit(placeId)
                        }
                        arrPlaceGrade = placeViewModel.getPlaceGrade(placeId)
                        visitCount = placeViewModel.getPlaceVisit(placeId)
                    }
                    val food = Food(foodListViewModel.getFoodListMaxId() + 1, searchResult.name, memo!!,
                        searchResult.fullAddress, searchResult.latitude, searchResult.longitude,
                        tasteGrade, cleanGrade, kindnessGrade, visited!!, placeId)
                    val place = Place(placeId, searchResult.name, searchResult.fullAddress, searchResult.latitude,
                        searchResult.longitude, arrPlaceGrade[0], arrPlaceGrade[1], arrPlaceGrade[2], visitCount)
                    CoroutineScope(Dispatchers.IO).launch {
                        foodListViewModel.insertFoodList(food)
                        placeViewModel.insertPlace(place)
                    }
                }
                1 -> {
                    // EditFoodListActivity에서 받은 수정된 food를 이용해 db 수정
                    val editFood = it.data?.getParcelableExtra<Food>("food")
                    CoroutineScope(Dispatchers.IO).launch {
                        foodListViewModel.insertFoodList(editFood!!)
                    }
                }
//                2 -> {
//                    // DeleteFoodListActivity에서 받은 삭제할 id list를 이용해 db에서 삭제
//                    val deleteIdList = it.data?.getIntegerArrayListExtra("deleteIdList")
//                    CoroutineScope(Dispatchers.IO).launch {
//                        foodListViewModel.deleteFoodList(deleteIdList!!)
//                    }
//                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 클릭한 툴바 메뉴 아이템의 id마다 다르게 실행하도록 설정
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setEmptyRecyclerView() {
        if (foodListAdapter.itemCount == 0) {
            binding.recyclerView.visibility = View.INVISIBLE
            binding.emptyText.visibility = View.VISIBLE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.emptyText.visibility = View.INVISIBLE
        }
    }
}
