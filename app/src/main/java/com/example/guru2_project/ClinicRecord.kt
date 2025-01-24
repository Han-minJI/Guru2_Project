package com.example.guru2_project

import android.content.Intent
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


    lateinit var tomain:ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.clinic_record)

        val calendarView: MaterialCalendarView
        calendarView = findViewById(R.id.calendarView)

        val calendarDecorators = CalendarDecorators() // 데코레이터 객체 선언
        var selectedDate: CalendarDay? = null // 날짜 선택 됐는지 확인 변수
        var selectedAgain: Boolean = false// 날짜가 한번 더 클릭 됐는지 확인

        tomain=findViewById(R.id.toMain)

        val fragmentContainer: View = findViewById(R.id.fragmentContainer)


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

            // FrameLayout을 보이게 하고, 프래그먼트 삽입
            //fragmentContainer.visibility=View.VISIBLE
            if (selectedDate != date) {
                // 프래그먼트를 FrameLayout에 동적으로 추가
                val fragment = RecordInfoFragment.newInstance(year, month, day)
                supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment)
                    .commit()
                Log.d("프래그먼트", "새로생성")

                fragmentContainer.visibility = View.VISIBLE
            } else if (selectedDate == date && selectedAgain == false) {
                Toast.makeText(
                    applicationContext, "같은 날짜를 선택하셨습니다.",
                    Toast.LENGTH_SHORT
                ).show()

                fragmentContainer.visibility = View.GONE
                selectedAgain = true

            } else {
                fragmentContainer.visibility = View.VISIBLE
                selectedAgain = false
            }


            selectedDate = date

        }

        tomain.setOnClickListener {
            val intent= Intent(this,MainPage::class.java)
            startActivity(intent)
        }
    }
}