package com.diplom.kotlindiplom.generalFragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.Callback
import com.diplom.kotlindiplom.MainActivity.FirebaseSingleton.firebase
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.diaries.Diary
import kotlinx.android.synthetic.main.fragment_login_diary.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginDiaryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginDiaryFragment : Fragment(), AdapterView.OnItemSelectedListener {
    // TODO: Rename and change types of parameters
    private var deletedDiary: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            deletedDiary = it.getBoolean("deletedDiary",false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login_diary, container, false)
    }

    var urlDiary: String = ""
    @ExperimentalStdlibApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().invalidateOptionsMenu()
        progressBar?.isVisible = true
        activity?.title = ""
        firebase.getFieldDiary(firebase.uidUser,"login",object : Callback<String>{
            override fun onComplete(value: String) {
                if(value.isNotEmpty()){
                    if (!deletedDiary){
                        val bundle = bundleOf()
                        bundle.putString("login",value)
                        Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.action_loginDiaryFragment_to_diaryFragment,bundle)
                    }else{
                        setupSpinner()
                    }
                }else{
                    setupSpinner()
                }
            }
        })
        loginDiaryTextInput?.editText?.doAfterTextChanged {
            loginDiaryTextInput?.error = null
        }
        passwordDiaryTextInput?.editText?.doAfterTextChanged {
            passwordDiaryTextInput?.error = null
        }
        enterDiaryButton?.setOnClickListener {
            if (urlDiary.isEmpty()) {
                Toast.makeText(requireContext(), "Выберите дневник", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val login = loginDiaryTextInput?.editText?.text.toString()
            val password = passwordDiaryTextInput?.editText?.text.toString()
            if (validateLogin(login, password)) {
                val diary = Diary()
                hideButtons()
                progressBar?.isVisible = true
                GlobalScope.launch(Dispatchers.Main) {
                    var rightLogin = false
                    when (urlDiary) {
                        diary.elschool.url -> rightLogin =
                            withContext(Dispatchers.IO) { diary.elschool.login(login, password) }
                    }
                    if (!rightLogin) {
                        showButtons()
                        Toast.makeText(
                            requireContext(),
                            "Не верный логин или пароль",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        firebase.createDiary(urlDiary)
                        firebase.setLoginAndPasswordDiary(login,password)
                        firebase.setFieldDiary(firebase.uidUser, "url", urlDiary)
                        firebase.updateSchedule()
                        val bundle = bundleOf()
                        bundle.putString("login",login)
                        bundle.putString("urlDiary",urlDiary)
                        Navigation.findNavController(
                            requireActivity(),
                            R.id.navFragment
                        ).navigate(R.id.action_loginDiaryFragment_to_diaryFragment,bundle)
                    }
                }
            } else {

                showButtons()
            }
        }

        writeMailButton?.setOnClickListener {
            val bundle = bundleOf()
            bundle.putString("topic","Добавить дневник")
            Navigation.findNavController(requireActivity(),R.id.navFragment).navigate(R.id.action_loginDiaryFragment_to_mailFragment,bundle)
        }
    }
    private fun showButtons(){
        enterDiaryButton?.isVisible = true
        staticMessageTextView?.isVisible = true
        writeMailButton?.isVisible = true
        loginDiaryTextInput?.editText?.isEnabled = true
        passwordDiaryTextInput?.editText?.isEnabled = true
    }
    private fun hideButtons(){
        staticMessageTextView?.isVisible = false
        writeMailButton?.isVisible = false
        enterDiaryButton?.isVisible = false
        staticMessageTextView?.isVisible = false
        writeMailButton?.isVisible = false
        loginDiaryTextInput?.editText?.isEnabled = false
        passwordDiaryTextInput?.editText?.isEnabled = false
    }
    private fun validateLogin(login:String, password:String):Boolean{
        var fl = true
        if (login.isEmpty()){
            loginDiaryTextInput?.error = resources.getString(R.string.messageEmptyField)
            fl = false
        }
        if (password.isEmpty()){
            passwordDiaryTextInput?.error = resources.getString(R.string.messageEmptyField)
            fl = false
        }
        return fl
    }
    private fun setupSpinner() {
        activity?.title = "Вход в дневник"
        progressBar?.isVisible = false
        diariesSpinnerLoginDiary?.isVisible = true
        loginDiaryTextInput?.isVisible = true
        passwordDiaryTextInput?.isVisible = true
        enterDiaryButton?.isVisible = true
        writeMailButton?.isVisible = true
        staticMessageTextView?.isVisible = true
        var arrayAdapter: ArrayAdapter<String>? = null
        firebase.getDiaries(object : Callback<List<String>> {
            override fun onComplete(value: List<String>) {
                arrayAdapter =
                    ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, value)
                arrayAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                diariesSpinnerLoginDiary?.adapter = arrayAdapter
            }
        })
        diariesSpinnerLoginDiary?.onItemSelectedListener = this
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val item = parent?.getItemAtPosition(position) as String
        if (item != "Электронного дневника нет") {
            urlDiary = item
        } else {
            urlDiary = ""
        }
    }
}