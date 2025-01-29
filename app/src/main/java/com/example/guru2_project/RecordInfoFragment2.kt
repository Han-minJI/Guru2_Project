package com.example.guru2_project

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RecordInfoFragment2.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecordInfoFragment2 : Fragment() {

    // 방문 목적 입력 후 보여줄 화면
    private lateinit var showAllTv2: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_record_info2, container, false)

        val reason=arguments?.getString("reason")

        showAllTv2=view.findViewById(R.id.showAllTv2)
        showAllTv2.text=reason.toString() // db에서 받아온 방문 목적 입력


        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RecordInfoFragment2.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(reason: String): RecordInfoFragment2 {
            val fragment = RecordInfoFragment2()
            val args = Bundle()
            args.putString("reason", reason)
            fragment.arguments = args

            return fragment
        }
    }
}