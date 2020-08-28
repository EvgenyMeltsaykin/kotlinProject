package com.diplom.kotlindiplom.parentFragments

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.diplom.kotlindiplom.ChooseActivity
import com.diplom.kotlindiplom.FirebaseCallback
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
    object Network {
        val network = FunctionsApi(cityId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.title = "Мой профиль"
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
        /*selectPhotoButtonParentMyProfile.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, 0)
            }else{
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1
                )
            }
        }*/

         */
        emailEditTextParentMyProfile?.setOnClickListener {
            changeEmail = true
        }
        emailEditTextParentMyProfile?.setOnKeyListener { v, keyCode, event ->
            v.emailEditTextParentMyProfile?.isCursorVisible = true
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

        usernameEditTextParentMyProfile?.doAfterTextChanged {
            saveChangeButtonParentMyProfile?.isVisible = true
        }
        emailEditTextParentMyProfile?.doAfterTextChanged {
            saveChangeButtonParentMyProfile?.isVisible = true
        }

        val cities: MutableList<City> = mutableListOf()
        cityEditTextParentMyProfile?.doAfterTextChanged {
            saveChangeButtonParentMyProfile?.isVisible = true
            Network.network.cityId = cityId
            Network.network.getNodeCities(cityEditTextParentMyProfile, requireContext(), cities)
        }

        cityEditTextParentMyProfile?.setOnItemClickListener { parent, view, position, id ->
            cityId = cities[id.toInt()].id
        }
    }

    //var selectedPhotoUri: Uri? = null

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /*(if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("TAG", "Photo was select")
            saveChangeButtonParentMyProfile?.isVisible = true
            //Новый способ загрузки картинки из галереи
            selectedPhotoUri = data.data
            val source =
                ImageDecoder.createSource(requireActivity().contentResolver, selectedPhotoUri!!)
            val bitmap = ImageDecoder.decodeBitmap(source)
            selectPhotoImageviewParentMyProfile.setImageBitmap(bitmap)
            requireActivity().photoImageviewDrawer.setImageBitmap(bitmap)
            selectPhotoButtonParentMyProfile.alpha = 0f
        }*/
    }

    private fun loadInformationFromFirebase() {
        val firebase = FunctionsFirebase()
        firebase.getParent(firebase.uidUser, object : FirebaseCallback<Parent> {
            override fun onComplete(value: Parent) {
                /*if (value.profileImageUrl.isNotEmpty()) {
                    //Загрузка изображения в боковое меню
                    val header = requireActivity().navView.getHeaderView(0);
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
                }*/
                usernameEditTextParentMyProfile?.setText(value.username)
                emailEditTextParentMyProfile?.setText(value.email)
                cityEditTextParentMyProfile?.setText(value.city)
                cityId = value.cityId.toString().toInt()
                saveChangeButtonParentMyProfile?.isVisible = false
                Network.network.cityId = cityId
            }
        })
    }

    private fun saveChangeInParentProfile() {
        val user = FirebaseAuth.getInstance().currentUser
        val email = emailEditTextParentMyProfile?.text.toString()
        val username = usernameEditTextParentMyProfile?.text.toString()
        val city = cityEditTextParentMyProfile?.text.toString()
        //val ref = FirebaseDatabase.getInstance().getReference("/users/parents/$uid")
        val firebase = FunctionsFirebase()
        if (changeEmail) {
            user?.updateEmail(emailEditTextParentMyProfile?.text.toString())
                ?.addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener
                    if (emailEditTextParentMyProfile?.text != null) {
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
