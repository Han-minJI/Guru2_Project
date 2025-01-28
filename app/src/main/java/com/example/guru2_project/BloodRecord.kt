package com.example.guru2_project

import android.app.Application
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.guru2_project.databinding.BloodRecordBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BloodRecord : AppCompatActivity() {
    private lateinit var binding: BloodRecordBinding
    private lateinit var bloodInsertBtn: ImageButton// 기록 완료 버튼
    private lateinit var bloodInsertEdt: EditText//혈당입력 EditText

    private lateinit var dbManager: DBManager
    private lateinit var sqlitedb: SQLiteDatabase
    //private val binding get()=_binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // 스피너 바인딩 설정
        binding=BloodRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //오늘 날짜 받아오기
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)
        var currentTime:String?=null // 현재 혈당체크 시간


        bloodInsertBtn=findViewById(R.id.bloodInsertBtn)
        bloodInsertEdt=findViewById(R.id.bloodInsertEdt)

        dbManager = DBManager(this, "userDB", null, 14)


        // 스피너 항목에 들어갈 stringArray
        val menuList= resources.getStringArray(R.array.bloodTime).toList()

        val adapter=OptionSpinnerAdapter(this,R.layout.item_spinner,menuList)
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        binding.spinner.adapter=adapter


        // Spinner의 항목이 선택될 때의 리스너
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // 선택한 항목의 텍스트를 TextView에 표시
                //if(position==0) return

                currentTime= parent.getItemAtPosition(position).toString()
                //tv_title.text = "$selectedItem"

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 아무것도 선택되지 않았을 때 처리
            }
        }




        bloodInsertBtn.setOnClickListener {

            // DB 읽기 전용으로 불러오기
            sqlitedb = dbManager.readableDatabase
            var nowUserID = "" // 현재 사용자 ID를 저장할 변수

            // session 테이블에서 현재 로그인한 사용자의 ID 가져오기
            val idCursor = sqlitedb.rawQuery("SELECT * FROM session;", null)

            while (idCursor.moveToNext()) {
                nowUserID = idCursor.getString(idCursor.getColumnIndexOrThrow("userId"))
            }
            idCursor.close()

            val nowBlood=bloodInsertEdt.text.toString()

            sqlitedb=dbManager.writableDatabase
            try{
                sqlitedb.execSQL("INSERT INTO bloodRecord VALUES('"+nowUserID+"','"+currentDate+"','"+nowBlood+"','"+currentTime+"');")
                //Log.d("$nowUserID,"+"$currentDate,"+"$nowBlood,"+"$currentTime","현재 정보")
                Toast.makeText(applicationContext, "$nowUserID,"+"$currentDate,"+"$nowBlood,"+"$currentTime", Toast.LENGTH_SHORT).show()
                sqlitedb.close()
                val intent= Intent(this,MainPage::class.java)
                startActivity(intent)

            }catch(e:SQLiteConstraintException){
                Toast.makeText(applicationContext,"이미 입력한 시간입니다", Toast.LENGTH_SHORT).show()
            }




        }
    }


}
