package com.example.wifood.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wifood.R
import com.example.wifood.entity.Food
import kotlin.math.round

class FoodListAdapter(private val context: Context): RecyclerView.Adapter<FoodListAdapter.FoodListViewHolder>() {
    private var foodList = mutableListOf<Food>()

    fun setFoodListData(data:MutableList<Food>) {
        foodList = data
    }

    fun setListDataClear() {
        foodList.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodListViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.food_list, parent, false)
        return FoodListViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodListViewHolder, position: Int) {
        val food: Food = foodList[position]
        holder.foodName.text = food.name
        holder.foodAddress.text = food.address
        holder.foodMemo.text = food.memo
        if (food.visited == 1) {
            var gradeScore = (food.myTasteGrade + food.myCleanGrade + food.myKindnessGrade) / 3
            var grade = "${round(gradeScore * 10) / 10}/5"  // 출력하는 평점은 taste, clean, kind의 평균
            holder.myGrade.text = grade
        } else {
            holder.myGrade.visibility = View.INVISIBLE
        }
        holder.itemView.setOnClickListener {
            foodListClickListener.onClick(it, position, food)
        }
        holder.popupMenu.setOnClickListener {
            popupClickListener.onClick(it, position, food)
        }
    }

    override fun getItemCount(): Int {
        return foodList.size
    }

    inner class FoodListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodName : TextView = itemView.findViewById(R.id.foodName)
        val foodAddress : TextView = itemView.findViewById(R.id.foodAddress)
        val foodMemo : TextView = itemView.findViewById(R.id.foodMemo)
        val myGrade : TextView = itemView.findViewById(R.id.myGrade)
        val popupMenu : ImageButton = itemView.findViewById(R.id.popupMenu)
    }

    interface FoodListClickListener {
        fun onClick(view: View, position: Int, item: Food)
    }

    private lateinit var popupClickListener: FoodListClickListener
    private lateinit var foodListClickListener: FoodListClickListener

    fun setFoodListClickListener(foodListClickListener: FoodListClickListener) {
        this.foodListClickListener = foodListClickListener
    }

    fun setPopupButtonClickListener(popupClickListener: FoodListClickListener) {
        this.popupClickListener = popupClickListener
    }
}