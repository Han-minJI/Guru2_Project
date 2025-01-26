package com.example.guru2_project

import android.app.TimePickerDialog
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
import android.os.Bundle
import android.view.Gravity
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.time.LocalDate

class HabRegPage : AppCompatActivity() {
    lateinit var btnBckToHabLst : ImageButton   // 복약 체크리스트 화면으로 가는 Button

    lateinit var edtHabName : EditText          // 복용약 입력하는 EditText

    lateinit var switchDayOfWeek : SwitchCompat // 모든 요일 선택하는 Swtich 버튼
    lateinit var cGroupDayOfWeek : ChipGroup    // 각각의 요일 선택하는 Chip 그룹

    // 각각의 요일 Chip 버튼 (일요일 ~ 토요일)
    lateinit var chipSun: Chip
    lateinit var chipMon: Chip
    lateinit var chipTue: Chip
    lateinit var chipWed: Chip
    lateinit var chipThur: Chip
    lateinit var chipFri: Chip
    lateinit var chipSat: Chip

    lateinit var edtHabTime : EditText      // 시간 입력 EditText
    lateinit var edtHabCnt : EditText

    lateinit var btnHabReg : ImageButton    // 복약 등록 버튼 -> 클릭 시 <MedLstPage - 복약 리스트> 화면으로 이동

    var timePickerDialog: TimePickerDialog? = null

    // !!! DBManager에 코드 추가 -> 버전 업글 -> 주석 해제 !!!
    lateinit var dbManager: DBManager    // DBManager 객체
    lateinit var sqlitedb: SQLiteDatabase  // SQLiteDatabase 객체
    // !!! DBManager에 코드 추가 -> 버전 업글 -> 주석 해제 !!!

    // 사용자가 선택한 요일을 저장할 변수 -> 복약 리스트 화면에서 오늘 요일에 해당하는 복약 체크리스트만 표시되기 위해 필요
    lateinit var userDayOfWeek : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hab_reg_page)

        // Layout의 위젯 ID와 앞서 선언한 변수 연결
        btnBckToHabLst = findViewById(R.id.btnBckToHabLst)

        edtHabName = findViewById(R.id.edtHabName)            // 복용약 입력하는 EditText

        switchDayOfWeek = findViewById(R.id.switchDayOfWeek)  // 모든 요일 선택하는 Swtich 버튼
        cGroupDayOfWeek = findViewById(R.id.cGroupDayOfWeek)  // 각각의 요일 선택하는 Chip 그룹

        // 각각의 요일 Chip 버튼 (일요일 ~ 토요일)
        chipSun = findViewById(R.id.chipSun)
        chipMon = findViewById(R.id.chipMon)
        chipTue = findViewById(R.id.chipTue)
        chipWed = findViewById(R.id.chipWed)
        chipThur = findViewById(R.id.chipThur)
        chipFri = findViewById(R.id.chipFri)
        chipSat = findViewById(R.id.chipSat)

        edtHabTime = findViewById(R.id.edtHabTime)   // 시간 입력 EditText
        edtHabCnt = findViewById(R.id.edtHabCnt)
        btnHabReg = findViewById(R.id.btnHabReg)    // 복약 등록 버튼

        // "매일" Swtich 버튼의 Check 상태가 바뀌면 (사용자가 클릭을 하면)
        switchDayOfWeek.setOnCheckedChangeListener { CompoundButton, onSwitch ->
            if (onSwitch) { // Switch 버튼을 킨 경우
                // 모든 요일 Chip 버튼의 체크 여부를 "true"로 지정
                chipSun.isChecked = true
                chipMon.isChecked = true
                chipTue.isChecked = true
                chipWed.isChecked = true
                chipThur.isChecked = true
                chipFri.isChecked = true
                chipSat.isChecked = true
            }
            else { // Switch 버튼을 끈 경우
                // 모든 요일 Chip 버튼의 체크 여부를 "false"로 지정
                chipSun.isChecked = false
                chipMon.isChecked = false
                chipTue.isChecked = false
                chipWed.isChecked = false
                chipThur.isChecked = false
                chipFri.isChecked = false
                chipSat.isChecked = false
            }
        }

        // 요일 Chip Group에 속한 Chip Button의 체크 상태가 바뀔 때
        cGroupDayOfWeek.setOnCheckedStateChangeListener { group, checkedID ->

            // 현재 선택된 모든 Chip을 확인(id 이용)하여, 뒤에 공백을 넣어 연결
            userDayOfWeek = group.checkedChipIds.joinToString(" ") { id ->
                when(id) {
                    // 각각의 chip 버튼 클릭 시 -> 해당 요일명 지정
                    R.id.chipSun -> "sun"
                    R.id.chipMon -> "mon"
                    R.id.chipTue -> "tue"
                    R.id.chipWed -> "wed"
                    R.id.chipThur -> "thur"
                    R.id.chipFri -> "fri"
                    R.id.chipSat -> "sat"
                    else -> "" // 사용자가 아무것도 선택하지 않은 경우
                }
            } + " "
        }

        // 시간 입력하는 EditText 클릭 시
        edtHabTime.setOnClickListener {

            val calendar = Calendar.getInstance() // Calaendar 인스턴스화

            // TimePicker 다이얼로그 설정
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)

                // 사용자가 Time Spinner 통해 시간 선택 -> 선택한 시간이 "HH:mm" 형태로 edtMedTime의 Text 값에 저장됨.
                edtHabTime.setText(SimpleDateFormat("HH:mm").format(calendar.time))

            }
            // TimePicker 다이얼로그 최종적으로 View
            timePickerDialog = TimePickerDialog(
                this,
                R.style.MedRegTimePickerTheme,
                timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            // 다이얼로그의 위치를 하단으로 조정
            timePickerDialog?.window?.apply {  // 하단 배치 적용
                setGravity(Gravity.BOTTOM)
                attributes = attributes.apply { // 하단 여백 제거 적용
                    y = 0
                }
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명 설정
            }
            timePickerDialog?.show() // 최종적으로 시간 선택 다이얼로그 표시
        }

        // 제일 하단의 "등록하기" 버튼 클릭 시
        btnHabReg.setOnClickListener {
            var nowUserID = "" // 현재 사용자 ID를 저장할 변수

            // !!! DBManager에 코드 추가 -> 버전 업글 -> 주석 해제 !!!
            // DB 읽기 전용으로 불러오기
            dbManager = DBManager(this, "userDB", null, 10)
            sqlitedb = dbManager.readableDatabase

            // session 테이블에서 현재 로그인한 사용자의 ID 가져오기
            val cursor = sqlitedb.rawQuery("SELECT * FROM session;", null)

            while (cursor.moveToNext()) {
                nowUserID = cursor.getString(cursor.getColumnIndexOrThrow("userId"))
            }
            cursor.close()

            // DB 쓰기 전용으로 불러오기 (등록할 때 값을 Insert 해야 하므로)
            dbManager = DBManager(this, "userDB", null, 10)
            sqlitedb = dbManager.writableDatabase

            // mediTBL에 사용자가 입력한 (약 이름, 선택 요일, 선택 시간)을 저장
            // 사용자 ID, 오늘 날짜, 체크 여부도 함께 저장
            sqlitedb.execSQL(
                "INSERT INTO habitTBL (user_id, habit_name, habit_day_of_week, habit_time, habit_date, habit_content, habit_check) VALUES ('" +
                        nowUserID + "', '" +
                        edtHabName.text.toString() + "', '" +
                        userDayOfWeek + "', '" +
                        edtHabTime.text.toString() + "', '" +
                        LocalDate.now().toString() + "', '" +
                        edtHabCnt.text.toString() + "', 0)"
            )
            sqlitedb.close()
            // !!! DBManager에 코드 추가 -> 버전 업글 -> 주석 해제 !!!

            // MedLstPage (복약 체크리스트 화면)으로 Intent 전달하며, 해당 화면으로 화면 전환
            val intent = Intent(this, HabLstPage::class.java)
            startActivity(intent)
        }

        btnBckToHabLst.setOnClickListener { // 이전 버튼 클릭 시 이벤트 처리
            // MedLstPage (복약 체크리스트 화면)으로 Intent 전달하며, 해당 화면으로 화면 전환
            val intent= Intent(this,HabLstPage::class.java)
            startActivity(intent)
        }
    }

    // 액티비티 소멸 시 호출되는 onDestroy 함수 오버라이딩
    override fun onDestroy() {
        super.onDestroy()
        timePickerDialog?.dismiss() // 시간 선택 다이얼로그 닫기 (관련 에러 해결을 위해 코드 추가)
    }
}