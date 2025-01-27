package com.example.guru2_project

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.example.guru2_project.databinding.ItemSpinnerBinding

class OptionSpinnerAdapter(context: Context, @LayoutRes private val resId:Int,
                           private val menuList:List<String>):
    ArrayAdapter<String>(context,resId,menuList) {

    // 드롭다운 하지 않은 상태의 Spinner 항목 뷰
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val binding= ItemSpinnerBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        binding.tvTitle.text=menuList[position]

        binding.tvTitle.setTextColor(if (position == 0) {
            // 첫 번째 항목일 때 다른 색상 적용
            ContextCompat.getColor(context, R.color.edit_color)
        } else {
            // 나머지 항목은 기본 색상
            ContextCompat.getColor(context, R.color.text_color2)
        })

        return binding.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding= ItemSpinnerBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        binding.tvTitle.text=menuList[position]

        binding.tvTitle.setTextColor(if (position == 0) {
            // 첫 번째 항목일 때 다른 색상 적용
            Color.GRAY
        } else {
            // 나머지 항목은 기본 색상
            ContextCompat.getColor(context, R.color.text_color)
        })



        // 구분선 추가위해 레이아웃에 View 추가
        if(position<menuList.size-1){
            // 구분선 추가
            val divider= View(parent.context).apply {
                layoutParams= ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,2
                )
                setBackgroundColor(Color.GRAY)
            }
            (binding.root as ViewGroup).addView(divider)
        }

        return binding.root
    }

    override fun getCount()=menuList.size
}