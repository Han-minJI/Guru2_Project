package com.example.guru2_project

import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.media.Image
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.guru2_project.DBManager

class LoginPage : AppCompatActivity() {

    lateinit var dbManager: DBManager //DB
    lateinit var sqlitedb: SQLiteDatabase

    //id, 비밀번호
    lateinit var userid: EditText
    lateinit var userpasswd: EditText

    //로그인, 회원가입, 비밀번호 찾기 버튼
    lateinit var loginbutton: ImageButton
    lateinit var signupbutton: ImageButton
    lateinit var forgetpasswdButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)

        userid = findViewById(R.id.userId)
        userpasswd = findViewById(R.id.userPasswd)

        loginbutton = findViewById(R.id.loginbutton)
        signupbutton = findViewById(R.id.gotoSignupButton)
        forgetpasswdButton = findViewById(R.id.forgetpasswdButton)

        // DBManager 초기화
        dbManager = DBManager(this, "userDB", null, 18)
        // 버전 6으로 통일 , 생년월일 추가하면 버전 7까지 버전 올라갈 것 같음

        // 회원가입 페이지로 이동
        signupbutton.setOnClickListener {
            val intent = Intent(this, SignupPage::class.java)
            startActivity(intent)
        }

        // 비밀번호찾기 페이지로 이동(개발 중 페이지로 이동)
        forgetpasswdButton.setOnClickListener {
            val intent = Intent(this, DevPage::class.java)
            startActivity(intent)
        }

        // 로그인 처리
        loginbutton.setOnClickListener {
            val userId = userid.text.toString().trim()
            val userPasswd = userpasswd.text.toString().trim()

            if (userId.isEmpty() || userPasswd.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sqlitedb = dbManager.readableDatabase
            val cursor: Cursor = sqlitedb.rawQuery(
                "SELECT * FROM member WHERE userId = ? AND userPasswd = ?",
                arrayOf(userId, userPasswd)
            )

            var loginSuccess = false

            // 로그인 성공 여부 확인
            if (cursor.moveToNext()) {
                loginSuccess = true
            }
            cursor.close()
            sqlitedb.close()

            if (loginSuccess) {
                // 세션 데이터 중복 방지
                sqlitedb = dbManager.writableDatabase
                try {
                    // 기존 세션 삭제
                    sqlitedb.execSQL("DELETE FROM session")

                    // 새로운 세션 정보 삽입
                    sqlitedb.execSQL(
                        "INSERT INTO session (userId, userPasswd) VALUES (?, ?)",
                        arrayOf(userId, userPasswd)
                    )
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()

                    // 메인 페이지로 이동
                    val intent = Intent(this, MainPage::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "세션 저장 중 오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    sqlitedb.close()
                }
            } else {
                Toast.makeText(this, "아이디 혹은 비밀번호가 틀렸습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
