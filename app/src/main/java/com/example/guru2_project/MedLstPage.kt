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

class MedLstPage : AppCompatActivity() {
    lateinit var btnBckToMain : ImageButton // 메인 화면 이동 Button
    lateinit var btnInitMed : Button // 체크리스트 전체 삭제 Button

    // "복용하시는 약을 등록해주세요" -> MedRegPage(복약 등록) 페이지로 이동 위한 Button
    lateinit var btnToMedReg : ImageButton

    lateinit var medLstLayout : LinearLayout // 복약 리스트(CheckBox)가 나열될 LinearLayout


    lateinit var dbManager : DBManager    // DBManger 객체
    lateinit var sqlitedb : SQLiteDatabase   // SQLiteDatabase 객체


    // medNameLst, medTimeLst -> 복약 리스트에 나열되는 CheckBox의 내용을 "복용할 약, 복용 예정 시간"으로 세팅해주기 위해 필요
    lateinit var medNameLst : MutableList<String>       // mediTBL(복약 테이블)의 medi_name 필드의 값만 저장한 가변 리스트
    lateinit var medTimeLst : MutableList<String>       // mediTBL(복약 테이블)의 medi_time 필드의 값만 저장한 가변 리스트
    lateinit var medDayOfWeekLst : MutableList<String>  // mediTBL(복약 테이블)의 medi_day_of_week 필드의 값만 저장한 가변 리스트
    lateinit var medCheckBoxLst : MutableList<CheckBox> // 화면에 표시될 복약 CheckBox를 담을 가변 리스트

    var mediNum: Int = 0                // mediTBL의 medi_num (PRIMARY KEY) 필드의 값을 가져올 변수
    lateinit var medName: String        // medNameLst에 원소를 추가하기 위해 필요한 변수
    lateinit var medTime: String        // medTimeLst에 원소를 추가하기 위해 필요한 변수
    lateinit var medDayOfWeek: String   // medDayOfWeekLst에 원소를 추가하기 위해 필요한 변수
    lateinit var medDate: String        // 사용자가 각각의 체크박스를 마지막으로 클릭한 시점을 저장할 변수
    var medCheck: Int? = null

    lateinit var todayDayOfWeek: String // 실행한 시간의 요일 -> 복약 리스트에 오늘 요일에 해당하는 CheckBox만 표시해주기 위해 필요한 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_med_lst_page)

        // Layout의 위젯 ID와 앞서 선언한 변수 연결
        btnBckToMain = findViewById(R.id.btnBckToMain)
        btnInitMed = findViewById(R.id.btnInitMed)

        btnToMedReg = findViewById(R.id.btnToMedReg)    // MedRegPage (복용약 등록 화면) 으로 이동하는 버튼 -> 복용하시는 약을 등록해주세요
        medLstLayout = findViewById(R.id.medLstLayout)  // 복용 체크리스트가 표시될 레이아웃

        refreshMedList()  // 나의 복약 체크리스트 목록 표시해주는 함수 호출

        btnToMedReg.setOnClickListener { // "복용하시는 약을 등록해주세요" 버튼 클릭 시
            // MedRegPage(복약 등록 화면)으로 Intent 전달하며 화면 전환
            var intent = Intent(this, MedRegPage::class.java)
            startActivity(intent)
        }

        btnBckToMain.setOnClickListener { // 이전 아이콘 버튼 클릭 시
            // MainPage(메인 화면)으로 Intent 전달하며 화면 전환
            val intent = Intent(this, MainPage::class.java)
            startActivity(intent)
        }

        btnInitMed.setOnClickListener { // 전체 삭제 버튼 클릭 시
            var nowUserID = getCurrentUserId() // 함수 호출하여 현재 로그인한 사용자 ID 가져오기

            // 값을 읽어오기만 할 것이므로 읽기 전용으로 DB 열기
            dbManager = DBManager(this, "userDB", null, 13)
            sqlitedb = dbManager.readableDatabase

            // mediTBL에서 레코드 삭제 (user_id 필드의 값 == 현재 로그인한 사용자의 ID인 경우에만)
            sqlitedb.execSQL("DELETE FROM mediTBL WHERE user_id = '" + nowUserID + "';")
            medLstLayout.removeAllViews() // 레이아웃에서도 모든 View 객체 지우기

            // 복약 체크리스트가 삭제되었다는 토스트 메시지 출력
            Toast.makeText(this@MedLstPage, "복약 체크리스트 전체 삭제됨", Toast.LENGTH_SHORT).show()

            sqlitedb.close()
            dbManager.close()

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

    // 복약 체크리스트 하나씩 추가하는 함수
    private fun refreshMedList() {
        medLstLayout.removeAllViews() // 우선, 해당 레이아웃에서 모든 뷰 삭제 후 진행

        var nowUserID = getCurrentUserId() // 함수 호출하여 현재 로그인한 사용자 ID 가져오기

        // 값을 Update해야 하므로 쓰기 전용으로 DB 열기
        dbManager = DBManager(this, "userDB", null, 13)
        sqlitedb = dbManager.writableDatabase

        todayDayOfWeek = getDayOfWeek().toString() // 오늘 요일 알아내는 함수 호출

        // mediTBL(복약 등록 테이블)의 값 업데이트
        // <하루가 지난 상태>인 경우 -> 즉, medi_date 필드에 저장된 날짜와 오늘 날짜가 다름 -> [medi_check] 필드 값을 0으로, [medi_date] 필드 값을 오늘 날짜로 업데이트
        // 하루가 지나면 이미 체크가 되어있는 체크박스들은 전부 초기화 필요함. 그럴려면 마지막으로 앱을 실행한 날짜가 DB에 주기적으로 기록이 되어야 함.

        // 따라서, 하루가 지나면 아래 과정을 수행하게 되는 것이다.
        // 1. 첫번째 execSQL문 : medi_check 필드의 값을 언체크 상태(0)으로 업데이트
        // 2. 두번째 execSQL문 : medi_date 필드에 오늘 날짜를 주기적으로 기록해주기.
        sqlitedb.execSQL("UPDATE mediTBL SET medi_check = 0 WHERE medi_date != '${LocalDate.now()}';")
        sqlitedb.execSQL("UPDATE mediTBL SET medi_date = '${LocalDate.now()}' WHERE medi_date != '${LocalDate.now()}';")

        // 사용자 ID가 동일하며, 앱을 실행한 요일이 포함된 체크리스트만 읽어옴
        val cursor = sqlitedb.rawQuery(
            "SELECT * FROM mediTBL WHERE medi_day_of_week LIKE '%$todayDayOfWeek%' AND user_id = '$nowUserID';",
            null
        )

        // mediTBL(테이블)의 각각의 필드의 값을 저장할 가변 리스트 초기화
        medNameLst = mutableListOf()
        medTimeLst = mutableListOf()
        medDayOfWeekLst = mutableListOf()
        medCheckBoxLst = mutableListOf()

        while (cursor.moveToNext()) {
            // 테이블의 각각의 필드의 값을 우선 변수에 저장
            mediNum = cursor.getInt(cursor.getColumnIndexOrThrow("medi_num"))
            medName = cursor.getString(cursor.getColumnIndexOrThrow("medi_name"))
            medTime = cursor.getString(cursor.getColumnIndexOrThrow("medi_time"))
            medDate = cursor.getString(cursor.getColumnIndexOrThrow("medi_date"))
            medCheck = cursor.getInt(cursor.getColumnIndexOrThrow("medi_check"))


            // CheckBox 객체를 동적으로 추가하여 속성 적용
            val checkBox = CheckBox(this).apply {
                // checkBox의 text 값에 들어갈 spannable
                val spannable = SpannableString("$medName\n$medTime") // medName은 복약 이름, medTime은 복약 예정 시간
                spannable.setSpan(StyleSpan(Typeface.BOLD), 0, medName.length, 0) // 복약 이름은 Bold체로 표시
                spannable.setSpan(AbsoluteSizeSpan(80), 0, medName.length, 0) // 복약 이름은 80px로 지정

                // 복약 예정 시간의 text color와 padding 설정
                spannable.setSpan(ForegroundColorSpan(Color.parseColor("#272D4F")), medName.length + 1, spannable.length, 0)
                spannable.setSpan(setPadding(20,0,0,0), medName.length + 1, spannable.length, 0)

                text = spannable // text에 들어갈 내용을 담은 spannable을 text로 넘겨줌
                buttonDrawable = null // checkBox 아이콘은 표시하지 않음

                setPadding(100, 0, 0, 0) // checkBox의 padding 설정

                // mediTBL(테이블)에서 체크 여부를 알아낸 후, 해당 checkBox의 isChecked 설정 변경해줌.
                setOnCheckedChangeListener(null) // 리스너 해제
                if(medCheck == 1) {
                    isChecked = true
                }
                else {
                    isChecked = false
                }
                setBackgroundResource(R.drawable.med_lst_checkbox_selector) // 체크 여부에 따른 backGround 이미지 변경

                tag = mediNum // mediNum(현재 체크박스의 고유 번호) 값을 tag에 저장

                // 체크 상태가 변경된 경우(즉, 사용자가 체크박스를 클릭한 경우) 이벤트 처리
                setOnCheckedChangeListener {_, isChecked ->
                    setBackgroundResource(R.drawable.med_lst_checkbox_selector) // 일단 background 이미지 먼저 바꾸기
                    val mediNumForUpdate = tag as Int  // 지역 변수 선언 및 "체크박스 고유 번호"로 초기화
                    updateMediCheck(mediNumForUpdate, isChecked) // DB의 mediTBL에 check 여부를 업데이트하는 함수 호출 -> 매개변수로 "체크박스 고유 번호", "체크 여부"를 넘겨줌
                    Log.d("mediCheck", "mediNumForUpdate: $mediNumForUpdate, isChecked: $isChecked") // 매개변수 값이 정확하게 넘어갔는지 확인하기 위해 로그 출력
                }
                val params = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT) // checkBox를 담을 레이아웃 높이와 너비 지정
                // 아래쪽 여백 (체크 박스 간의 위아래 간격 조정을 위해 위쪽 여백&아래쪽 여백 설정
                params.topMargin = 20
                params.bottomMargin = 20
                layoutParams = params
            }
            medCheckBoxLst.add(checkBox) // 복약 체크리스트에 해당 체크박스 추가
            medLstLayout.addView(checkBox) // 화면에 해당 체크박스 추가하여 보여줌
        }
        cursor.close()
        sqlitedb.close()
        dbManager.close()
    }

    // DB의 mediTBL에 check 여부를 저장하는 함수
    private fun updateMediCheck(mediNum: Int, isChecked: Boolean) {
        var nowUserID = getCurrentUserId() // 현재 로그인한 사용자 ID 가져오기

        // 값을 업데이트해야 하므로 읽기 전용으로 DB 열기
        dbManager = DBManager(this, "userDB", null, 13)
        val sqlitedb = dbManager.writableDatabase

        // 이미 닫힌 DB를 reopen 하는 에러를 방지하기 위해 try-catch-finally로 작성
        try {
            sqlitedb.execSQL(
                "UPDATE mediTBL SET medi_check = ${if (isChecked) 1 else 0} WHERE medi_num = '$mediNum' AND user_id = '$nowUserID';"
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
        dbManager = DBManager(this, "userDB", null, 13)
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