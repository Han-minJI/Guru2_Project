package com.example.guru2_project

import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.util.Calendar

class MedLstPage : AppCompatActivity() {

    // "복용하시는 약을 등록해주세요" -> MedRegPage(복약 등록) 페이지로 이동 위한 버튼
    lateinit var btnToMedReg : ImageButton

    lateinit var medLstLayout : LinearLayout // 복약 리스트(CheckBox)가 나열될 LinearLayout

    // !!! DB 클래스 만들고 여기 안에 주석 해제 !!!

//    lateinit var dbManager : MedDBManager    // DBManger 객체
//    lateinit var sqlitedb : SQLiteDatabase   // SQLiteDatabase 객체

    // !!! DB 클래스 만들고 여기 안에 주석 해제 !!!

    // medNameLst, medTimeLst -> 복약 리스트에 나열되는 CheckBox의 내용을 "복용할 약, 복용 예정 시간"으로 세팅해주기 위해 필요
    lateinit var medNameLst : MutableList<String>       // mediTBL(복약 테이블)의 medi_name 필드의 값만 저장한 가변 리스트
    lateinit var medTimeLst : MutableList<String>       // mediTBL(복약 테이블)의 medi_time 필드의 값만 저장한 가변 리스트
    lateinit var medDayOfWeekLst : MutableList<String>  // mediTBL(복약 테이블)의 medi_day_of_week 필드의 값만 저장한 가변 리스트
    lateinit var medCheckBoxLst : MutableList<CheckBox> // 화면에 표시될 복약 CheckBox를 담을 가변 리스트

    var mediNum: Int = 0
    lateinit var medName: String        // medNameLst에 원소를 추가하기 위해 필요한 변수
    lateinit var medTime: String        // medTimeLst에 원소를 추가하기 위해 필요한 변수
    lateinit var medDayOfWeek: String   // medDayOfWeekLst에 원소를 추가하기 위해 필요한 변수
    lateinit var medDate: String
    var medCheck: Int? = null

    lateinit var todayDayOfWeek: String // 실행한 시간의 요일 -> 복약 리스트에 오늘 요일에 해당하는 CheckBox만 표시해주기 위해 필요한 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_med_lst_page)


        // Layout의 위젯 ID와 앞서 선언한 변수 연결
        btnToMedReg = findViewById(R.id.btnToMedReg)    // MedRegPage (복용약 등록 화면) 으로 이동하는 버튼 -> 복용하시는 약을 등록해주세요
        medLstLayout = findViewById(R.id.medLstLayout)  // 복용 체크리스트가 표시될 레이아웃

//        val today: String? = getDayOfWeek()

        // 실행일 기준 무슨 요일인지 가져오는 함수 호출 -> todayDayOfWeek 변수에 저장
        todayDayOfWeek = getDayOfWeek().toString()


//        // !!! DB 클래스 만들고 여기 안에 주석 해제 !!!
//        // DB 사용을 위해 DBManger 객체 생성 (mediTBL - 복약 테이블)
//        dbManager = MedDBManager(this, "mediTBL", null, 1)
//        sqlitedb = dbManager.writableDatabase
//
//        var cursor: Cursor
//
//        // medi_day_of_week(약 먹을 요일) 필드의 값 == "오늘 요일(실행일 기준)"인 레코드만 필터링
//        cursor = sqlitedb.rawQuery("SELECT * FROM mediTBL where medi_day_of_week like '%" + todayDayOfWeek + "%';", null)
//
//        //// 위의 코드에서 rawQuery를 이해하기 위해 하단에 예시 서술함.
//        //// medi_day_of_week 필드에 "mon twu wed thur fri sat" 이라는 String 값이 저장되어 있으며,
//        //// todayDayOfWeek = "mon", 즉 앱을 실행한 날짜의 요일이 월요일이면,
//        //// "mon twu wed thur fri sat" 가 저장된 레코드도 조건에 부합함 -> 얘도 읽어오면 됨.
//
//
//        // 위에 선언한 가변 리스트들 초기화
//        medNameLst = mutableListOf()
//        medTimeLst = mutableListOf()
//        medDayOfWeekLst = mutableListOf()
//        medCheckBoxLst = mutableListOf()
//
//
//        while (cursor.moveToNext()) {
//            // 필터링한 결과 읽은 레코드 중 특정 필드의 값 -> 변수에 저장
//            mediNum = cursor.getInt(cursor.getColumnIndexOrThrow("medi_num")) // medi_name 필드 값 -> medName
//            medName = cursor.getString(cursor.getColumnIndexOrThrow("medi_name")) // medi_name 필드 값 -> medName
//            medTime = cursor.getString(cursor.getColumnIndexOrThrow("medi_time")) // medi_time 필드 값 -> medTime
//            medDayOfWeek = cursor.getString(cursor.getColumnIndexOrThrow("medi_day_of_week")) // medi_day_of_week 필드 값 -> medDayOfWeek
//            medDate = cursor.getString(cursor.getColumnIndexOrThrow("medi_date")) // medi_date 필드 값 -> medDayOfWeek
//            medCheck = cursor.getInt(cursor.getColumnIndexOrThrow("medi_check")) // medi_check 필드 값 -> medDayOfWeek
//
//            medNameLst.add(medName)             // medName에 저장된 값 -> <복용약 이름 리스트>에 추가
//            medTimeLst.add(medTime)             // medTime 저장된 값 -> <복약 예정 시간 리스트>에 추가
//            medDayOfWeekLst.add(medDayOfWeek)   // medDayOfWeek 저장된 값 -> <복약 예정 요일 리스트>에 추가
//
//
//            val checkBox = CheckBox(this).apply {    // CheckBox 객체 생성
//                text = "$medName\n$medTime" // 해당 CheckBox 객체의 text -> "복용약 이름 (줄바꿈) 복약 예정 시간" 으로 세팅
//                if (medCheck == 1 && medDate == LocalDate.now().toString()) {
//                    isChecked = true
//                }
//            }
//            checkBox.apply {
//                if (medCheck == 0 || medDate != LocalDate.now().toString()) {
//                    dbManager = MedDBManager(context, "mediTBL", null, 1)
//                    sqlitedb = dbManager.writableDatabase
//
//                    sqlitedb.execSQL("UPDATE mediTBL SET medi_date = '" + LocalDate.now().toString() + "', medi_check = 0 WHERE medi_num = '" + mediNum +"';")
//                    //                    saveData(LocalDate.now().toString(), false)
//
//                    sqlitedb.close()
//                    dbManager.close()
//                }
//            }
//            medCheckBoxLst.add(checkBox)    // 복약 CheckBox 리스트에 해당 CheckBox 객체 추가
//            medLstLayout.addView(checkBox)  // 복약 CheckBox가 쭉 표시될 레이아웃에 해당 CheckBox 객체 addView(동적으로 추가)
//            // 사용자가 체크 버튼을 누르면 -> 마지막으로 체크한 날짜 + 체크 여부 저장
//            checkBox.setOnClickListener {
//                dbManager = MedDBManager(this, "mediTBL", null, 1)
//                sqlitedb = dbManager.writableDatabase
//
//                if (checkBox.isChecked) {
//                    sqlitedb.execSQL("UPDATE mediTBL SET medi_date = '" + LocalDate.now().toString() + "', medi_check = 1 WHERE medi_num = '" + mediNum +"';")
//                }
//                else {
//                    sqlitedb.execSQL("UPDATE mediTBL SET medi_date = '" + LocalDate.now().toString() + "', medi_check = 0 WHERE medi_num = '" + mediNum +"';")
//
//                }
//                sqlitedb.close()
//                dbManager.close()
//            }
//        }
//        cursor.close()
//        sqlitedb.close()
//        dbManager.close()
//
//        // !!! DB 클래스 만들고 여기 안에 주석 해제 !!!

        // 오늘 요일에 해당하지 않는 체크리스트는 목록에서 삭제
        // removeView를 하면 인덱스가 바뀌므로 IndexOutOfBoundsException 방지를 위해 역순으로 체크
        for (i in medCheckBoxLst.indices.reversed()) { // 현재 화면 상에 나타난 CheckBox의 개수만큼 반복
            val checkBox = medCheckBoxLst[i]
            val medDayOfWeek = medDayOfWeekLst[i]

            if (medDayOfWeek.contains(todayDayOfWeek) == false) { // 현재 화면 상에 나타난 CheckBox가 오늘 요일(앱 실행일 기준)에 해당하지 않는다면
                medLstLayout.removeView(checkBox)                 // 화면에서 해당 CheckBox 삭제

                // 앞서 원소를 추가해주었던 가변 리스트에서도 삭제해줌.
                medCheckBoxLst.removeAt(i)
                medNameLst.removeAt(i)
                medTimeLst.removeAt(i)
                medDayOfWeekLst.removeAt(i)
            }
        }

        btnToMedReg.setOnClickListener{ // "복용하시는 약을 등록해주세요" 버튼 클릭 시
            // MedRegPage(복약 등록 화면)으로 Intent 전달하며 화면 전환
            var intent = Intent(this, MedRegPage::class.java)
            startActivity(intent)
        }


    }
    // 오늘 무슨 요일인지 가져오는 함수
    fun getDayOfWeek(): String? {
        val cal: Calendar = Calendar.getInstance()      // Calendar 인스턴스화
        var todayDayOfWeek: String? = null              // 오늘 요일 (앱 실행일 기준)을 저장할 변수 초기화
        val nWeek: Int = cal.get(Calendar.DAY_OF_WEEK)  // 요일 값 (Int 타입) 가져오기

        if (nWeek == 1) {      // 요일 값 = 1 -> 일요일
            todayDayOfWeek = "sun"
        }
        else if (nWeek == 2) { // 요일 값 = 2 -> 월요일
            todayDayOfWeek = "mon"
        }
        else if (nWeek == 3) { // 요일 값 = 3 -> 화요일
            todayDayOfWeek = "tue"
        }
        else if (nWeek == 4) { // 요일 값 = 4 -> 수요일
            todayDayOfWeek = "wed"
        }
        else if (nWeek == 5) { // 요일 값 = 5 -> 목요일
            todayDayOfWeek = "thur"
        }
        else if (nWeek == 6) { // 요일 값 = 6 -> 금요일
            todayDayOfWeek = "fri"
        }
        else if (nWeek == 7) { // 요일 값 = 7 -> 토요일
            todayDayOfWeek = "sat"
        }
        return todayDayOfWeek // 오늘 무슨 요일인지 반환

    }
}