package com.diplom.kotlindiplom.child

import android.content.Context
import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.diplom.kotlindiplom.BaseFragment
import com.diplom.kotlindiplom.ChooseActivity
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_child_main.*
import kotlinx.android.synthetic.main.fragment_child_my_profile.*
import kotlinx.android.synthetic.main.fragment_child_my_profile.view.*
import kotlinx.android.synthetic.main.header.*
import kotlinx.coroutines.launch
import java.lang.Exception

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

var changeEmail: Boolean = false
var cityId: Int? = -1
var schoolId: Int? = -1
class ChildMyProfileFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.setTitle("Мой профиль")

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
        return inflater.inflate(R.layout.fragment_child_my_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().invalidateOptionsMenu()
        changeEmail = false
        loadInformationFromFirebase()

        val network = FunctionsApi(cityId)
        val firebase = FunctionsFirebase()

        selectphotoButtonChildmyprofile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        emailEditTextChildmyprofile.setOnClickListener {
            changeEmail = true
        }

        emailEditTextChildmyprofile.setOnKeyListener { v, keyCode, event ->
            v.emailEditTextChildmyprofile.isCursorVisible = true
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                val imn =
                    activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                //imn.hideSoftInputFromWindow(requireActivity().window.decorView.windowToken, 0)
                true
            } else {
                false
            }
        }

        saveChangeButtonChildMyProfile.setOnClickListener {
            firebase.uploadImageToFirebase(selectedPhotoUri, requireActivity(),"children")
            saveChangeInChildProfile()
        }

        emailEditTextChildmyprofile.doAfterTextChanged {
            saveChangeButtonChildMyProfile.isVisible = true
        }

        val  cities: MutableList<City> = mutableListOf()
        cityEditTextChildMyProfile.doAfterTextChanged {
            saveChangeButtonChildMyProfile.isVisible = true
            educationalInstitutionEditTextChildMyProfile.text.clear()
            schoolId = -1
            network.getNodeCities(cityEditTextChildMyProfile,requireContext(),cities)
        }
        cityEditTextChildMyProfile.setOnItemClickListener { parent, view, position, id ->
            cityId = cities[id.toInt()].id
        }

        val schools: MutableList<SchoolClass> = mutableListOf()
        educationalInstitutionEditTextChildMyProfile.doAfterTextChanged {
            saveChangeButtonChildMyProfile.isVisible = true
            network.getNodeSchools(educationalInstitutionEditTextChildMyProfile,requireContext(),schools,cityId)
        }
        educationalInstitutionEditTextChildMyProfile.setOnItemClickListener { parent, view, position, id ->
            schoolId = schools[id.toInt()].id
        }

    }

    var selectedPhotoUri: Uri? = null

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d(TAG, "Photo was select")
            //Новый способ загрузки картинки из галереи
            selectedPhotoUri = data.data
            val source =
                ImageDecoder.createSource(requireActivity().contentResolver, selectedPhotoUri!!)
            val bitmap = ImageDecoder.decodeBitmap(source)
            selectphotoImageviewChildmyprofile.setImageBitmap(bitmap)
            requireActivity().photoImageviewDrawer.setImageBitmap(bitmap)
            selectphotoButtonChildmyprofile.alpha = 0f
        }
    }
    var childId : Int = 0
    private fun loadInformationFromFirebase() {
        val firebase = FunctionsFirebase()

        firebase.getChild(firebase.uidUser!!,object : FirebaseCallback<Child>{
            override fun onComplete(value: Child) {
                if (value.profileImageUrl.isNotEmpty()){
                    val header = requireActivity().navViewChild.getHeaderView(0);
                    val photo =
                        header.findViewById<CircleImageView>(R.id.photoImageviewDrawer)
                    Glide.with(requireActivity()).load(value.profileImageUrl)
                        .into(photo)
                    //Загрузка изображения в профиль
                    Glide.with(requireActivity()).load(value.profileImageUrl)
                        .diskCacheStrategy(
                            DiskCacheStrategy.ALL
                        ).into(selectphotoImageviewChildmyprofile)
                    selectphotoButtonChildmyprofile.alpha = 0f
                }
                usernameEditTextChildMyProfile.setText(value.username)
                emailEditTextChildmyprofile.setText(value.email)
                pointTextViewChildMyProfile.text = value.point.toString()
                cityEditTextChildMyProfile.setText(value.city)
                educationalInstitutionEditTextChildMyProfile.setText(value.educationalInstitution)
                cityId = value.cityId.toString().toInt()
                schoolId =value.educationalInstitutionId.toString().toInt()
                idTextViewChildMyProfile.text = "id: " + value.id;
                saveChangeButtonChildMyProfile.isVisible = false
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun saveChangeInChildProfile() {
        val username =  usernameEditTextChildMyProfile.text.toString();
        val city =  cityEditTextChildMyProfile.text.toString()
        val educationalInstitution = educationalInstitutionEditTextChildMyProfile.text.toString()
        val email = emailEditTextChildmyprofile.text.toString()
        val point = pointTextViewChildMyProfile.text.toString().toInt()

        val user = FirebaseAuth.getInstance().currentUser
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/children/$uid")
        if (changeEmail) {
            user?.updateEmail(email)
                ?.addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener
                    if (emailEditTextChildmyprofile.text != null) {
                        ref.child("email").setValue(email)
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
        ref.child("username").setValue(username)
        ref.child("cityId").setValue(cityId)
        ref.child("city").setValue(city)
        ref.child("educationalInstitutionId").setValue(schoolId)
        ref.child("educationalInstitution")
            .setValue(educationalInstitution)
        saveChangeButtonChildMyProfile.isVisible = false

        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        if (netInfo != null && netInfo.isConnected){
            Toast.makeText(requireContext(), "Изменения успешно сохранены", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(requireContext(), "Изменения будут сохранены, когда вы подключитесь к интернету", Toast.LENGTH_SHORT).show()
        }

    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {

        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MyProfileFragment.
         */
        val TAG = "ChildMyProfile"

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChildMyProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
