package com.example.guru2_project

import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter
import kotlin.math.log

class ClinicRecord : AppCompatActivity(){

    private lateinit var dbManager: DBManager
    private lateinit var sqlitedb: SQLiteDatabase

    lateinit var tomain:ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.clinic_record)

        val calendarView: MaterialCalendarView
        calendarView = findViewById(R.id.calendarView)

        val calendarDecorators = CalendarDecorators() // 데코레이터 객체 선언
//        var selectedDate: CalendarDay? = null // 날짜 선택 됐는지 확인 변수
//        var selectedAgain: Boolean = false// 날짜가 한번 더 클릭 됐는지 확인

        tomain=findViewById(R.id.toMain)

        val fragmentContainer: View = findViewById(R.id.fragmentContainer)
        val fragmentContainer2:View=findViewById(R.id.fragmentContainer2)// 두번째 프래그먼트

        dbManager = DBManager(this, "userDB", null, 15)


        //캘린더뷰에 데코레이터 추가
        calendarView.addDecorators(
            calendarDecorators.dayDecorator(this),
            calendarDecorators.todayDecorator(this),
            calendarDecorators.sundayDecorator(this),
            calendarDecorators.saturdayDecorator(this),
            calendarDecorators.selectedMonthDecorator(this, CalendarDay.today().month)

        )

        calendarView.setDateSelected(CalendarDay.today(), true)

        // 월 변경 될 때
        calendarView.setOnMonthChangedListener { widget, date ->
            // 캘린더 위젯에서 현재 선택된 날짜 모두 선택 해제
            widget.clearSelection()
            // 캘린더 위젯에 적용된 모든 데코레이터 제거
            widget.removeDecorators()
            //데코레이터 제거하고 다시 위젯 그려짐
            widget.invalidateDecorators()
            //새로운 월에 해당하는 데코레이터 생성 후 다시 할당
            calendarView.addDecorators(
                calendarDecorators.dayDecorator(this),
                calendarDecorators.todayDecorator(this),
                calendarDecorators.sundayDecorator(this),
                calendarDecorators.saturdayDecorator(this),
                calendarDecorators.selectedMonthDecorator(this, date.month)

            )

        }

        // 요일 텍스트 포메터 설정
        calendarView.setWeekDayFormatter(ArrayWeekDayFormatter(resources.getTextArray(R.array.custom_weekdays)))
        // 헤더 텍스트 모양 설정
        calendarView.setHeaderTextAppearance(R.style.CalendarWidgetHeader)

        calendarView.setOnDateChangedListener { widget, date, selected ->
            val year = date.year
            val month = date.month
            val day = date.day
            // DB 읽기 전용으로 불러오기
            sqlitedb = dbManager.readableDatabase
            var nowUserID = "" // 현재 사용자 ID를 저장할 변수


            // session 테이블에서 현재 로그인한 사용자의 ID 가져오기
            val idCursor = sqlitedb.rawQuery("SELECT * FROM session;", null)

            while (idCursor.moveToNext()) {
                nowUserID = idCursor.getString(idCursor.getColumnIndexOrThrow("userId"))
            }
            idCursor.close()


            // FrameLayout을 보이게 하고, 프래그먼트 삽입
            //캘린더 뷰 일정 불러오기
            val dateInsert = String.format("%04d-%02d-%02d", year, month, day) // db 입력 날짜
            val cursor: Cursor

            cursor=sqlitedb.rawQuery("SELECT reason FROM clinicRecord " +
                    "WHERE userId='$nowUserID' and date = '$dateInsert';",null)

            if(cursor.moveToNext())
            {
                val selectReason=cursor.getString(cursor.getColumnIndexOrThrow("reason")).toString()
                val secondFragment=RecordInfoFragment2.newInstance(selectReason)
                supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer2,secondFragment).commit()
                fragmentContainer2.visibility=View.VISIBLE
                fragmentContainer.visibility=View.GONE


                Log.d("정보 불러오기","성공")
            }

            else{

                // 프래그먼트를 FrameLayout에 동적으로 추가
                val fragment=RecordInfoFragment.newInstance(year, month, day)
                supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer,fragment).commit()
                Log.d("프래그먼트","새로생성")

                fragmentContainer.visibility=View.VISIBLE
                fragmentContainer2.visibility=View.GONE
            }

            cursor.close()
            sqlitedb.close()

        }

        tomain.setOnClickListener {
            val intent= Intent(this,MainPage::class.java)
            startActivity(intent)
        }
    }
}