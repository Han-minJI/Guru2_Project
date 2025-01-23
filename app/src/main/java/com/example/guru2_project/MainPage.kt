package com.example.guru2_project

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class MainPage : AppCompatActivity() {
    lateinit var mypagebutton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        mypagebutton = findViewById(R.id.mypagebutton)

        //이미지 버튼 클릭시 마이페이지로 넘어가도록
        mypagebutton.setOnClickListener {
            val intent = Intent(this, MyPage::class.java)
            startActivity(intent)
        }
    }
}

