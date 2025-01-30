package com.example.guru2_project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.util.Timer
import kotlin.concurrent.timer

class ExerTimerPage : AppCompatActivity() {
    // 타이머 사용을 위한 변수 선언 및 초기화
    private var time = 0
    private var timerTask : Timer? = null

    // 그 외 변수들
    lateinit var txtExerDate : TextView // 오늘 날짜 표시할 TextView
    lateinit var txtTimerName : TextView // 운동 이름 표시할 TextView
    lateinit var txtTimerValue : TextView // 운동 시간 표시할 TextView
    lateinit var exerProgress: ProgressBar // 운동 타이머 UI ProgressBar
    lateinit var btnBckToLst : ImageButton // 이전 화면 버튼
    lateinit var btnOKLayout : LinearLayout // 타이머 끝나면 동적으로 추가될 확인 버튼

    var exerTimer : Int = 0 // 이전 화면에서 전달받은 Intent의 "운동 시간"
    lateinit var exerName : String // 이전 화면에서 전달받은 Intent의 "운동 이름"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exer_timer_page)

        // 앞서 선언한 변수들과 layout 파일의 위젯 연결
        txtExerDate = findViewById(R.id.txtExerDate) // 오늘 날짜
        txtTimerName = findViewById(R.id.txtTimerName) // 운동 이름
        txtTimerValue = findViewById(R.id.txtTimerValue) // 운동 시간
        btnBckToLst = findViewById(R.id.btnBckToLst) // 이전 버튼
        btnOKLayout = findViewById(R.id.btnOKLayout) // 확인 버튼 담을 layout
        exerProgress = findViewById(R.id.exerProgress) // 타이머 UI ProgressBar

        // 이전 화면에서 intent 전달받음
        exerTimer = intent.getIntExtra("EXER_TIMER", 0) // 운동 시간(기본값 0) 가져옴
        exerName = intent.getStringExtra("EXER_NAME").toString() // 운동 이름 가져옴

        txtExerDate.text = LocalDate.now().toString() // 오늘 날짜 알아내서 표시

        txtTimerName.text = exerName // TextView 내용을 Intent로 전달받은 exerName(운동 이름)으로 설정
        time = exerTimer * 60 // 1분 -> 60초로 변환

        if (time > 0) { // time이 0이 되기 전이라면
            startTimer() // Timer 시작하는 함수 호출
        }

        // 이전 화면으로 가는 버튼 클릭 시, 운동 체크리스트 화면으로 Intent 전달하며 화면 전환
        btnBckToLst.setOnClickListener {
            val intent = Intent(this, ExerLstPage::class.java)
            startActivity(intent)
        }
    }

    // Timer 시작하는 함수 (1초마다 time 값을 1(초)씩 감소시킴)
    private fun startTimer() {
        timerTask = timer(period = 1000) { // 1초(1000밀리초)마다 실행
            time-- // time 값이 1(초)씩 감소
            runOnUiThread { // UI 업데이트
                val min = time / 60 // 현재 몇 분인지 계산
                val sec = time % 60 // 현재 몇 초인지 계산
                txtTimerValue.text = String.format("%02d:%02d", min, sec) // 00:00 -> 즉, "분:초" 로 설정

                // 나눗셈을 해야 하므로 Float 타입으로 형변환
                // (현재 시간 / 운동 목표 시간)으로 나누기 -> 둘 다 초(sec) 단위임
                // 나눗셈 후, 백분율(%)로 변환
                val progress = (time.toFloat() / (exerTimer * 60).toFloat()) * 100 // progress(타이머 진행률) 계산
                exerProgress.setProgress(progress.toInt(), true) // ProgressBar의 진행도를 계산한 값으로 세팅

                exerProgress.invalidate() // View 새로고침하는 함수 (ProgressBar UI가 바뀌지 않는 버그를 해결하기 위해 추가)

                Log.d("Timer", "Progress: $progress") // 현재 진행도 값을 로그로 출력하여 확인
                Log.d("Timer", "Max Value: ${exerProgress.max}") // 진행도 최댓값을 로그로 출력하여 확인

                if (time == 0) { // time 값이 0이 되면
                    timerTask?.cancel() // 타이머 종료
                    exerProgress.progress = 0 // ProgressBar의 진행도도 0으로 세팅

                    // "확인" 버튼 레이아웃 xml 리소스를 View 객체로 만들기 (화면에 표시해주기 위해)
                    val view = LayoutInflater.from(this@ExerTimerPage).inflate(R.layout.exer_timer_ok_btn, btnOKLayout, false)
                    btnOKLayout.addView(view) // 원하는 레이아웃에 해당 View 객체 추가하여 화면에 표시

                    val btnTimerOK = view.findViewById<ImageButton>(R.id.btnTimerOK) // 확인 버튼

                    // 확인 버튼 클릭 시, 운동 체크리스트 화면으로 Intent 전달하며 화면 전환
                    btnTimerOK.setOnClickListener {
                        val intent = Intent(this@ExerTimerPage, ExerLstPage::class.java)
                        startActivity(intent)
                    }
                } // if
            } // runOnUiThread
        } // timerTask = timer(period = 1000) { ... }
    } // startTimer()
}