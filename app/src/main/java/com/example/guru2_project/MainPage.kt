package com.example.guru2_project

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class MainPage : AppCompatActivity() {
    lateinit var mypagebutton: ImageButton
    lateinit var clinicRecordBtn:ImageButton//내원 기록하기 버튼

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        mypagebutton = findViewById(R.id.mypagebutton)
        clinicRecordBtn=findViewById(R.id.clinicRecordBtn)

        //이미지 버튼 클릭시 마이페이지로 넘어가도록
        mypagebutton.setOnClickListener {
            val intent = Intent(this, MyPage::class.java)
            startActivity(intent)
        }

        //내원 기록하기 버튼 클릭시 캘린터 뷰 화면으로 넘어가기
        clinicRecordBtn.setOnClickListener {
            val intent=Intent(this,ClinicRecord::class.java)
            startActivity(intent)
        }


    }
}

