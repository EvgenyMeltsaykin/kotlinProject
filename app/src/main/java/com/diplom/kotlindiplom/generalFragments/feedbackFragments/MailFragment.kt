package com.diplom.kotlindiplom.generalFragments.feedbackFragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.diplom.kotlindiplom.Callback
import com.diplom.kotlindiplom.MainActivity.FirebaseSingleton.firebaseFeedback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.Feedback
import kotlinx.android.synthetic.main.fragment_mail.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MailFragment : Fragment() {

    companion object {
        private const val CODE_OTHER = 0
        private const val CODE_ADD_DIARY = 1
        private const val CODE_ADD_SCHOOLBOOK = 2
        private const val MAX_NUMBER_CONSECUTIVE_MESSAGES = 3
    }

    private var numberClass: String? = null
    private var subjectName: String? = null
    private var topic: String? = null
    private var codeQuestion = CODE_OTHER


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            numberClass = it.getString("numberClass")
            subjectName = it.getString("subjectName")
            topic = it.getString("topic")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().invalidateOptionsMenu()
        activity?.title = "Обратная связь"
        messageUserTextView?.isVisible = false

        if (topic == "Добавить учебник"){
            addSchoolBook()
            codeQuestion = CODE_ADD_SCHOOLBOOK
        }
        if (topic == "Добавить дневник"){
            addDiary()
            codeQuestion = CODE_ADD_DIARY
        }
        sendEmailButton?.setOnClickListener {
            if (contentMessageEditText?.text?.isEmpty()!! || topicMessageEditText?.text?.isEmpty()!!){
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
            }else{
                //val to = resources.getString(R.string.emailSupport)
                firebaseFeedback.getCountOpenFeedback(object :Callback<Int>{
                    override fun onComplete(value: Int) {
                        Log.d("Tag",value.toString())
                        if (value >= MAX_NUMBER_CONSECUTIVE_MESSAGES){
                            Toast.makeText(requireContext(),"Ошибка при создании обращения. Подождите пока служба поддержки ответит на ваши вопросы.",Toast.LENGTH_SHORT).show()
                        }else{
                            sendFeedback()
                            Navigation.findNavController(requireActivity(),R.id.navFragment).popBackStack()
                        }
                    }
                })
            }
        }
    }
    private fun sendFeedback(){
        val topic = topicMessageEditText?.text.toString()
        val message = contentMessageEditText?.text.toString()
        firebaseFeedback.addFeedback(codeQuestion,topic, message)
        Toast.makeText(requireContext(),"Обращение успешно отправлено",Toast.LENGTH_SHORT).show()
        topicMessageEditText?.setText("")
        contentMessageEditText?.setText("")
    }
    private fun addDiary(){
        topicMessageEditText?.setText(topic.toString())
        contentMessageEditText?.hint = "Введите электронный адрес или название электронного дневника"
    }
    private fun addSchoolBook(){
        var name = ""
        when(subjectName){
            "computerScience" -> name = resources.getString(R.string.computerScience)
            "maths" -> name = resources.getString(R.string.maths)
            "russianLanguage" -> name = resources.getString(R.string.russianLanguage)
            "geometry" -> name = resources.getString(R.string.geometry)
            "literature" -> name = resources.getString(R.string.literature)
            "biology" -> name = resources.getString(R.string.biology)
            "geography" -> name = resources.getString(R.string.geography)
            "historyOfRussia" -> name = resources.getString(R.string.historyOfRussia)
            "socialScience" -> name = resources.getString(R.string.socialScience)
            "surroundingWorld" -> name = resources.getString(R.string.surroundingWorld)
            "nativeLanguage" -> name = resources.getString(R.string.nativeLanguage)
            "literaryReading" -> name = resources.getString(R.string.literaryReading)
            "englishLanguage" -> name = resources.getString(R.string.englishLanguage)
            "germanLanguage" -> name = resources.getString(R.string.germanLanguage)
            "frenchLanguage" -> name = resources.getString(R.string.frenchLanguage)
            "physics" -> name = resources.getString(R.string.physics)
            "chemistry" -> name = resources.getString(R.string.chemistry)
        }
        messageUserTextView?.isVisible = true
        messageUserTextView?.text = "Основную информацию мы заполнили за Вас! Осталось ввести только фамилию автора"
        topicMessageEditText?.setText(topic.toString())
        contentMessageEditText?.setText("Класс: $numberClass\nПредмет: $name\nАвтор: ")
    }



}