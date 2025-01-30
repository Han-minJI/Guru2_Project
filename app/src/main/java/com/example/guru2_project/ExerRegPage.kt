package com.example.guru2_project

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate

class ExerRegPage : AppCompatActivity() {
    // 사용자가 입력하는 값과 관련된 변수
    lateinit var edtExerName : EditText // 어떤 운동을 할 것인지
    lateinit var edtExerTime : EditText // 몇 분 운동을 할 것인지

    // 버튼 관련 변수
    lateinit var btnExerReg : ImageButton // 등록하기 버튼
    lateinit var btnBckToLst : ImageButton // 이전 화면으로 가는 버튼

    lateinit var dbManager : DBManager
    lateinit var sqlitedb : SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exer_reg_page)

        // 앞서 선언한 변수와 layout 파일의 위젯 연결
        edtExerName = findViewById(R.id.edtExerName) // 어떤 운동을 할 건지
        edtExerTime = findViewById(R.id.edtExerTime) // 몇 분 운동할 건지
        btnExerReg = findViewById(R.id.btnExerReg) // 등록하기 버튼
        btnBckToLst = findViewById(R.id.btnBckToLst) // 이전 화면 버튼

        // 이전 화면 버튼 클릭 시, 나의 운동 기록 체크리스트 화면으로 Intent 전달하며 화면 전환
        btnBckToLst.setOnClickListener {
            val intent = Intent(this, ExerLstPage::class.java)
            startActivity(intent)
        }

        // 제일 하단의 "등록하기" 버튼 클릭 시
        btnExerReg.setOnClickListener {
            var nowUserID = "" // 현재 사용자 ID를 저장할 변수

            // DB 읽기 전용으로 불러오기
            dbManager = DBManager(this, "userDB", null, 18)
            sqlitedb = dbManager.readableDatabase

            // session 테이블에서 현재 로그인한 사용자의 ID 가져오기
            val cursor = sqlitedb.rawQuery("SELECT * FROM session;", null)

            while (cursor.moveToNext()) {
                nowUserID = cursor.getString(cursor.getColumnIndexOrThrow("userId"))
            }
            cursor.close()

            // DB 쓰기 전용으로 불러오기 (등록할 때 값을 Insert 해야 하므로)
            dbManager = DBManager(this, "userDB", null, 18)
            sqlitedb = dbManager.writableDatabase

            // exerTBL에 사용자가 입력한 내용(운동 이름, 운동 시간)을 저장
            // 사용자 ID, 오늘 날짜, 체크 여부(Default값 0 -> false)도 함께 저장
            sqlitedb.execSQL(
                "INSERT INTO exerTBL (user_id, exer_name, exer_time, exer_date, exer_check) VALUES ('" +
                        nowUserID + "', '" +
                        edtExerName.text.toString() + "', '" +
                        edtExerTime.text.toString() + "', '" +
                        LocalDate.now().toString() + "', 0)"
            )
            sqlitedb.close()

            // 최종적으로, 운동 체크리스트 화면으로 Intent 전달하며 화면 전환
            val intent = Intent(this, ExerLstPage::class.java)
            startActivity(intent)
        }
    }
}