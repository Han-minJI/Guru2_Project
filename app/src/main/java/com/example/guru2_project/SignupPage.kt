package com.example.guru2_project

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.guru2_project.DBManager
import com.example.guru2_project.R

class SignupPage : AppCompatActivity() {

    lateinit var dbManager: DBManager // DBManager
    lateinit var sqlitedb: SQLiteDatabase

    lateinit var userid: EditText
    lateinit var userpasswd: EditText
    lateinit var username: EditText
    lateinit var typeSpinner: Spinner



    lateinit var signupbtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_page)

        userid = findViewById(R.id.userId)
        userpasswd = findViewById(R.id.userPasswd)
        username = findViewById(R.id.userName)
        typeSpinner = findViewById(R.id.typeSpinner)
        signupbtn = findViewById(R.id.signupbutton)

        //생년월일
        val years = listOf("YYYY")+(1920..2025).toList().map { it.toString() } // 1900년부터 2025년까지
        val months = listOf("MM")+(1..12).toList().map { it.toString().padStart(2, '0') } // 01 ~ 12
        val days = listOf("DD") +(1..31).toList().map { it.toString().padStart(2, '0') } // 01 ~ 31

// 연도 Spinner
        val yearAdapter = ArrayAdapter(this, R.layout.custom_spinner_item, years)
        yearAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        val yearSpinner: Spinner = findViewById(R.id.yearSpinner)
        yearSpinner.adapter = yearAdapter

// 월 Spinner
        val monthAdapter = ArrayAdapter(this, R.layout.custom_spinner_item, months)
        monthAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        val monthSpinner: Spinner = findViewById(R.id.monthSpinner)
        monthSpinner.adapter = monthAdapter

// 일 Spinner
        val dayAdapter = ArrayAdapter(this, R.layout.custom_spinner_item, days)
        dayAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        val daySpinner: Spinner = findViewById(R.id.daySpinner)
        daySpinner.adapter = dayAdapter



        dbManager = DBManager(this, "userDB", null, 14)

        // Spinner 데이터 설정
        val types = listOf("당뇨병 전단계", "제1형 당뇨병", "제2형 당뇨병")
        val adapter = ArrayAdapter(this, R.layout.custom_spinner_item, types)
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        typeSpinner.adapter = adapter

        //회원가입 버튼 클릭
        signupbtn.setOnClickListener {
            val userId = userid.text.toString()
            val userPasswd = userpasswd.text.toString()
            val userName = username.text.toString()
            val userType = typeSpinner.selectedItem.toString()
            val birthYear = yearSpinner.selectedItem.toString()
            val birthMonth = monthSpinner.selectedItem.toString()
            val birthDay = daySpinner.selectedItem.toString()
            val birthDate = "$birthYear-$birthMonth-$birthDay"

            if (userId.isEmpty() || userPasswd.isEmpty() || userName.isEmpty()) {
                Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sqlitedb = dbManager.readableDatabase

            val cursor = sqlitedb.rawQuery("SELECT * FROM member WHERE userId = '$userId'", null)

            if (cursor.count > 0) {
                // 아이디가 이미 존재하는 경우
                Toast.makeText(this, "이미 존재하는 아이디입니다.", Toast.LENGTH_SHORT).show()
            } else {
                // 아이디가 중복되지 않은 경우 회원가입 처리
                sqlitedb = dbManager.writableDatabase
                sqlitedb.execSQL("INSERT INTO member VALUES ('$userId', '$userPasswd', '$userName', '$birthDate', '$userType')")

                Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()

                // 성공 시 성공 페이지로 이동
                val intent = Intent(this, SussessPage::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
        }

    }
}