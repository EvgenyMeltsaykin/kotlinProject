package com.diplom.kotlindiplom.parent

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.diplom.kotlindiplom.child.changeEmail
import com.diplom.kotlindiplom.child.cityId
import com.diplom.kotlindiplom.models.City
import com.diplom.kotlindiplom.models.FunctionsApi
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.Parent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_parent_main.*
import kotlinx.android.synthetic.main.fragment_child_my_profile.*
import kotlinx.android.synthetic.main.fragment_parent_my_profile.*
import kotlinx.android.synthetic.main.fragment_parent_my_profile.view.*
import kotlinx.android.synthetic.main.header.*
import kotlinx.coroutines.launch
import java.lang.Exception

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

class ParentMyProfileFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        return inflater.inflate(R.layout.fragment_parent_my_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        changeEmail = false
        loadInformationFromFirebase()

        val network = FunctionsApi(cityId)
        val firebase = FunctionsFirebase()
        selectPhotoButtonParentMyProfile.setOnClickListener {
            Log.d("TAG", "Click on select photo")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        emailEditTextParentMyProfile.setOnClickListener {
            changeEmail = true
        }
        emailEditTextParentMyProfile.setOnKeyListener { v, keyCode, event ->
            v.emailEditTextParentMyProfile.isCursorVisible = true
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                val imn =
                    activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imn.hideSoftInputFromWindow(requireActivity().window.decorView.windowToken, 0)
                true
            } else {
                false
            }
        }


        saveChangeButtonParentMyProfile.setOnClickListener {
            firebase.uploadImageToFirebase(selectedPhotoUri, requireActivity(), "parents")
            saveChangeInParentProfile()
        }

        emailEditTextParentMyProfile.doAfterTextChanged {
            saveChangeButtonParentMyProfile.isVisible = true
        }

        val cities: MutableList<City> = mutableListOf()
        cityEditTextParentMyProfile.doAfterTextChanged {
            saveChangeButtonParentMyProfile.isVisible = true
            network.getNodeCities(cityEditTextParentMyProfile, requireContext(), cities)
        }

        cityEditTextParentMyProfile.setOnItemClickListener { parent, view, position, id ->
            cityId = cities[id.toInt()].id
        }

    }

    var selectedPhotoUri: Uri? = null

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("TAG", "Photo was select")
            //Новый способ загрузки картинки из галереи
            selectedPhotoUri = data.data
            val source =
                ImageDecoder.createSource(requireActivity().contentResolver, selectedPhotoUri!!)
            val bitmap = ImageDecoder.decodeBitmap(source)
            selectPhotoImageviewParentMyProfile.setImageBitmap(bitmap)
            requireActivity().photoImageviewDrawer.setImageBitmap(bitmap)
            selectPhotoButtonParentMyProfile.alpha = 0f
        }
    }

    private fun loadInformationFromFirebase() {
        val firebase = FunctionsFirebase()

        firebase.getParent(firebase.uidUser, object : FirebaseCallback<Parent> {
            override fun onComplete(value: Parent) {
                if (value.profileImageUrl.isNotEmpty()) {
                    //Загрузка изображения в боковое меню
                    val header = requireActivity().navViewParent.getHeaderView(0);
                    val photo =
                        header.findViewById<CircleImageView>(R.id.photoImageviewDrawer)
                    Glide.with(requireActivity()).load(value.profileImageUrl)
                        .into(photo)
                    //Загрузка изображения в профиль
                    Glide.with(requireActivity()).load(value.profileImageUrl)
                        .diskCacheStrategy(
                            DiskCacheStrategy.ALL
                        ).into(selectPhotoImageviewParentMyProfile)
                    selectPhotoButtonParentMyProfile.alpha = 0f
                }
                usernameEditTextParentMyProfile.setText(value.username)
                emailEditTextParentMyProfile.setText(value.email)
                cityEditTextParentMyProfile.setText(value.city)
                cityId = value.cityId.toString().toInt()
                saveChangeButtonParentMyProfile.isVisible = false
            }
        })
    }

    private fun saveChangeInParentProfile() {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/parents/$uid")
        if (changeEmail) {
            user?.updateEmail(emailEditTextParentMyProfile.text.toString())
                ?.addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener
                    if (emailEditTextParentMyProfile.text != null) {
                        ref.child("email").setValue(emailEditTextParentMyProfile.text.toString())
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
        ref.child("username").setValue(usernameEditTextParentMyProfile.text.toString())
        ref.child("cityId").setValue(cityId)
        ref.child("city").setValue(cityEditTextParentMyProfile.text.toString())
        saveChangeButtonParentMyProfile.isVisible = false;
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        if (netInfo != null && netInfo.isConnected){
            Toast.makeText(requireContext(), "Изменения успешно сохранены", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(requireContext(), "Изменения будут сохранены, когда вы подключитесь к интернету", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ParentMyProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ParentMyProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
