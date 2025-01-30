package com.example.guru2_project

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.FrameStats
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import org.w3c.dom.Text

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RecordInfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecordInfoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    // 내원일 입력하기 레이아웃,버튼,텍스트뷰
    private lateinit var showAllLayout:LinearLayout
    private lateinit var drugInsertBtn:ImageButton
    private lateinit var showAllTv:TextView
    private lateinit var showtodaysc:LinearLayout
    private lateinit var drugInsertBtn_fl: FrameLayout

    // 내원일 입력 레이아웃
    private lateinit var drugInsertLayout:LinearLayout

    // 복약 내역 입력 EditText
    private lateinit var drugInsertEdit:EditText

    // 복약 내역 입력 확인 버튼
    private lateinit var insertBtn: ImageButton

    // 내원일 확인 레이아웃
    private lateinit var drugShowLayout:LinearLayout

    // 복약 내역 확인 TextView
    private lateinit var drugShowTv:TextView

    // 복약 내역 수정/닫기 버튼
    private lateinit var drugChangeBtn: ImageButton
    private lateinit var showCloseBtn: ImageButton

    //DB 매니저/sqlitedb 초기화
    private lateinit var dbManager: DBManager
    private lateinit var sqlitedb: SQLiteDatabase

    private lateinit var reason:String // 병원 방문 목적

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view= inflater.inflate(R.layout.fragment_record_info, container, false)

        val year=arguments?.getInt("year")
        val month=arguments?.getInt("month")
        val day=arguments?.getInt("day")
        val dateInsert = String.format("%04d-%02d-%02d", year, month, day) // db 입력 날짜

        dbManager = DBManager(requireContext(), "userDB", null, 18)


        // 내원일 입력하기 버튼
        showAllLayout=view.findViewById(R.id.showAllLayout)
        drugInsertBtn=view.findViewById(R.id.drugInsertBtn)
        showAllTv=view.findViewById(R.id.showAllTv)
        showtodaysc=view.findViewById(R.id.showtodaysc)
        drugInsertBtn_fl=view.findViewById(R.id.drugInsertBtn_fl)

        // 내원일 입력 레이아웃/ eidtText/ 버튼
        drugInsertLayout=view.findViewById(R.id.drugInsertLayout)
        drugInsertEdit=view.findViewById(R.id.drugInsertEdit)
        insertBtn=view.findViewById(R.id.insertBtn)

        // 내원일 확인 레이아웃/textview/버튼
        drugShowLayout=view.findViewById(R.id.drugShowLayout)
        drugShowTv=view.findViewById(R.id.drugShowTv)
        drugChangeBtn=view.findViewById(R.id.drugChangeBtn)
        showCloseBtn=view.findViewById(R.id.showCloseBtn)

        // 내원일 입력하기 버튼 누를 시 입력 레이아웃 보임
        drugInsertBtn.setOnClickListener {
            drugInsertLayout.visibility=View.VISIBLE
            showAllLayout.visibility=View.GONE
            //drugInsertBtn.visibility=View.GONE
        }

        // 확인 버튼 누를 시 레이아웃 전환
        insertBtn.setOnClickListener {
            drugInsertLayout.visibility=View.GONE
            drugShowLayout.visibility=View.VISIBLE

            drugShowTv.setText(drugInsertEdit.text.toString())
        }

        // 수정 버튼 누를 시 다시 입력 레이아웃으로
        drugChangeBtn.setOnClickListener {
            drugInsertLayout.visibility=View.VISIBLE
            drugShowLayout.visibility=View.GONE
        }

        // 닫기 버튼 누를 시 처음 레이아웃으로
        showCloseBtn.setOnClickListener {
            showAllLayout.visibility=View.VISIBLE
            drugInsertLayout.visibility=View.GONE
            drugShowLayout.visibility=View.GONE
            showAllTv.setText(drugShowTv.text.toString())
            showtodaysc.visibility=View.VISIBLE
            drugInsertBtn_fl.visibility=View.GONE

            reason=drugShowTv.text.toString() // 방문 목적 가져올 변수

            var nowUserID = "" // 현재 사용자 ID를 저장할 변수

            // DB 읽기 전용으로 불러오기
            sqlitedb = dbManager.readableDatabase

            // session 테이블에서 현재 로그인한 사용자의 ID 가져오기
            val cursor = sqlitedb.rawQuery("SELECT * FROM session;", null)

            while (cursor.moveToNext()) {
                nowUserID = cursor.getString(cursor.getColumnIndexOrThrow("userId"))
            }
            cursor.close()

            sqlitedb=dbManager.writableDatabase
            sqlitedb.execSQL("INSERT INTO clinicRecord VALUES('"+nowUserID+"','"+dateInsert+"','"+reason+"');")
            sqlitedb.close()
            Log.d("$dateInsert"+"$reason","db입력 확인")
        }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RecordInfoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(year: Int, month: Int, day:Int) :RecordInfoFragment{
            val fragment=RecordInfoFragment()
            val args=Bundle()
            args.putInt("year",year)
            args.putInt("month",month)
            args.putInt("day",day)
            fragment.arguments=args

            return fragment
        }

    }
}