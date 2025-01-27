package com.example.guru2_project

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.media.Image
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import java.time.LocalDate

class MainPage : AppCompatActivity() {
    lateinit var mypagebutton: ImageButton
    lateinit var clinicRecordBtn:ImageButton//내원 기록하기 버튼
    lateinit var medCheckBtn:ImageButton //복약 체크하기 버튼
    lateinit var habitCheckBtn:ImageButton //생활 체크하기 버튼
    lateinit var bloodRecordBtn:ImageButton // 혈당 기록하기 버튼

    // 혈당량 Bar 차트 관련 변수
    lateinit var barChart: BarChart
    lateinit var chartLayout: LinearLayout
    lateinit var dbManager: DBManager
    lateinit var sqlitedb: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        mypagebutton = findViewById(R.id.mypagebutton)
        clinicRecordBtn=findViewById(R.id.clinicRecordBtn)
        medCheckBtn=findViewById(R.id.medCheckBtn)
        habitCheckBtn=findViewById(R.id.habitCheckBtn)
        bloodRecordBtn=findViewById(R.id.bloodRecordBtn)

        // 혈당량 Bar 차트 관련 변수
        barChart = findViewById(R.id.barChart)
        chartLayout = findViewById(R.id.chartLayout)

        //이미지 버튼 클릭시 마이페이지로 넘어가도록
        mypagebutton.setOnClickListener {
            val intent = Intent(this, MyPage::class.java)
            startActivity(intent)
        }

        //내원 기록하기 버튼 클릭시 캘린터 뷰 화면으로 넘어가기
        clinicRecordBtn.setOnClickListener {
            val intent=Intent(this,ClinicRecord::class.java)
            startActivity(intent)
        }

        //복약 체크하기 버튼 클릭시 복약 체크하기 화면으로 넘어가기
        medCheckBtn.setOnClickListener {
            val intent=Intent(this,MedLstPage::class.java)
            startActivity(intent)
        }

        //생활 체크하기 버튼 클릭시 생활 체크하기 화면으로 넘어가기
        habitCheckBtn.setOnClickListener {
            val intent=Intent(this,HabLstPage::class.java)
            startActivity(intent)
        }

        bloodRecordBtn.setOnClickListener {
            val intent=Intent(this,BloodRecord::class.java)
            startActivity(intent)
        }

        // 차트 관련 코드 하단에 쭉 기술함. -> 주석은 코드 분석 후 추가할 예정.
        var entries = ArrayList<BarEntry>() // 차트 x, y 값 쌍을 저장할 BarEntry 선언 및 초기화

        // 차트의 y축 값 선언 및 초기화
        var value1 = 0.0f ; var value2 = 0.0f ; var value3 = 0.0f ; var value4 = 0.0f
        var value5 = 0.0f ; var value6 = 0.0f ; var value7 = 0.0f ; var value8 = 0.0f

        var nowUserID = getCurrentUserId() // 함수 호출하여 현재 로그인한 사용자 ID 가져오기

        // !!! DB 연결 후 주석 해제 !!!
        // 값을 가져오기만 할 것이므로 읽기 전용으로 DB 열기
        dbManager = DBManager(this, "userDB", null, 13)
        sqlitedb = dbManager.readableDatabase

        // 사용자 ID가 동일하며, 앱을 실행한 요일이 포함된 체크리스트만 읽어옴
        val cursor = sqlitedb.rawQuery(
            "SELECT blood, bloodtime FROM bloodRecord WHERE date = '${LocalDate.now()}' AND userId = '$nowUserID';",
            null
        )

        while (cursor.moveToNext()) {
            val bloodValue = cursor.getString(cursor.getColumnIndexOrThrow("blood")).toFloat()
            val bloodTime = cursor.getString(cursor.getColumnIndexOrThrow("bloodtime"))
            when(bloodTime) {
                "공복" -> value1 = bloodValue
                "아침 식전" -> value2 = bloodValue
                "아침 식후" -> value3 = bloodValue
                "점심 식전" -> value4 = bloodValue
                "점심 식후" -> value5 = bloodValue
                "저녁 식전" -> value6 = bloodValue
                "저녁 식후" -> value7 = bloodValue
                "취침 전" -> value8 = bloodValue
            }
        }
        cursor.close()
        sqlitedb.close()
        dbManager.close()
        // !!! DB 연결 후 주석 해제 !!!

        // (x, y) 값 entries에 추가
        entries.add(BarEntry(1.0f,value1))
        entries.add(BarEntry(2.0f,value2))
        entries.add(BarEntry(3.0f,value3))
        entries.add(BarEntry(4.0f,value4))
        entries.add(BarEntry(5.0f,value5))
        entries.add(BarEntry(6.0f,value6))
        entries.add(BarEntry(7.0f,value7))
        entries.add(BarEntry(8.0f,value8))

        barChart.run {
            barChartSet(this@MainPage, barChart)
            barChartY(this@MainPage, barChart)
            barChartX(this@MainPage, barChart)
        }

        var set = BarDataSet(entries,"DataSet")
        set.color = ContextCompat.getColor(applicationContext!!,R.color.white)

        val dataSet :ArrayList<IBarDataSet> = ArrayList()
        dataSet.add(set)
        val data = BarData(dataSet)
        data.barWidth = 0.3f
        barChart.run {
            this.data = data
            setFitBars(true)
            invalidate()
        }
    }
    // 하단은 차트 표시에 필요한 함수 정의한 것을 전부 정리해 둔 것 -> 코드 분석 후 주석 추가 예정

    // 차트의 속성을 조정하는 함수
    fun barChartSet(context: Context, barChart: BarChart) {
        barChart.description.isEnabled = false
        barChart.setMaxVisibleValueCount(8)
        barChart.setPinchZoom(false)
        barChart.setDrawBarShadow(false)
        barChart.setDrawGridBackground(false)
        barChart.axisRight.isEnabled = false
        barChart.setTouchEnabled(false)
        barChart.animateY(2000)
        barChart.legend.isEnabled = false
    }

    // 차트의 Y축 설정하는 함수
    fun barChartY(context: Context, barChart: BarChart) {
        barChart.axisLeft.run {
            axisMaximum = 201f
            axisMinimum = 0f
            granularity = 50f
            setDrawLabels(true)
            setDrawGridLines(true)
            setDrawAxisLine(false)
            axisLineColor = ContextCompat.getColor(context,R.color.white)
            gridColor = ContextCompat.getColor(context,R.color.white)
            textColor = ContextCompat.getColor(context,R.color.white)
            textSize = 13f
        }
    }

    // 차트의 X축 설정하는 함수
    fun barChartX(context: Context, barChart: BarChart) {
        barChart.xAxis.run {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawAxisLine(true)
            setDrawGridLines(false)

            textColor = ContextCompat.getColor(context,R.color.white)
            textSize = 12f
            valueFormatter = MyXAxisFormatter()
            labelRotationAngle = 90f
        }
    }

    // X축 라벨(X축의 글자) 설정 함수
    inner class MyXAxisFormatter : ValueFormatter() {
        private val days = arrayOf("공복","아침 식전","아침 식후","점심 식전","점심 식후","저녁 식전","저녁 식후","취침 전")
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return days.getOrNull(value.toInt()-1) ?: value.toString()
        }
    }

    // !!! DB 연결 후 주석 해제 !!!
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

