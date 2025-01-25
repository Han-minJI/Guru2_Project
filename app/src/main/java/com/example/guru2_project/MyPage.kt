package com.example.guru2_project

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MyPage : AppCompatActivity() {

    lateinit var dbManager: DBManager
    lateinit var logoutButton: ImageButton
    lateinit var userNameTextView: TextView
    lateinit var userTypeTextView: TextView
    lateinit var userBirthTextView :TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        logoutButton = findViewById(R.id.logoutButton)
        userNameTextView = findViewById(R.id.userNameTextView)
        userTypeTextView = findViewById(R.id.userTypeTextView)
        userBirthTextView = findViewById(R.id.userBirthTextView)

        dbManager = DBManager(this, "userDB", null, 8)  //버전 6으로 통일 , 생년월일 추가하면 버전 7까지 버전 올라갈 것 같음

        // 디버깅: 세션 데이터 출력
        val sessionCursor: Cursor = dbManager.readableDatabase.rawQuery("SELECT * FROM session", null)
        //session 테이블에 있는 아이디와 비밀번호를 member 테이블과 대조하여 정보 출력
        if (sessionCursor.moveToFirst()) {
            do {
                val userId = sessionCursor.getString(0)
                val userPasswd = sessionCursor.getString(1)

                // member 테이블에서 사용자 정보 가져오기
                val memberCursor: Cursor = dbManager.readableDatabase.rawQuery(
                    "SELECT userName,BirthDate, userType FROM member WHERE userId = ? AND userPasswd = ?",
                    arrayOf(userId, userPasswd)
                )

                if (memberCursor.moveToFirst()) {
                    val userName = memberCursor.getString(0)
                    val userBirth = memberCursor.getString(1)
                    val userType = memberCursor.getString(2)

                    // 사용자 정보 표시
                    userNameTextView.text = "$userName"
                    userBirthTextView.text = "$userBirth"
                    userTypeTextView.text = "$userType"
                } else {
                    Toast.makeText(this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
                memberCursor.close()
            } while (sessionCursor.moveToNext())
        } else {
            Toast.makeText(this, "세션 정보가 없습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
            finish()
        }
        sessionCursor.close()

        // 로그아웃 버튼
        logoutButton.setOnClickListener {
            val sessionDb = dbManager.writableDatabase
            sessionDb.execSQL("DELETE FROM session")
            sessionDb.close()

            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
            finish()
        }
    }
}
