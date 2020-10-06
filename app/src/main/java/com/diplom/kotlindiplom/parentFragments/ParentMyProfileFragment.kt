package com.diplom.kotlindiplom.parentFragments

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.diplom.kotlindiplom.ChooseActivity
import com.diplom.kotlindiplom.Callback
import com.diplom.kotlindiplom.MainActivity
import com.diplom.kotlindiplom.MainActivity.FirebaseSingleton.firebase
import com.diplom.kotlindiplom.MainActivity.Network.network
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.*
import com.diplom.kotlindiplom.models.apiResponse.cities.City
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_parent_my_profile.*
import kotlinx.android.synthetic.main.fragment_parent_my_profile.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ParentMyProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
var cityId: Int? = -1
var changeEmail = false
class ParentMyProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_parent_my_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().invalidateOptionsMenu()
        activity?.title = "Мой профиль"
        changeEmail = false
        loadInformationFromFirebase()
        emailTextInputParentMyProfile?.editText?.setOnClickListener {
            changeEmail = true
        }
        emailTextInputParentMyProfile?.editText?.setOnKeyListener { v, keyCode, event ->
            v.emailTextInputParentMyProfile?.editText?.isCursorVisible = true
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                val imn =
                    activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imn.hideSoftInputFromWindow(requireActivity().window.decorView.windowToken, 0)
                true
            } else {
                false
            }
        }

        saveChangeButtonParentMyProfile?.setOnClickListener {
            //firebase.uploadImageToFirebase(selectedPhotoUri, requireActivity(), "parents")
            saveChangeInParentProfile()
        }

        usernameTextInputParentMyProfile?.editText?.doAfterTextChanged {
            saveChangeButtonParentMyProfile?.isVisible = true
        }
        emailTextInputParentMyProfile?.editText?.doAfterTextChanged {
            saveChangeButtonParentMyProfile?.isVisible = true
        }

        val cities: MutableList<City> = mutableListOf()
        cityAutoCompleteTextViewParentMyProfile?.doAfterTextChanged {
            saveChangeButtonParentMyProfile?.isVisible = true
            network.getNodeCities(
                cityAutoCompleteTextViewParentMyProfile?.text.toString(),
                cities,
                object :Callback<List<String>>{
                    override fun onComplete(value: List<String>) {
                        val adapterCity = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_list_item_1,
                            value
                        )
                        cityAutoCompleteTextViewParentMyProfile.setAdapter(adapterCity)
                        cityAutoCompleteTextViewParentMyProfile.setOnFocusChangeListener { v, hasFocus -> if (hasFocus) cityAutoCompleteTextViewParentMyProfile.showDropDown() }
                    }
                }
            )
        }

        cityAutoCompleteTextViewParentMyProfile?.setOnItemClickListener { parent, view, position, id ->
            cityId = cities[id.toInt()].id
        }
    }
    private fun loadInformationFromFirebase() {
        firebase.getParent(firebase.uidUser, object : Callback<Parent> {
            override fun onComplete(value: Parent) {
                usernameTextInputParentMyProfile?.editText?.setText(value.username)
                emailTextInputParentMyProfile?.editText?.setText(value.email)
                cityAutoCompleteTextViewParentMyProfile?.setText(value.city)
                cityId = value.cityId.toString().toInt()
                saveChangeButtonParentMyProfile?.isVisible = false
            }
        })
    }

    private fun saveChangeInParentProfile() {
        val user = FirebaseAuth.getInstance().currentUser
        val email = emailTextInputParentMyProfile?.editText?.text.toString()
        val username = usernameTextInputParentMyProfile?.editText?.text.toString()
        val city = cityAutoCompleteTextViewParentMyProfile?.text.toString()
        if (changeEmail) {
            user?.updateEmail(emailTextInputParentMyProfile?.editText?.text.toString())
                ?.addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener
                    if (emailTextInputParentMyProfile?.editText?.text != null) {
                        firebase.setFieldUserDatabase(firebase.uidUser!!,"email",email)
                    }
                    Toast.makeText(
                        requireContext(),
                        "Смена email прошла успешно, авторизуйтесь заного",
                        Toast.LENGTH_LONG
                    ).show()
                    val intent = Intent(
                        requireActivity(),
                        ChooseActivity::class.java
                    )
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
                ?.addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка при смене email: ${it.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
        firebase.setFieldUserDatabase(firebase.uidUser!!,"username",username)
        firebase.setFieldUserDatabase(firebase.uidUser!!,"cityId",cityId)
        firebase.setFieldUserDatabase(firebase.uidUser!!,"city",city)
        val header = requireActivity().navView.getHeaderView(0);
        val userNameHeader = header.findViewById<TextView>(R.id.usernameTextviewDrawer)
        userNameHeader.text = username
        saveChangeButtonParentMyProfile?.isVisible = false

        val network = FunctionsNetwork()
        if(network.checkConnect(context)){
            Toast.makeText(requireContext(), "Изменения успешно сохранены", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(requireContext(), "Изменения будут сохранены, когда вы подключитесь к интернету", Toast.LENGTH_SHORT).show()
        }
    }

}
