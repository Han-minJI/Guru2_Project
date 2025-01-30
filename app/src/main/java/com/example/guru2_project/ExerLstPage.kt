package com.example.guru2_project

import android.content.DialogInterface
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate

class ExerLstPage : AppCompatActivity() {
    lateinit var exerLstLayout : LinearLayout // 운동 계획 체크리스트가 담길 Layout
    lateinit var btnToExerReg : ImageButton // 운동 계획을 등록할 수 있는 + 버튼
    lateinit var btnBckToMain : ImageButton // 이전 화면으로 갈 수 있는 버튼
    lateinit var exerProgress : ProgressBar // 오늘 운동 진행도

    lateinit var dbManager : DBManager
    lateinit var sqlitedb : SQLiteDatabase

    // exerTBL(운동 기록 테이블)의 값을 저장할 변수들
    var exerNum : Int = 0 // 운동 체크리스트를 구분할 고유 번호
    lateinit var exerName : String // 어떤 운동을 할 것인지
    lateinit var exerTime : String // 몇 분 운동할 건지
    lateinit var txtExerDate : TextView // 오늘 날짜 표시
    var exerCheck : Int = 0 // 운동 checkBox의 체크 여부

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exer_lst_page)

        // 앞서 선언한 변수를 레이아웃 파일의 위젯과 연결
        exerLstLayout = findViewById(R.id.exerLstLayout) // 운동 계획 체크리스트 담을 container
        btnToExerReg = findViewById(R.id.btnToExerReg) // + 버튼 (등록하기 버튼)
        btnBckToMain = findViewById(R.id.btnBckToLst) // 이전 화면으로 가는 버튼

        txtExerDate = findViewById(R.id.txtExerDate) // 오늘 날짜 표시하는 TextView
        exerProgress = findViewById(R.id.exerProgress) // 오늘 운동 진행도 표시하는 Progressbar

        progressSet() // 우선 Progressbar 먼저 세팅해주는 함수 호출
        exerProgress.isIndeterminate = false // Progressbar가 무한 루프를 돌지 않도록 설정

        refreshExerList() // 나의 운동 체크리스트 목록 표시해주는 함수 호출

        btnToExerReg.setOnClickListener { // + 버튼 클릭 시, 운동 계획 등록하는 화면으로 이동
            val intent = Intent(this, ExerRegPage::class.java)
            startActivity(intent)
        }
        btnBckToMain.setOnClickListener { // < 버튼(이전 버튼) 클릭 시, 이전 화면인 MainPage로 이동
            val intent = Intent(this, MainPage::class.java)
            startActivity(intent)
        }
    }

    // Progressbar 진행도 세팅하는 함수
    fun progressSet() {
        // 값을 가져오기만 할 것이므로 읽기 전용으로 DB 열기
        dbManager = DBManager(this, "userDB", null, 18)
        val sqlitedb = dbManager.readableDatabase

        var totalCount = 0 // 사용자가 오늘 해야할 운동 목록이 총 몇 개인지 저장할 변수
        var checkCount = 0 // 사용자가 오늘 운동 목록 중 몇 개를 마쳤는지 저장할 변수

        var nowUserID = getCurrentUserId() // 함수 호출하여 현재 로그인한 사용자 ID 가져오기

        // count(*) -> 레코드 개수 총합 -> totalCount
        // sum(case when exer_check = 1 then 1 else 0 end) -> "exer_check = 1"(운동 체크리스트에서 사용자가 체크를 한 것)인 레코드 개수 총합 -> checkCount
        val cursor = sqlitedb.rawQuery("select count(*) as totalCount, sum(case when exer_check = 1 then 1 else 0 end) as checkCount from exerTBL WHERE user_id = '$nowUserID';", null) // session 테이블 값 읽어오기
        if (cursor.moveToFirst()) {
            totalCount = cursor.getInt(cursor.getColumnIndexOrThrow("totalCount")) // 새로 생성된 totalCount 열의 값 가져오기
            checkCount = cursor.getInt(cursor.getColumnIndexOrThrow("checkCount")) // 새로 생성된 checkCount 열의 값 가져오기
            Log.d("ExerCheck", "totalCount: $totalCount, checkCount: $checkCount") // totalCount, checkCount 값을 log로 확인
        }
        cursor.close()
        sqlitedb.close()

        // ProgressBar UI 업데이트
        runOnUiThread {
            // 나눗셈을 위해 checkCount, totalCount를 Float 타입으로 형변환
            // checkCount를 totalCount로 나눈 뒤, 백분율(%) 단위로 변환하여 progress 변수에 저장
            val progress = (checkCount.toFloat() / totalCount.toFloat()) * 100
            exerProgress.setProgress(progress.toInt(), true) // 계산된 progress 값으로 ProgressBar의 진행률 설정, animation 속성도 추가
        }
    }

    // 나의 생활 체크리스트 하나씩 추가하는 함수
    fun refreshExerList() {
        exerLstLayout.removeAllViews() // 우선, 해당 레이아웃에서 모든 뷰(여러 개의 운동 체크박스) 삭제 후 진행

        var nowUserID = getCurrentUserId() // 함수 호출하여 현재 로그인한 사용자 ID 가져오기

        // 값을 Update해야 하므로 쓰기 전용으로 DB 열기
        dbManager = DBManager(this, "userDB", null, 18)
        sqlitedb = dbManager.writableDatabase

        txtExerDate.text = LocalDate.now().toString() // 오늘 날짜 표시

        // 사용자 ID가 동일하며, 오늘 등록한 운동 체크리스트만 가져옴
        val cursor = sqlitedb.rawQuery(
            "SELECT * FROM exerTBL WHERE exer_date = '${LocalDate.now()}' AND user_id = '$nowUserID';",
            null
        )

        while (cursor.moveToNext()) {
            // 테이블의 각각의 필드의 값을 우선 변수에 저장
            exerNum = cursor.getInt(cursor.getColumnIndexOrThrow("exer_num")) // 고유번호
            exerName = cursor.getString(cursor.getColumnIndexOrThrow("exer_name")) // 어떤 운동인지 (러닝, 유산소, 수영...)
            exerTime = cursor.getString(cursor.getColumnIndexOrThrow("exer_time")) // 운동 시간 (예 : 30분)
            exerCheck = cursor.getInt(cursor.getColumnIndexOrThrow("exer_check")) // 운동 완료 여부(체크 여부)

            // 운동 체크리스트 레이아웃 xml 리소르를 View 객체로 만들기 (화면에 표시해주기 위해)
            val view = LayoutInflater.from(this).inflate(R.layout.exer_checkbox_form, exerLstLayout, false)

            // 해당 레이아웃 파일의 위젯과 변수 연결
            val btnExerName = view.findViewById<TextView>(R.id.btnExerName)
            val txtExerTime = view.findViewById<TextView>(R.id.txtExerTime)
            val btnExerOpt = view.findViewById<ImageButton>(R.id.btnExerOpt)
            val chkExer = view.findViewById<CheckBox>(R.id.chkExer)

            btnExerName.text = exerName // TextView의 내용을 exerName(어떤 운동인지)로 변경
            txtExerTime.text = "${exerTime}분" // TextView의 내용을 exerTime분(운동 시간)으로 변경

            val tag1 = exerNum // 현재 View 객체의 exerNum(고유 번호) tag1로 지정
            val tag2 = exerTime.toInt() // 현재 View 객체의 exerTime(운동 시간)을 tag2로 지정
            val tag3 = btnExerName.text.toString() // 현재 View 객체의 btnExerName(어떤 운동인지)를 tag3로 지정

            chkExer.setOnCheckedChangeListener(null) // 운동 체크박스 체크 변화 리스너 해제
            if(exerCheck == 1) { // 테이블에 저장된 값이 1이면
                chkExer.isChecked = true // checkBox를 체크 상태로 변경
            }
            else { // 테이블에 저장된 값이 0이면
                chkExer.isChecked = false // checkBox를 언체크 상태로 변경
            }

            // 체크 상태가 변경된 경우(즉, 사용자가 체크박스를 클릭한 경우) 이벤트 처리
            chkExer.setOnCheckedChangeListener { _, isChecked ->
                val exerNumForUpDate = tag1 as Int // 지역변수 선언 후 tag1 값으로 초기화
                updateExerCheck(exerNumForUpDate, isChecked) // 테이블에 체크 상태를 업데이트해주는 함수 호출(현재 View 객체의 고유 번호 전달)
                Log.d("exerCheck", "exerNumForUpDate: $exerNumForUpDate, isChecked: $isChecked") // 현재 클릭된 체크박스의 고유 번호와 체크 여부 확인
            }

            // 운동 이름(버튼) 클릭 시, 타이머 화면으로 Intent 전달하며 화면 전환
            btnExerName.setOnClickListener {
                val exerTimer = tag2 as Int // 지역변수 선언 후 tag2로 초기화
                val exerName = tag3 as String // 지역변수 선언 후 tag3로 초기화
                Toast.makeText(this, "${exerTimer}분 타이머 시작", Toast.LENGTH_SHORT).show() // 해당 객체의 운동 시간 몇 분인지 확인
                val intent = Intent(this, ExerTimerPage::class.java)
                intent.putExtra("EXER_TIMER", exerTimer) // exerTimer(운동 시간) 값을 intent에 담기
                intent.putExtra("EXER_NAME", exerName) // exerName(운동 이름) 값을 intent에 담기
                startActivity(intent)
            }

            // ... 버튼 (옵션 버튼) 클릭 시, 해당 객체를 삭제할 건지 물어보는 Dialog 창 띄우기
            btnExerOpt.setOnClickListener {
                val exerNumForUpDate = tag1 as Int // 지역변수 선언 후 tag1으로 초기화
                val exerNameForDelete = tag3 as String // 지역변수 선언 후 tag3로 초기화
                dialog(it, exerNameForDelete, exerNumForUpDate) // dialog 함수 호출(View 객체, 운동 종류, 고유 번호)
            }
            exerLstLayout.addView(view) // 최종적으로 원하는 레이아웃에 해당 View 객체 추가
        }
        cursor.close()
        sqlitedb.close()
        dbManager.close()
    }

    // dialog 창에서 View 객체 삭제하겠다는 버튼을 누르면 호출되는 함수
    // 삭제되는 View 객체를 화면에서 없애기 위해, 액티비티를 새로고침해주는 함수
    fun refreshView() {
        val intent = intent
        finish()
        startActivity(intent)
    }

    // DB의 exerTBL에 check 여부를 저장하는 함수
    fun updateExerCheck(exerNum: Int, isChecked: Boolean) {
        var nowUserID = getCurrentUserId() // 현재 로그인한 사용자 ID 가져오기

        // 값을 업데이트해야 하므로 읽기 전용으로 DB 열기
        dbManager = DBManager(this, "userDB", null, 18)
        val sqlitedb = dbManager.writableDatabase

        // 이미 닫힌 DB를 reopen 하는 에러를 방지하기 위해 try-catch-finally로 작성
        try { // CheckBox의 체크 상태에 따라 exerTBL의 체크 여부 필드(exer_check)에 값을 저장
            sqlitedb.execSQL(
                "UPDATE exerTBL SET exer_check = ${if (isChecked) 1 else 0} WHERE exer_num = '$exerNum' AND user_id = '$nowUserID';"
            )
        } catch (e: Exception) { // 예외 처리
            e.printStackTrace()
        } finally { // 무조건 실행 -> DB 닫기
            sqlitedb.close()
            dbManager.close()
        }
        // 체크 상태가 업데이트되었으므로 ProgressBar 진행률 UI도 업데이트
        progressSet()
    }

    // 현재 로그인한 사용자 ID 가져오는 함수
    fun getCurrentUserId(): String? {
        var userId: String? = null // 로그인 사용자 ID를 저장할 변수

        // 값을 가져오기만 할 것이므로 읽기 전용으로 DB 열기
        dbManager = DBManager(this, "userDB", null, 18)
        val sqlitedb = dbManager.readableDatabase

        val cursorId = sqlitedb.rawQuery("SELECT * FROM session;", null) // session 테이블 값 읽어오기
        if (cursorId.moveToFirst()) {
            userId = cursorId.getString(cursorId.getColumnIndexOrThrow("userId")) // userID에 현재 로그인한 사용자의 ID 저장
        }
        cursorId.close()
        sqlitedb.close()

        return userId // 가져온 로그인 사용자 ID 리턴
    }

    // View 객체를 삭제할 것인지 결정할 수 있는 Dialog 창 띄우는 함수
    fun dialog(view: View, name: String, num: Int) {
        AlertDialog.Builder(view.context).apply {
            setTitle("운동 기록") // Dialog 창 타이틀 설정
            setMessage("${name}을/를 삭제하시겠어요?") // Dialog 창에 표시할 메시지 설정

            // 확인 버튼을 눌렀을 때
            setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                Toast.makeText(view.context, "${name} 삭제 완료", Toast.LENGTH_SHORT).show() // 삭제할 운동 이름을 토스트 메시지로 표시
                deleteExer(num) // 실제로 Table에서 해당 객체와 관련된 레코드 삭제하는 함수 호출
                refreshView() // 해당 View 객체를 화면에서 없애는 함수 호출
            })

            // 취소 버튼을 눌렀을 때
            setNegativeButton("취소", DialogInterface.OnClickListener { dialog, which ->
                Toast.makeText(view.context, "${name} 삭제 취소", Toast.LENGTH_SHORT).show() // 삭제 취소한 운동 이름을 토스트 메시지로 표시
            })
            show() // 최종적으로 Dialog창 화면에 show
        }
    }

    // exerTBL에서 삭제할 객체와 관련된 레코드 없애는 함수
    fun deleteExer(num: Int) {
        var nowUserID = getCurrentUserId() // 함수 호출하여 현재 로그인한 사용자 ID 가져오기

        // 값을 읽어오기만 할 것이므로 읽기 전용으로 DB 열기
        dbManager = DBManager(this, "userDB", null, 18)
        sqlitedb = dbManager.readableDatabase

        // 객체의 고유 번호를 통해 접근하여, exerTBL에서 해당 레코드 삭제 (user_id 필드의 값 == 현재 로그인한 사용자의 ID인 경우에만)
        sqlitedb.execSQL("DELETE FROM exerTBL WHERE user_id = '" + nowUserID + "' and exer_num = '" + num + "';")

    }
}