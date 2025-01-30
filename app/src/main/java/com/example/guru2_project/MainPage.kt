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
    lateinit var exerRecordBtn:ImageButton

    // 혈당량 Bar 차트 관련 변수
    lateinit var barChart: BarChart // Bar 차트
    lateinit var chartLayout: LinearLayout // Bar 차트가 담길 LinearLayout
    lateinit var dbManager: DBManager // bloodRecord 테이블에서 혈당량 값을 가져오기 위해 필요함.
    lateinit var sqlitedb: SQLiteDatabase // bloodRecord 테이블에서 혈당량 값을 가져오기 위해 필요함.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        mypagebutton = findViewById(R.id.mypagebutton)
        clinicRecordBtn=findViewById(R.id.clinicRecordBtn)
        medCheckBtn=findViewById(R.id.medCheckBtn)
        habitCheckBtn=findViewById(R.id.habitCheckBtn)
        bloodRecordBtn=findViewById(R.id.bloodRecordBtn)
        exerRecordBtn=findViewById(R.id.exerRecordBtn)

        // 혈당량 Bar 차트 관련 변수 -> 레이아웃 파일의 위젯 id와 연결
        barChart = findViewById(R.id.barChart) // 혈당량 값이 표시될 Bar 차트
        chartLayout = findViewById(R.id.chartLayout) // Bar 차트가 표시될 레이아웃

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

        //나의 운동 기록 버튼 클릭 시 나의 운동 기록 화면으로 넘어가기
        exerRecordBtn.setOnClickListener {
            val intent = Intent(this, ExerLstPage::class.java)
            startActivity(intent)
        }

        // 차트 관련 코드를 하단에 쭉 기술함.
        var entries = ArrayList<BarEntry>() // 차트 x, y 값 쌍을 저장할 BarEntry 선언 및 초기화

        // 차트의 y축 값 선언 및 초기화 (y축 - 혈당량)
        var value1 = 0.0f ; var value2 = 0.0f ; var value3 = 0.0f ; var value4 = 0.0f
        var value5 = 0.0f ; var value6 = 0.0f ; var value7 = 0.0f ; var value8 = 0.0f

        var nowUserID = getCurrentUserId() // 함수 호출하여 현재 로그인한 사용자 ID 가져오기

        // 혈당량 값을 가져오기만 할 것이므로 읽기 전용으로 DB 열기
        dbManager = DBManager(this, "userDB", null, 17)
        sqlitedb = dbManager.readableDatabase

        // 조건 -> 사용자 ID가 동일하며, 오늘 등록한 것
        // 위 조건에 맞게 혈당량 값과 측정한 시간(공복 ~ 취침 전)을 가져옴
        val cursor = sqlitedb.rawQuery(
            "SELECT blood, bloodtime FROM bloodRecord WHERE date = '${LocalDate.now()}' AND userId = '$nowUserID';",
            null
        )

        while (cursor.moveToNext()) {
            val bloodValue = cursor.getString(cursor.getColumnIndexOrThrow("blood")).toFloat() // 혈당량 값이 변수에 저장됨 -> 차트의 y축
            val bloodTime = cursor.getString(cursor.getColumnIndexOrThrow("bloodtime")) // 혈당량 측정 시간이 변수에 저장됨 -> 차트의 x축
            when(bloodTime) { // 혈당량 측정 시간을 보고 혈당량 값을 알맞게 대입
                // <value1 ~ value8> 변수에 "공복 ~ 취침 전" 순서대로 혈당량 값이 각각 저장됨
                "공복" -> value1 = bloodValue // "공복" 시간에 측정한 혈당량 저장
                "아침 식전" -> value2 = bloodValue // "아침 식전" 시간에 측정한 혈당량 저장
                "아침 식후" -> value3 = bloodValue // "아침 식후" 시간에 측정한 혈당량 저장
                "점심 식전" -> value4 = bloodValue // "점심 식전" 시간에 측정한 혈당량 저장
                "점심 식후" -> value5 = bloodValue // "점심 식후" 시간에 측정한 혈당량 저장
                "저녁 식전" -> value6 = bloodValue // "저녁 식전" 시간에 측정한 혈당량 저장
                "저녁 식후" -> value7 = bloodValue // "저녁 식후" 시간에 측정한 혈당량 저장
                "취침 전" -> value8 = bloodValue // "취침 전" 시간에 측정한 혈당량 저장
            }
        }
        cursor.close()
        sqlitedb.close()
        dbManager.close()

        // (x, y) 값이 Pair로 entries(차트 데이터)에 추가됨
        // 1번째 매개변수 -> x : float 형태로 직접 지정
        // 2번째 매개변수 -> y : value1 ~ value8 변수에 저장했던 값 넘겨줌
        entries.add(BarEntry(1.0f,value1))
        entries.add(BarEntry(2.0f,value2))
        entries.add(BarEntry(3.0f,value3))
        entries.add(BarEntry(4.0f,value4))
        entries.add(BarEntry(5.0f,value5))
        entries.add(BarEntry(6.0f,value6))
        entries.add(BarEntry(7.0f,value7))
        entries.add(BarEntry(8.0f,value8))

        barChart.run {
            barChartSet(this@MainPage, barChart) // 차트의 속성을 조정하는 함수 호출
            barChartY(this@MainPage, barChart) // 차트의 Y축 설정하는 함수 호출
            barChartX(this@MainPage, barChart) // 차트의 X축 설정하는 함수 호출
        }

        var set = BarDataSet(entries,"DataSet") // 차트에 표시할 데이터인 entries, 범례는 "DataSet"
        set.color = ContextCompat.getColor(applicationContext!!,R.color.white) // 차트의 Bar 부분 색깔 지정

        val dataSet :ArrayList<IBarDataSet> = ArrayList() // 데이터셋 리스트
        dataSet.add(set) // 데이터셋 리스트에 set 추가
        val data = BarData(dataSet) // 보여질 데이터인 BarData
        data.barWidth = 0.6f // 차트의 bar 너비 지정
        barChart.run {
            this.data = data // 차트의 데이터를 data로 설정
            invalidate() // View를 다시 draw하는 함수 호출
        }
    }

    // 하단에 차트 표시를 위해 정의한 함수를 전부 정리해두었음.

    // 차트의 속성을 조정하는 함수
    fun barChartSet(context: Context, barChart: BarChart) {
        barChart.description.isEnabled = false // 라벨의 Description 표시 X
        barChart.setMaxVisibleValueCount(8) // 그래프 최대 개수를 8로 지정
        barChart.setPinchZoom(true) // 줌인 가능하도록 설정
        barChart.setDrawBarShadow(false) // 차트에 그림자 속성 주지 않음
        barChart.setDrawGridBackground(false) // 차트 배경에 그리드 라인 넣지 않음
        barChart.axisRight.isEnabled = false // 왼쪽 y축만 보이고, 오른쪽 y축은 보이지 않게
        barChart.setTouchEnabled(false) // 차트 터치해도 아무 변화 X
        barChart.animateY(1500) // 1.5초 동안 바 차트가 올라가는 애니메이션 효과 추가
        barChart.legend.isEnabled = false // 차트 범례 표시 X
    }

    // 차트의 Y축 설정하는 함수
    fun barChartY(context: Context, barChart: BarChart) {
        barChart.axisLeft.run {
            axisMaximum = 200f // y 최댓값을 200.0으로 설정
            axisMinimum = 0f // y 최솟값을 0.0으로 설정
            granularity = 50f // y축 간격 설정(50.0)
            setDrawLabels(true) // y축 옆에 라벨 표시
            setDrawGridLines(false) // 수평 방향의 라인 표시 X
            setDrawAxisLine(true) // y축 선 표시
            axisLineColor = ContextCompat.getColor(context,R.color.white) // y축 선의 컬러 지정
            textColor = ContextCompat.getColor(context,R.color.white) // y축 글자 컬러 지정
            textSize = 13f // y축 글자 크기 조정(단위 : pixels)
        }
    }

    // 차트의 X축 설정하는 함수
    fun barChartX(context: Context, barChart: BarChart) {
        barChart.xAxis.run {
            position = XAxis.XAxisPosition.BOTTOM // x축의 위치를 차트 하단부로 설정
            granularity = 1f // x축 간격 설정(1.0)
            setDrawGridLines(false) // 수직 방향의 라인 표시 X
            setDrawAxisLine(true) // x축 선 표시
            textColor = ContextCompat.getColor(context,R.color.white) // x축 글자 컬러 지정
            textSize = 12f // x축 글자 크기 지엉
            valueFormatter = MyXAxisFormatter() // x축의 라벨 바꿔주기 위해 필요
            labelRotationAngle = 45f // x축 항목 글자 부분 45도 회전
        }
    }

    // X축 라벨(X축의 글자) 설정 함수
    inner class MyXAxisFormatter : ValueFormatter() {
        private val times = arrayOf("공복","아침 식전","아침 식후","점심 식전","점심 식후","저녁 식전","저녁 식후","취침 전") // x축 밑에 표시될 글자
        override fun getAxisLabel(value: Float, axis: AxisBase?): String { // value(축의 위치)를 입력받아 그 값에 해당하는 라벨의 이름 반환
            return times.getOrNull(value.toInt()-1) ?: value.toString() // 축의 범위(여기서는 0~7)을 벗어날 경우 축의 인덱스를 String 타입으로 반환하여 리턴
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

