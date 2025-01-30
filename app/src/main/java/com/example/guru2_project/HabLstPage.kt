package com.example.guru2_project

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.WindowManager.LayoutParams
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.util.Calendar

class HabLstPage : AppCompatActivity() {
    lateinit var btnBckToMain : ImageButton // 메인 화면 이동 Button
    lateinit var btnInitHab : Button        // 체크리스트 전체 삭제 Button

    // "오늘의 목표 리스트를 등록해주세요" -> HabRegPage(나의 생활 체크 등록) 페이지로 이동 위한 Button
    lateinit var btnToHabReg : ImageButton

    lateinit var habLstLayout : LinearLayout // 생활 습관 리스트(CheckBox)가 나열될 LinearLayout

    lateinit var dbManager : DBManager      // DBManger 객체
    lateinit var sqlitedb : SQLiteDatabase  // SQLiteDatabase 객체


    // habNameLst, habTimeLst, habCntLst -> 생활 습관 리스트에 나열되는 CheckBox의 내용에 "습관 이름, 습관 시간대, 습관 자세한 내용"으로 세팅해주기 위해 필요
    lateinit var habNameLst : MutableList<String>       // habitTBL(생활 습관 테이블)의 habit_name 필드의 값만 저장한 가변 리스트
    lateinit var habTimeLst : MutableList<String>       // habitTBL(생활 습관 테이블)의 habit_time 필드의 값만 저장한 가변 리스트
    lateinit var habCntLst : MutableList<String>        // habitTBL(생활 습관 테이블)의 habit_content 필드의 값만 저장한 가변 리스트
    lateinit var habDayOfWeekLst : MutableList<String>  // habitTBL(생활 습관 테이블)의 habit_day_of_week 필드의 값만 저장한 가변 리스트
    lateinit var habCheckBoxLst : MutableList<CheckBox> // 화면에 표시될 생활 습관 CheckBox를 담을 가변 리스트

    var habitNum: Int = 0               // habitTBL habit_num (PRIMARY KEY) 필드의 값을 가져올 변수
    lateinit var habName: String        // habNameLst에 원소를 추가하기 위해 필요한 변수
    lateinit var habTime: String        // habTimeLst에 원소를 추가하기 위해 필요한 변수
    lateinit var habCnt: String         // habCntLst에 원소를 추가하기 위해 필요한 변수
    lateinit var habDayOfWeek: String   // habDayOfWeekLst에 원소를 추가하기 위해 필요한 변수
    lateinit var habDate: String        // 사용자가 각각의 체크박스를 마지막으로 클릭한 시점을 저장할 변수
    var habCheck: Int? = null           // habitTBL habit_check 필드의 값을 가져올 변수

    lateinit var todayDayOfWeek: String // 실행한 시간의 요일 -> 습관 리스트에 오늘 요일에 해당하는 CheckBox만 표시해주기 위해 필요한 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hab_lst_page)

        // Layout의 위젯 ID와 앞서 선언한 변수 연결
        btnBckToMain = findViewById(R.id.btnBckToMain)
        btnInitHab = findViewById(R.id.btnInitHab)

        btnToHabReg = findViewById(R.id.btnToHabReg)    // HabRegPage (생활 습관 등록 화면) 으로 이동하는 버튼 -> "오늘의 목표 리스트를 등록해주세요"
        habLstLayout = findViewById(R.id.habLstLayout)  // 목표 체크리스트가 표시될 레이아웃

        refreshHabList() // 나의 생활 체크리스트 목록 표시해주는 함수 호출

        btnToHabReg.setOnClickListener { // "오늘의 목표 리스트를 등록해주세요" 버튼 클릭 시
            // HabRegPage(생활 습관 등록 화면)으로 Intent 전달하며 화면 전환
            var intent = Intent(this, HabRegPage::class.java)
            startActivity(intent)
        }

        btnBckToMain.setOnClickListener { // 이전 아이콘 버튼 클릭 시
            // MainPage(메인 화면)으로 Intent 전달하며 화면 전환
            val intent = Intent(this, MainPage::class.java)
            startActivity(intent)
        }

        btnInitHab.setOnClickListener { // 전체 삭제 버튼 클릭 시
            var nowUserID = getCurrentUserId() // 함수 호출하여 현재 로그인한 사용자 ID 가져오기

            // 값을 읽어오기만 할 것이므로 읽기 전용으로 DB 열기
            dbManager = DBManager(this, "userDB", null, 17)
            sqlitedb = dbManager.readableDatabase

            // habitTBL에서 레코드 삭제 (user_id 필드의 값 == 현재 로그인한 사용자의 ID인 경우에만)
            sqlitedb.execSQL("DELETE FROM habitTBL WHERE user_id = '" + nowUserID + "';")
            habLstLayout.removeAllViews() // 레이아웃에서도 모든 View 객체 지우기

            // 생활 습관(목표) 체크리스트가 삭제되었다는 토스트 메시지 출력
            Toast.makeText(this@HabLstPage, "목표 체크리스트 전체 삭제됨", Toast.LENGTH_SHORT).show()

            sqlitedb.close()
            dbManager.close()
        }
    }
    // 오늘 무슨 요일인지 가져오는 함수
    fun getDayOfWeek(): String? {
        val cal: Calendar = Calendar.getInstance()      // Calendar 인스턴스화
        var todayDayOfWeek: String? = null              // 오늘 요일 (앱 실행일 기준)을 저장할 변수 초기화
        val nWeek: Int = cal.get(Calendar.DAY_OF_WEEK)  // 요일 값 (Int 타입) 가져오기

        if (nWeek == 1) {        // 요일 값 = 1 -> 일요일
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
        return todayDayOfWeek   // 오늘 무슨 요일인지 반환
    }

    // 나의 생활 체크리스트 하나씩 추가하는 함수
    private fun refreshHabList() {
        habLstLayout.removeAllViews() // 우선, 해당 레이아웃에서 모든 뷰 삭제 후 진행

        var nowUserID = getCurrentUserId() // 함수 호출하여 현재 로그인한 사용자 ID 가져오기

        // 값을 Update해야 하므로 쓰기 전용으로 DB 열기
        dbManager = DBManager(this, "userDB", null, 17)
        sqlitedb = dbManager.writableDatabase

        todayDayOfWeek = getDayOfWeek().toString() // 오늘 요일 알아내는 함수 호출

        // habitTBL(나의 생활 습관 등록 테이블)의 값 업데이트
        // <하루가 지난 상태>인 경우 -> 즉, habit_date 필드에 저장된 날짜와 오늘 날짜가 다름 -> [habit_check] 필드 값을 0으로, [habit_date] 필드 값을 오늘 날짜로 업데이트
        // 하루가 지나면 이미 체크가 되어있는 체크박스들은 전부 초기화 필요함. 그럴려면 마지막으로 앱을 실행한 날짜가 DB에 주기적으로 기록이 되어야 함.
        // 따라서,
        // 1. 첫번째 execSQL문 : habit_check 필드의 값을 언체크 상태(0)으로 업데이트
        // 2. 두번째 execSQL문 : habit_date 필드에 오늘 날짜를 주기적으로 기록해주기.
        sqlitedb.execSQL("UPDATE habitTBL SET habit_check = 0 WHERE habit_date != '${LocalDate.now()}';")
        sqlitedb.execSQL("UPDATE habitTBL SET habit_date = '${LocalDate.now()}' WHERE habit_date != '${LocalDate.now()}';")

        // 사용자 ID가 동일하며, 앱을 실행한 요일이 포함된 체크리스트만 읽어옴
        val cursor = sqlitedb.rawQuery(
            "SELECT * FROM habitTBL WHERE habit_day_of_week LIKE '%$todayDayOfWeek%' AND user_id = '$nowUserID';",
            null
        )

        // habitTBL(테이블)의 각각의 필드의 값을 저장할 가변 리스트 초기화
        habNameLst = mutableListOf()
        habTimeLst = mutableListOf()
        habCntLst = mutableListOf()
        habDayOfWeekLst = mutableListOf()
        habCheckBoxLst = mutableListOf()

        while (cursor.moveToNext()) {
            // 테이블의 각각의 필드의 값을 우선 변수에 저장
            habitNum = cursor.getInt(cursor.getColumnIndexOrThrow("habit_num"))
            habName = cursor.getString(cursor.getColumnIndexOrThrow("habit_name"))
            habTime = cursor.getString(cursor.getColumnIndexOrThrow("habit_time"))
            habDate = cursor.getString(cursor.getColumnIndexOrThrow("habit_date"))
            habCnt = cursor.getString(cursor.getColumnIndexOrThrow("habit_content"))
            habCheck = cursor.getInt(cursor.getColumnIndexOrThrow("habit_check"))


            // CheckBox 객체를 동적으로 추가하여 속성 적용
            val checkBox = CheckBox(this).apply {
                // checkBox의 text 값에 들어갈 spannable
                val spannable = SpannableString("$habName $habCnt\n$habTime") // habName은 생활 습관 이름, habCnt는 생활 습관의 자세한 내용, habTime은 생활 습관 예정 시간

                // 나의 생활 습관 이름 스타일 지정 - Bold체, 크기는 80px
                spannable.setSpan(StyleSpan(Typeface.BOLD), 0, habName.length, 0)
                spannable.setSpan(AbsoluteSizeSpan(80), 0, habName.length, 0)

                // 나의 생활 습관 자세한 내용 스타일 지정 - ITALIC체, 크기는 40px, padding 속성 적용
                spannable.setSpan(StyleSpan(Typeface.ITALIC), habName.length + 1, habName.length + 1 + habCnt.length, 0)
                spannable.setSpan(AbsoluteSizeSpan(40), habName.length + 1, habName.length + 1 + habCnt.length, 0)
                spannable.setSpan(setPadding(20,0,0,0), habName.length + 1, habName.length + 1 + habCnt.length, 0)

                // 나의 생활 습관 예정 시간 text color와 padding 설정
                spannable.setSpan(ForegroundColorSpan(Color.parseColor("#272D4F")), habName.length + 1 + habCnt.length + 1, spannable.length, 0)
                spannable.setSpan(setPadding(20,0,0,0), habName.length + 1 + habCnt.length + 1, spannable.length, 0)

                text = spannable // text에 들어갈 내용을 담은 spannable을 text로 넘겨줌
                buttonDrawable = null // checkBox 아이콘은 표시하지 않음

                setPadding(100, 0, 0, 0) // checkBox의 padding 설정

                // habitTBL(테이블)에서 체크 여부를 알아낸 후, 해당 checkBox의 isChecked 설정 변경해줌.
                setOnCheckedChangeListener(null) // 리스너 해제
                if(habCheck == 1) {
                    isChecked = true
                }
                else {
                    isChecked = false
                }
                setBackgroundResource(R.drawable.med_lst_checkbox_selector) // 체크 여부에 따른 backGround 이미지 변경

                tag = habitNum // habitNum(현재 체크박스의 고유 번호) 값을 tag에 저장

                // 체크 상태가 변경된 경우(즉, 사용자가 체크박스를 클릭한 경우) 이벤트 처리
                setOnCheckedChangeListener { _, isChecked ->
                    setBackgroundResource(R.drawable.med_lst_checkbox_selector) // background 이미지 우선 변경
                    val habitNumForUpDate = tag as Int // 지역 변수 선언 및 "체크박스 고유 번호"로 초기화
                    updateMediCheck(habitNumForUpDate, isChecked) // DB의 habitTBL의 check 여부를 저장하는 함수 호출 -> 매개변수로 "체크박스 고유 번호", "체크 여부"를 넘겨줌
                    Log.d("habitCheck", "habitNumForUpDate: $habitNumForUpDate, isChecked: $isChecked") // 매개변수 값이 정확하게 넘어갔는지 확인하기 위해 로그 출력
                }
                val params = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT) // checkBox를 담을 레이아웃 높이와 너비 지정
                // 체크 박스 간의 위아래 간격 조정을 위해 위쪽 여백&아래쪽 여백 설정
                params.topMargin = 20
                params.bottomMargin = 20
                layoutParams = params
            }
            habCheckBoxLst.add(checkBox) // 나의 생활 체크리스트에 해당 체크박스 추가
            habLstLayout.addView(checkBox) // 화면에 해당 체크박스 추가하여 보여줌
        }
        cursor.close()
        sqlitedb.close()
        dbManager.close()
    }

    // DB의 habitTBL에 check 여부를 저장하는 함수
    private fun updateMediCheck(habitNum: Int, isChecked: Boolean) {
        var nowUserID = getCurrentUserId() // 현재 로그인한 사용자 ID 가져오기

        // 값을 업데이트해야 하므로 읽기 전용으로 DB 열기
        dbManager = DBManager(this, "userDB", null, 17)
        val sqlitedb = dbManager.writableDatabase

        // 이미 닫힌 DB를 reopen 하는 에러를 방지하기 위해 try-catch-finally로 작성
        try { // CheckBox의 체크 상태에 따라 habitTBL의 체크 여부 필드(habit_check)에 값을 저장
            sqlitedb.execSQL(
                "UPDATE habitTBL SET habit_check = ${if (isChecked) 1 else 0} WHERE habit_num = '$habitNum' AND user_id = '$nowUserID';"
            )
        } catch (e: Exception) { // 예외 처리
            e.printStackTrace()
        } finally { // 무조건 실행 -> DB 닫기
            sqlitedb.close()
            dbManager.close()
        }
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
}