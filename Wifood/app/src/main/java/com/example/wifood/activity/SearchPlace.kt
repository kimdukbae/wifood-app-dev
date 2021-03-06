package com.example.wifood.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wifood.adapter.SearchPlaceAdapter
import com.example.wifood.databinding.ActivitySearchPlaceBinding
import com.example.wifood.entity.Search
import com.skt.Tmap.TMapData
import com.skt.Tmap.TMapTapi
import com.skt.Tmap.poi_item.TMapPOIItem

class SearchPlace : AppCompatActivity() {
    lateinit var binding : ActivitySearchPlaceBinding
    lateinit var searchPlaceAdapter : SearchPlaceAdapter
    var searchResult = MutableLiveData<MutableList<Search>>()
    lateinit var inputMethodManager: InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // tmap api에서 받은 검색 결과를 출력하기 위한 recyclerView 설정
        searchPlaceAdapter = SearchPlaceAdapter(this)
        binding.searchResultRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.searchResultRecyclerView.adapter = searchPlaceAdapter
        binding.searchResultRecyclerView.addItemDecoration(DividerItemDecoration(this, 1))
        searchResult.observe(this) {
            searchPlaceAdapter.setListData(it)
            searchPlaceAdapter.notifyDataSetChanged()
        }

        // tmap api 사용을 위한 key 설정
        val tmapTAPI = TMapTapi(this)
        tmapTAPI.setSKTMapAuthentication("l7xx56bf2cddf5f84556bdf35558d72f530a")


        // 검색 버튼
        binding.searchButton.setOnClickListener {
            clickSearchBtn()
        }

        // 엔터키 버튼 입력
        binding.keywordText.setOnEditorActionListener { textView, i, keyEvent ->
            var handled = false
            if (i == EditorInfo.IME_ACTION_DONE) {
                clickSearchBtn()
                handled = true
            }
            handled
        }

        searchPlaceAdapter.setSearchResultClickListener(object: SearchPlaceAdapter.SearchResultClickListener{
            override fun onClick(view: View, position: Int, item: Search) {
                val intent = Intent().apply {
                    putExtra("searchResult", item)
                }
                setResult(RESULT_OK, intent)
                finish()
            }
        })
    }

    fun clickSearchBtn() {
        inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.keywordText.windowToken, 0)
        searchPlace(binding.keywordText.text.toString())
    }

    // tmap api에 keyword를 이용해 검색한 결과를 받아와 searchResult에 저장
    // TODO("ViewModel로 빼기")
    private fun searchPlace(keyword: String) {
        val search : MutableList<Search> = mutableListOf()
        val tmapData = TMapData()
        tmapData.findAllPOI(keyword, TMapData.FindAllPOIListenerCallback {
            for (i in it) {
                val poiItem:TMapPOIItem = i
                val bizName: String = poiItem.middleBizName.toString() + "," + poiItem.lowerBizName + "," + poiItem.detailBizName
                var addressRoad = ""
                for (a in poiItem.newAddressList)
                    addressRoad = a.fullAddressRoad
                addressRoad += poiItem.detailAddrName.replace("null", "")
                search.add(Search(addressRoad, poiItem.poiName.toString(), poiItem.poiPoint.latitude,
                        poiItem.poiPoint.longitude, bizName))
                searchResult.postValue(search)
            }
        })
    }
}