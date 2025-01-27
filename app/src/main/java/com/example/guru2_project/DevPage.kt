package com.example.guru2_project

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class DevPage : AppCompatActivity() {
    lateinit var btnToClose : ImageButton // 닫기 버튼
    lateinit var btnToOK : ImageButton // 확인 버튼
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dev_page)

        btnToClose = findViewById(R.id.btnToClose)
        btnToOK = findViewById(R.id.btnToOK)

        // 닫기 버튼 클릭 시 이전 화면으로 이동
        btnToClose.setOnClickListener {
            finish()
        }
        // 확인 버튼 클릭 시 이전 화면으로 이동
        btnToOK.setOnClickListener {
            finish()
        }
    }
}