package com.example.guru2_project

import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class MyPage : AppCompatActivity() {

    lateinit var dbManager: DBManager
    lateinit var logoutButton: ImageButton
    lateinit var userNameTextView: TextView
    lateinit var userTypeTextView: TextView
    lateinit var userBirthTextView :TextView

    lateinit var dano : Button // 메인화면으로 가는 "DANO" 버튼

    // "아직 마치지 못한 일" 박스 관련 변수
    lateinit var todoLayout: LinearLayout // 할 일 목록을 담을 레이아웃
    lateinit var sqlitedb : SQLiteDatabase

    // 다노 커뮤니티 버튼 변수
    lateinit var btnToCS : ImageButton // 고객센터 버튼
    lateinit var btnToInsta : ImageButton // 다노 인스타그램 버튼
    lateinit var btnToYoutube : ImageButton // 다노 유튜브 채널 버튼

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        dano = findViewById(R.id.dano) // 메인 화면으로 가는 Dano 버튼
        logoutButton = findViewById(R.id.logoutButton)
        userNameTextView = findViewById(R.id.userNameTextView)
        userTypeTextView = findViewById(R.id.userTypeTextView)
        userBirthTextView = findViewById(R.id.userBirthTextView)

        // 다노 커뮤니티 버튼 변수 연결
        btnToCS = findViewById(R.id.btnToCS) // 고객센터 버튼
        btnToInsta = findViewById(R.id.btnToInsta) // 다노 인스타그램 버튼
        btnToYoutube = findViewById(R.id.btnToYoutube) // 다노 유튜브 채널 버튼

        todoLayout = findViewById(R.id.todoLayout) // "아직 마치지 못한 일" 박스 관련 변수 연결
        refreshTodoList() // "아직 마치지 못한 일"에 텍스트를 추가하는 함수 호출

        dbManager = DBManager(this, "userDB", null, 17)
        //버전 6으로 통일 , 생년월일 추가하면 버전 7까지 버전 올라갈 것 같음

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

        // DANO 버튼 클릭 시 메인 페이지(메인 화면)으로 이동
        dano.setOnClickListener {
            var intent = Intent(this, MainPage::class.java)
            startActivity(intent)
        }

        // 하단의 버튼 3개(다노 커뮤니티 관련 버튼) -> 클릭 시, 개발 중 페이지로 이동
        btnToCS.setOnClickListener { // 고객센터 버튼 클릭 시
            var intent = Intent(this, DevPage::class.java)
            startActivity(intent)
        }
        btnToInsta.setOnClickListener { // 다노 인스타그램 버튼 클릭 시
            var intent = Intent(this, DevPage::class.java)
            startActivity(intent)
        }
        btnToYoutube.setOnClickListener { // 다노 유튜브 채널 버튼 클릭 시
            var intent = Intent(this, DevPage::class.java)
            startActivity(intent)
        }
    }

    // "아직 마치지 못한 일" TextView 동적 추가 함수
    private fun refreshTodoList() {
        todoLayout.removeAllViews() // 일단 TextView를 담을 레이아웃의 모든 뷰를 삭제하고 시작

        var nowUserID = getCurrentUserId() // 현재 로그인한 사용자의 ID 가져오기
        var todayDayOfWeek = getDayOfWeek().toString() // 오늘 무슨 요일인지 가져오기

        // TextView 동적으로 추가하며 속성 적용
        val todoLstTxt = TextView(this).apply {
            text = "아직 마치지 못한 일" // TextView 내용 설정
            setTextAppearance(R.style.mypage_todo_title_theme) // TextView 스타일 지정
        }
        todoLayout.addView(todoLstTxt) // 해당 텍스트뷰 레이아웃에 추가하기

        // 값을 가져오기만 할 것이므로 DB 읽기 전용으로 열기
        dbManager = DBManager(this, "userDB", null, 17)
        sqlitedb = dbManager.readableDatabase

        // 조건 : 오늘 요일에 해당 & check 속성이 0(즉, 아직 체크하지 않은 것) & 현재 로그인한 사용자의 ID에 해당하는 것
        // mediTBL에서 위 조건에 맞는 레코드만 가져옴
        val cursorMedi = sqlitedb.rawQuery(
            "SELECT * FROM mediTBL WHERE medi_day_of_week LIKE '%$todayDayOfWeek%' AND medi_check = 0 AND user_id = '$nowUserID';",
            null
        )

        while (cursorMedi.moveToNext()) {
            val mediName = cursorMedi.getString(cursorMedi.getColumnIndexOrThrow("medi_name")) // 테이블의 medi_name(복용약 이름) 필드의 값 저장

            val textView = TextView(this).apply {
                text = mediName // TextView의 text를 "복용약 이름"으로 설정
                setTextAppearance(R.style.mypage_todo_txt_theme) // TextView 스타일 지정
            }
            todoLayout.addView(textView) // 속성 적용된 TextView를 앞서 지정한 Layout에 동적으로 추가
        }

        // 조건 : 오늘 요일에 해당 & check 속성이 0(즉, 아직 체크하지 않은 것) & 현재 로그인한 사용자의 ID에 해당하는 것
        // habitTBL에서 위 조건에 맞는 레코드만 가져옴
        val cursorHabit = sqlitedb.rawQuery(
            "SELECT * FROM habitTBL WHERE habit_day_of_week LIKE '%$todayDayOfWeek%' AND habit_check = 0 AND user_id = '$nowUserID';",
            null
        )

        while (cursorHabit.moveToNext()) {
            val habitName = cursorHabit.getString(cursorHabit.getColumnIndexOrThrow("habit_name")) // 테이블의 habit_name(습관 이름) 필드의 값 저장

            val textView = TextView(this).apply {
                text = habitName // TextView의 text를 "습관 이름"으로 설정
                setTextAppearance(R.style.mypage_todo_txt_theme) // TextView 스타일 지정
            }
            todoLayout.addView(textView) // 속성 적용된 TextView를 앞서 지정한 Layout에 동적으로 추가
        }
        // 사용한 모든 Cursor와 DB 닫기
        cursorHabit.close()
        cursorMedi.close()
        sqlitedb.close()
        dbManager.close()
    }

    // 현재 로그인한 사용자 ID 가져오는 함수
    private fun getCurrentUserId(): String? {
        var userId: String? = null // 로그인 사용자 ID를 저장할 변수

        // 값을 가져오기만 할 것이므로 읽기 전용으로 DB 열기
        dbManager = DBManager(this, "userDB", null, 17)
        val sqlitedb = dbManager.readableDatabase

        val cursorId = sqlitedb.rawQuery("SELECT * FROM session;", null) // session 테이블 값 읽어오기
        if (cursorId.moveToFirst()) {
            userId = cursorId.getString(cursorId.getColumnIndexOrThrow("userId")) // userID에 현재 로그인한 사용자의 ID 저장
        }
        cursorId.close()
        sqlitedb.close()

        return userId // 가져온 로그인 사용자 ID 리턴
    }

    // 오늘 무슨 요일인지 가져오는 함수
    fun getDayOfWeek(): String? {
        val cal: Calendar = Calendar.getInstance()      // Calendar 인스턴스화
        var todayDayOfWeek: String? = null              // 오늘 요일 (앱 실행일 기준)을 저장할 변수 초기화
        val nWeek: Int = cal.get(Calendar.DAY_OF_WEEK)  // 요일 값 (Int 타입) 가져오기

        if (nWeek == 1) {      // 요일 값 = 1 -> 일요일
            todayDayOfWeek = "sun"
        } else if (nWeek == 2) { // 요일 값 = 2 -> 월요일
            todayDayOfWeek = "mon"
        } else if (nWeek == 3) { // 요일 값 = 3 -> 화요일
            todayDayOfWeek = "tue"
        } else if (nWeek == 4) { // 요일 값 = 4 -> 수요일
            todayDayOfWeek = "wed"
        } else if (nWeek == 5) { // 요일 값 = 5 -> 목요일
            todayDayOfWeek = "thur"
        } else if (nWeek == 6) { // 요일 값 = 6 -> 금요일
            todayDayOfWeek = "fri"
        } else if (nWeek == 7) { // 요일 값 = 7 -> 토요일
            todayDayOfWeek = "sat"
        }
        return todayDayOfWeek // 오늘 무슨 요일인지 반환
    }
}
