package com.diplom.kotlindiplom.generalFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.FunctionsFirebase
import kotlinx.android.synthetic.main.activity_registry.diariesSpinner
import kotlinx.android.synthetic.main.fragment_weekday.diaryTextView
import kotlinx.android.synthetic.main.fragment_weekday_without_diary.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [WeekdayWithoutDiaryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WeekdayWithoutDiaryFragment : Fragment(), AdapterView.OnItemSelectedListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    var role: String = ""
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
        return inflater.inflate(R.layout.fragment_weekday_without_diary, container, false)
    }
    var urlDiary:String = ""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val firebase = FunctionsFirebase()
        firebase.getFieldDiaryWithRole(firebase.uidUser!!,"url", object : FirebaseCallback<List<String>>{
            override fun onComplete(value: List<String>) {
                if (value[0].isNotEmpty()){
                    if (value[1] == "child"){
                        Navigation.findNavController(requireActivity(), R.id.navFragmentChild)
                            .navigate(R.id.action_weekdayWithoutDiaryFragment_to_weekdayFragment)
                    }else{
                        Navigation.findNavController(requireActivity(), R.id.navFragmentParent)
                            .navigate(R.id.action_weekdayWithoutDiaryFragment_to_weekdayFragment)
                    }
                }else{
                    setupSpinner()
                    diaryTextView.text =
                        "Расписание недоступно.\nВыберите электронный дневник"
                }
            }
        })
        enterDiaryButton.setOnClickListener {
            if (urlDiary.isEmpty()){
                Toast.makeText(requireContext(),"Выберите дневник",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (loginDiaryEditText.text.isNotEmpty() && passwordDiaryEditText.text.isNotEmpty()){
                val login = loginDiaryEditText.text.toString()
                val password = passwordDiaryEditText.text.toString()
                val firebase = FunctionsFirebase()
                firebase.setFieldDatabase(firebase.uidUser!!,"diary/login",login)
                firebase.setFieldDatabase(firebase.uidUser!!,"diary/password",password)
                firebase.setFieldDatabase(firebase.uidUser!!,"diary/url",urlDiary)
                firebase.getRoleByUid(firebase.uidUser!!, object : FirebaseCallback<String>{
                    override fun onComplete(value: String) {
                        if(value == "child"){
                            Navigation.findNavController(requireActivity(),R.id.navFragmentChild).navigate(R.id.action_weekdayWithoutDiaryFragment_to_weekdayFragment)
                        }else{
                            Navigation.findNavController(requireActivity(),R.id.navFragmentParent).navigate(R.id.action_weekdayWithoutDiaryFragment_to_weekdayFragment)
                        }
                    }
                })
            }else{
                Toast.makeText(requireContext(),"Войти не удалось",Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun setupSpinner(){
        val firebase = FunctionsFirebase()
        var arrayAdapter: ArrayAdapter<String>? = null
        firebase.getDiaries(object : FirebaseCallback<List<String>> {
            override fun onComplete(value: List<String>) {
                arrayAdapter =
                    ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, value)
                arrayAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                diariesSpinner.adapter = arrayAdapter
            }
        })
        diariesSpinner.onItemSelectedListener = this
    }
    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val item = parent?.getItemAtPosition(position) as String
        if (item != "Электронного дневника нет") {
            urlDiary = item

            //Navigation.findNavController(requireActivity(),R.id.navFragmentChild).navigate(R.id.action_weekdayWithoutDiaryFragment_to_weekdayFragment)
        }else{
            urlDiary = ""
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment WeekdayWithoutDiaryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WeekdayWithoutDiaryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}