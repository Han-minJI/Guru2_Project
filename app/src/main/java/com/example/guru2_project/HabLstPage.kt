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
    lateinit var btnInitHab : Button // 체크리스트 전체 삭제 Button

    // "복용하시는 약을 등록해주세요" -> MedRegPage(복약 등록) 페이지로 이동 위한 Button
    lateinit var btnToHabReg : ImageButton

    lateinit var habLstLayout : LinearLayout // 복약 리스트(CheckBox)가 나열될 LinearLayout

    // !!! DBManager에 코드 추가 -> 버전 업글 -> 주석 해제 !!!
    lateinit var dbManager : DBManager    // DBManger 객체
    lateinit var sqlitedb : SQLiteDatabase   // SQLiteDatabase 객체
    // !!! DBManager에 코드 추가 -> 버전 업글 -> 주석 해제 !!!


    // medNameLst, medTimeLst -> 복약 리스트에 나열되는 CheckBox의 내용을 "복용할 약, 복용 예정 시간"으로 세팅해주기 위해 필요
    lateinit var habNameLst : MutableList<String>       // mediTBL(복약 테이블)의 medi_name 필드의 값만 저장한 가변 리스트
    lateinit var habTimeLst : MutableList<String>       // mediTBL(복약 테이블)의 medi_time 필드의 값만 저장한 가변 리스트
    lateinit var habCntLst : MutableList<String>
    lateinit var habDayOfWeekLst : MutableList<String>  // mediTBL(복약 테이블)의 medi_day_of_week 필드의 값만 저장한 가변 리스트
    lateinit var habCheckBoxLst : MutableList<CheckBox> // 화면에 표시될 복약 CheckBox를 담을 가변 리스트

    var habitNum: Int = 0                // mediTBL의 medi_num (PRIMARY KEY) 필드의 값을 가져올 변수
    lateinit var habName: String        // medNameLst에 원소를 추가하기 위해 필요한 변수
    lateinit var habTime: String        // medTimeLst에 원소를 추가하기 위해 필요한 변수
    lateinit var habCnt: String
    lateinit var habDayOfWeek: String   // medDayOfWeekLst에 원소를 추가하기 위해 필요한 변수
    lateinit var habDate: String        // 사용자가 각각의 체크박스를 마지막으로 클릭한 시점을 저장할 변수
    var habCheck: Int? = null

    lateinit var todayDayOfWeek: String // 실행한 시간의 요일 -> 복약 리스트에 오늘 요일에 해당하는 CheckBox만 표시해주기 위해 필요한 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hab_lst_page)

        // Layout의 위젯 ID와 앞서 선언한 변수 연결
        btnBckToMain = findViewById(R.id.btnBckToMain)
        btnInitHab = findViewById(R.id.btnInitHab)

        btnToHabReg = findViewById(R.id.btnToHabReg)    // MedRegPage (복용약 등록 화면) 으로 이동하는 버튼 -> 복용하시는 약을 등록해주세요
        habLstLayout = findViewById(R.id.habLstLayout)  // 복용 체크리스트가 표시될 레이아웃

        // 화면에 표시되어있던 체크리스트를 우선 없애고 시작
        onResume()

        btnToHabReg.setOnClickListener { // "복용하시는 약을 등록해주세요" 버튼 클릭 시
            // MedRegPage(복약 등록 화면)으로 Intent 전달하며 화면 전환
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

            // !!! DBManager에 코드 추가 -> 버전 업글 -> 주석 해제 !!!
            // 값을 읽어오기만 할 것이므로 읽기 전용으로 DB 열기
            dbManager = DBManager(this, "userDB", null, 11)
            sqlitedb = dbManager.readableDatabase

            // mediTBL에서 레코드 삭제 (user_id 필드의 값 == 현재 로그인한 사용자의 ID인 경우에만)
            sqlitedb.execSQL("DELETE FROM habitTBL WHERE user_id = '" + nowUserID + "';")
            habLstLayout.removeAllViews() // 레이아웃에서도 모든 View 객체 지우기

            // 복약 체크리스트가 삭제되었다는 토스트 메시지 출력
            Toast.makeText(this@HabLstPage, "복약 체크리스트 전체 삭제됨", Toast.LENGTH_SHORT).show()

            sqlitedb.close()
            dbManager.close()
            // !!! DBManager에 코드 추가 -> 버전 업글 -> 주석 해제 !!!
        }
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

    // 다시 앱으로 돌아왔을 때 호출되는 함수 재정의
    override fun onResume() {
        super.onResume()

        // <화면 새로고침한 뒤, 복약 CheckBox 동적으로 추가> 함수 호출
        refreshMedList()
    }

    // 복약 체크리스트 하나씩 추가하는 함수
    private fun refreshMedList() {
        habLstLayout.removeAllViews() // 우선, 해당 레이아웃에서 모든 뷰 삭제 후 진행

        var nowUserID = getCurrentUserId() // 함수 호출하여 현재 로그인한 사용자 ID 가져오기

        // !!! DBManager에 코드 추가 -> 버전 업글 -> 주석 해제 !!!
        // 값을 Update해야 하므로 쓰기 전용으로 DB 열기
        dbManager = DBManager(this, "userDB", null, 11)
        sqlitedb = dbManager.writableDatabase

        todayDayOfWeek = getDayOfWeek().toString() // 오늘 요일 알아내는 함수 호출

        // mediTBL(복약 등록 테이블)의 값을 업데이트 (현재 로그인한 사용자와 관련된 레코드만)
        // <하루가 지난 상태>인 경우 -> [medi_check] 필드 값을 0으로, [medi_date] 필드 값을 오늘 날짜로 업데이트
        // 하루가 지나면 체크된 체크박스도 전부 초기화해줘야 함. 그럴려면 마지막으로 앱을 실행한 날짜를 알아야함
        // 따라서 medi_date 필드에 지금 현재 앱을 실행한 날짜를 주기적으로 기록해주는 것.
        sqlitedb.execSQL("UPDATE habitTBL SET habit_check = 0 WHERE habit_date != '${LocalDate.now()}' AND user_id = '$nowUserID';")
        sqlitedb.execSQL("UPDATE habitTBL SET habit_date = '${LocalDate.now()}' WHERE habit_date != '${LocalDate.now()}' AND user_id = '$nowUserID';")

        // 사용자 ID가 동일하며, 앱을 실행한 요일이 포함된 체크리스트만 읽어옴
        val cursor = sqlitedb.rawQuery(
            "SELECT * FROM habitTBL WHERE habit_day_of_week LIKE '%$todayDayOfWeek%' AND user_id = '$nowUserID';",
            null
        )

        // mediTBL(테이블)의 각각의 필드의 값을 저장할 가변 리스트 초기화
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
                val spannable = SpannableString("$habName $habCnt\n$habTime") // habName은 생활 습관 이름, habTime은 생활 습관 예정 시간
                spannable.setSpan(StyleSpan(Typeface.BOLD), 0, habName.length, 0) // 생활 습관 이름은 Bold체로 표시
                spannable.setSpan(AbsoluteSizeSpan(80), 0, habName.length, 0) // 생활 습관 이름은 80px로 지정

                spannable.setSpan(StyleSpan(Typeface.ITALIC), habName.length + 1, habName.length + 1 + habCnt.length, 0)
                spannable.setSpan(AbsoluteSizeSpan(40), habName.length + 1, habName.length + 1 + habCnt.length, 0)
                spannable.setSpan(setPadding(20,0,0,0), habName.length + 1, habName.length + 1 + habCnt.length, 0)

                // 생활 습관 예정 시간의 text color와 padding 설정
                spannable.setSpan(ForegroundColorSpan(Color.parseColor("#272D4F")), habName.length + 1 + habCnt.length + 1, spannable.length, 0)
                spannable.setSpan(setPadding(20,0,0,0), habName.length + 1 + habCnt.length + 1, spannable.length, 0)

                text = spannable // text에 들어갈 내용을 담은 spannable을 text로 넘겨줌
                buttonDrawable = null // checkBox 아이콘은 표시하지 않음

                setPadding(100, 0, 0, 0) // checkBox의 padding 설정

                // checkBox의 체크 속성 설정 - 사용자가 체크를 했으며, 오늘 체크를 한 경우에만 체크 상태 유지
                isChecked = (habCheck == 1 && habDate == LocalDate.now().toString())
                setBackgroundResource(R.drawable.med_lst_checkbox_selector) // 체크 여부에 따른 backGround 이미지 변경

                // 체크 상태가 변경된 경우(즉, 사용자가 체크박스를 클릭한 경우) 이벤트 처리
                setOnCheckedChangeListener { _, isChecked ->
                    updateMediCheck(habitNum, isChecked) // DB의 mediTBL에 check 여부를 저장하는 함수 호출
                }
                val params = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT) // checkBox를 담을 레이아웃 높이와 너비 지정
                // 아래쪽 여백 (체크 박스 간의 위아래 간격 조정을 위해 위쪽 여백&아래쪽 여백 설정
                params.topMargin = 20
                params.bottomMargin = 20
                layoutParams = params
            }
            habCheckBoxLst.add(checkBox) // 복약 체크리스트에 해당 체크박스 추가
            habLstLayout.addView(checkBox) // 화면에 해당 체크박스 추가하여 보여줌
        }
        cursor.close()
        sqlitedb.close()
        dbManager.close()
        // !!! DBManager에 코드 추가 -> 버전 업글 -> 주석 해제 !!!
    }

    // DB의 mediTBL에 check 여부를 저장하는 함수
    private fun updateMediCheck(habitNum: Int, isChecked: Boolean) {
        var nowUserID = getCurrentUserId() // 현재 로그인한 사용자 ID 가져오기

        // !!! DBManager에 코드 추가 -> 버전 업글 -> 주석 해제 !!!
        // 값을 업데이트해야 하므로 읽기 전용으로 DB 열기
        dbManager = DBManager(this, "userDB", null, 11)
        val sqlitedb = dbManager.writableDatabase

        // 이미 닫힌 DB를 reopen 하는 에러를 방지하기 위해 try-catch-finally로 작성
        try { //
            sqlitedb.execSQL(
                "UPDATE habitTBL SET habit_check = ${if (isChecked) 1 else 0} WHERE habit_num = '$habitNum' AND user_id = '$nowUserID';"
            )
        } catch (e: Exception) { // 예외 처리
            e.printStackTrace()
        } finally { // 무조건 실행 -> DB 닫기
            sqlitedb.close()
            dbManager.close()
        }
        // !!! DBManager에 코드 추가 -> 버전 업글 -> 주석 해제 !!!
    }

    // 현재 로그인한 사용자 ID 가져오는 함수
    private fun getCurrentUserId(): String? {
        var userId: String? = null // 로그인 사용자 ID를 저장할 변수

        // !!! DBManager에 코드 추가 -> 버전 업글 -> 주석 해제 !!!
        // 값을 가져오기만 할 것이므로 읽기 전용으로 DB 열기
        dbManager = DBManager(this, "userDB", null, 11)
        val sqlitedb = dbManager.readableDatabase

        val cursorId = sqlitedb.rawQuery("SELECT * FROM session;", null) // session 테이블 값 읽어오기
        if (cursorId.moveToFirst()) {
            userId = cursorId.getString(cursorId.getColumnIndexOrThrow("userId")) // userID에 현재 로그인한 사용자의 ID 저장
        }
        cursorId.close()
        sqlitedb.close()
        // !!! DBManager에 코드 추가 -> 버전 업글 -> 주석 해제 !!!

        return userId // 가져온 로그인 사용자 ID 리턴
    }
}