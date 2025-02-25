package com.example.guru2_project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SussessPage : AppCompatActivity() {
    lateinit var signupCheckButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sussess_page)

        signupCheckButton = findViewById(R.id.signupCheckButton)

        //회원가입 성공 확인 버튼 클릭 시 로그인 페이지로 이동
        signupCheckButton.setOnClickListener {
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
        }
    }
}