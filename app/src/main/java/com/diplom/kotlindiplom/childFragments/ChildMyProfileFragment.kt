package com.diplom.kotlindiplom.childFragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.diplom.kotlindiplom.ChooseActivity
import com.diplom.kotlindiplom.Callback
import com.diplom.kotlindiplom.MainActivity
import com.diplom.kotlindiplom.MainActivity.FirebaseSingleton.firebase
import com.diplom.kotlindiplom.MainActivity.Network.network
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.*
import com.diplom.kotlindiplom.models.apiResponse.cities.City
import com.diplom.kotlindiplom.models.apiResponse.schoolClass.SchoolClass
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_child_my_profile.*
//import kotlinx.android.synthetic.main.fragment_child_my_profile.*
import kotlinx.android.synthetic.main.fragment_child_my_profile.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

var changeEmail: Boolean = false
var cityId: Int? = -1
var schoolId: Int? = -1

class ChildMyProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.fragment_child_my_profile, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().invalidateOptionsMenu()
        activity?.title = "Мой профиль"
        changeEmail = false
        loadInformationFromFirebase()
        emailTextInputChildMyProfile?.editText?.setOnClickListener {
            changeEmail = true
        }
        usernameTextInputChildMyProfile?.editText?.doAfterTextChanged {
            saveChangeButtonChildMyProfile?.isVisible = true
        }
        emailTextInputChildMyProfile?.editText?.setOnKeyListener { v, keyCode, event ->
            v.emailTextInputChildMyProfile?.editText?.isCursorVisible = true
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                val imn =
                    activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imn.hideSoftInputFromWindow(requireActivity().window.decorView.windowToken, 0)
                true
            } else {
                false
            }
        }

        saveChangeButtonChildMyProfile?.setOnClickListener {
            saveChangeInChildProfile()
        }

        emailTextInputChildMyProfile?.editText?.doAfterTextChanged {
            saveChangeButtonChildMyProfile?.isVisible = true
        }

        val cities: MutableList<City> = mutableListOf()
        val schools: MutableList<SchoolClass> = mutableListOf()
        cityAutoCompleteTextViewChildMyProfile?.doAfterTextChanged {
            saveChangeButtonChildMyProfile?.isVisible = true
            educationalInstitutionAutoCompleteTextViewChildMyProfile?.text?.clear()
            schoolId = -1
            network.getNodeCities(
                cityAutoCompleteTextViewChildMyProfile!!,
                requireContext(),
                cities
            )
        }
        cityAutoCompleteTextViewChildMyProfile?.setOnItemClickListener { parent, view, position, id ->
            cityId = cities[id.toInt()].id
            network.getNodeSchools(
                schools,
                educationalInstitutionAutoCompleteTextViewChildMyProfile.text.toString(),
                cityId!!,
                object : Callback<List<String>> {
                    override fun onComplete(value: List<String>) {
                        val adapterEducational: ArrayAdapter<String> = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_list_item_1,
                            value
                        )
                        educationalInstitutionAutoCompleteTextViewChildMyProfile.setAdapter(adapterEducational                        )
                        educationalInstitutionAutoCompleteTextViewChildMyProfile.setOnFocusChangeListener { v, hasFocus -> if (hasFocus) educationalInstitutionAutoCompleteTextViewChildMyProfile.showDropDown() }
                    }
                }
            )
        }
        educationalInstitutionAutoCompleteTextViewChildMyProfile?.doAfterTextChanged {
            saveChangeButtonChildMyProfile?.isVisible = true
            network.getNodeSchools(
                schools,
                educationalInstitutionAutoCompleteTextViewChildMyProfile.text.toString(),
                cityId!!,
                object : Callback<List<String>> {
                    override fun onComplete(value: List<String>) {
                        val adapterEducational: ArrayAdapter<String> = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_list_item_1,
                            value
                        )
                        educationalInstitutionAutoCompleteTextViewChildMyProfile.setAdapter(adapterEducational)
                        educationalInstitutionAutoCompleteTextViewChildMyProfile.setOnFocusChangeListener { v, hasFocus -> if (hasFocus) educationalInstitutionAutoCompleteTextViewChildMyProfile.showDropDown() }
                    }
                }
            )
        }
        educationalInstitutionAutoCompleteTextViewChildMyProfile?.setOnItemClickListener { parent, view, position, id ->
            schoolId = schools[id.toInt()].id
        }
    }

    private fun loadInformationFromFirebase() {

        firebase.getChild(firebase.uidUser!!, object : Callback<Child> {
            override fun onComplete(value: Child) {
                val header = requireActivity().navView.getHeaderView(0);
                val userNameHeader = header.findViewById<TextView>(R.id.usernameTextviewDrawer)
                userNameHeader.text = value.username
                usernameTextInputChildMyProfile?.editText?.setText(value.username)
                emailTextInputChildMyProfile?.editText?.setText(value.email)
                pointTextViewChildMyProfile?.text = value.point.toString()
                cityAutoCompleteTextViewChildMyProfile?.setText(value.city)
                educationalInstitutionAutoCompleteTextViewChildMyProfile?.setText(value.educationalInstitution)
                cityId = value.cityId.toString().toInt()
                schoolId = value.educationalInstitutionId.toString().toInt()
                idTextViewChildMyProfile?.text = "id: " + value.id;
                saveChangeButtonChildMyProfile?.isVisible = false
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun saveChangeInChildProfile() {
        val username = usernameTextInputChildMyProfile?.editText?.text.toString();
        val city = cityAutoCompleteTextViewChildMyProfile?.text.toString()
        val educationalInstitution =
            educationalInstitutionAutoCompleteTextViewChildMyProfile?.text.toString()
        val email = emailTextInputChildMyProfile?.editText?.text.toString()
        val point = pointTextViewChildMyProfile?.text.toString().toInt()

        val user = FirebaseAuth.getInstance().currentUser
        if (changeEmail) {
            user?.updateEmail(email)
                ?.addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener
                    if (emailTextInputChildMyProfile?.editText?.text != null) {
                        firebase.setFieldUserDatabase(firebase.uidUser!!, "email", email)
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
                    intent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
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
        val header = requireActivity().navView.getHeaderView(0);
        val userNameHeader = header.findViewById<TextView>(R.id.usernameTextviewDrawer)
        userNameHeader.text = username
        firebase.setFieldUserDatabase(firebase.uidUser!!, "username", username)
        firebase.setFieldUserDatabase(firebase.uidUser!!, "cityId", cityId)
        firebase.setFieldUserDatabase(firebase.uidUser!!, "city", city)
        firebase.setFieldUserDatabase(firebase.uidUser!!, "educationalInstitutionId", schoolId)
        firebase.setFieldUserDatabase(
            firebase.uidUser,
            "educationalInstitution",
            educationalInstitution
        )
        saveChangeButtonChildMyProfile?.isVisible = false

        val network = FunctionsNetwork()
        if (network.checkConnect(context)) {
            Toast.makeText(requireContext(), "Изменения успешно сохранены", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(
                requireContext(),
                "Изменения будут сохранены, когда вы подключитесь к интернету",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    override fun onDetach() {
        super.onDetach()
    }

}
