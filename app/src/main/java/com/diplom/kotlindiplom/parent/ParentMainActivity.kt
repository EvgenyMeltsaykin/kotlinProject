package com.diplom.kotlindiplom.parent

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.diplom.kotlindiplom.ChooseActivity
import com.diplom.kotlindiplom.FirebaseCallback
import com.diplom.kotlindiplom.R
import com.diplom.kotlindiplom.models.Child
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.FunctionsUI
import com.diplom.kotlindiplom.models.Parent
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_child_main.*
import kotlinx.android.synthetic.main.activity_parent_main.*
import kotlinx.android.synthetic.main.header.*
import kotlinx.android.synthetic.main.header.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

class ParentMainActivity : AppCompatActivity() {
    private var drawer: DrawerLayout? = null
    private var back_pressed: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parent_main)
        setupDrawerAndToolbar()
        //Нажатие на аватарку в боковом меню
        val header = navViewParent.getHeaderView(0);
        val photo = header.findViewById<CircleImageView>(R.id.photoImageviewDrawer)
        photo.setOnClickListener {
            val navController = Navigation.findNavController(
                this,
                R.id.navFragmentParent
            )
            navController.navigate(R.id.parentMyProfileFragment)
            drawer = findViewById(R.id.drawerLayoutParent)
            drawer?.closeDrawer(GravityCompat.START)
        }
        //Обработка нажатия выхода в боковом меню
        val menu = navViewParent.menu
        val quit = menu.findItem(R.id.parentExitApplication)
        quit.setOnMenuItemClickListener {
            FirebaseAuth.getInstance().signOut()
            intent = Intent(this, ChooseActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            return@setOnMenuItemClickListener true
        }
        val firebase = FunctionsFirebase()
        val uiFunctions = FunctionsUI()
        val ref = firebase.parentRef.child(firebase.uidUser!!)
        ref.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                if (p0.key == "acceptAnswer") {
                    if (p0.value.toString() == "0") {
                        Toast.makeText(
                            this@ParentMainActivity,
                            "Ваш запрос отклонен",
                            Toast.LENGTH_SHORT
                        ).show()
                        firebase.setFieldDatabaseParent(firebase.uidUser!!, "acceptAnswer", "-1")
                    }
                    if (p0.value.toString() == "1") {
                        Toast.makeText(
                            this@ParentMainActivity,
                            "Ваш запрос принят",
                            Toast.LENGTH_SHORT
                        ).show()
                        firebase.setFieldDatabaseParent(firebase.uidUser!!, "acceptAnswer", "-1")
                    }
                }
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                return
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                TODO("Not yet implemented")
            }

        })

        val newTaskRef = firebase.taskRef
        newTaskRef.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("Not yet implemented")
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val task = firebase.getAllFieldsTask(p0)
                if (task.showNotification == 1 || task.status != 0) return
                uiFunctions.createNotificationParent(
                    applicationContext,
                    ParentMainActivity::class.java,
                    R.drawable.ic_launcher_background,
                    "Ребенок выполнил задание",
                    "Ребенок выполнил задание",
                    task.title,
                    task
                )

            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                return
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                return
            }


        })
    }

    override fun invalidateOptionsMenu() {
        super.invalidateOptionsMenu()
    }

    override fun onBackPressed() {
        val navController = Navigation.findNavController(
            this,
            R.id.navFragmentParent
        )
        if (navController.currentDestination?.id == R.id.parentAllTasksFragment || navController.currentDestination?.id == R.id.parentNewTaskFragment) {
            navController.popBackStack()
            setTitle("Задания")
            return
        }
        if (navController.currentDestination?.id == R.id.parentTaskContentFragment) {
            navController.popBackStack()
            setTitle("")
            return
        }
        if (navController.currentDestination?.id == R.id.sheduleDayFragment) {
            navController.popBackStack()
            setTitle("Дни недели")
            return
        }
        if (drawer?.isDrawerOpen(GravityCompat.START) == true) {
            drawer?.closeDrawer(GravityCompat.START)
        } else if (back_pressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
            finishAffinity()
        } else {
            Toast.makeText(this, "Для выхода нажмите \"Назад\" ещё раз", Toast.LENGTH_SHORT).show()
        }

        back_pressed = System.currentTimeMillis();

    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        //Отключение клавиатуры
        if (currentFocus != null) {
            val imn = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imn.hideSoftInputFromWindow(window.decorView.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun setupDrawerAndToolbar() {
        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.navFragmentParent) as NavHostFragment? ?: return
        val navController = host.navController
        val sideBar = findViewById<NavigationView>(R.id.navViewParent)
        sideBar?.setupWithNavController(navController)
        val toolBar = findViewById<Toolbar>(R.id.parentToolbar)
        setSupportActionBar(toolBar)
        drawer = findViewById(R.id.drawerLayoutParent)
        val toggle = ActionBarDrawerToggle(
            this,
            drawer,
            toolBar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer?.addDrawerListener(toggle)
        toggle.syncState()
        //Загрузка фото и имени в боковое меню при запуске приложения
        val firebase = FunctionsFirebase()
        val header = navViewParent.getHeaderView(0)
        firebase.getParent(firebase.uidUser!!,object : FirebaseCallback<Parent> {
            override fun onComplete(value: Parent) {
                header.usernameTextviewDrawer.text = value.username.toUpperCase()
                if (value.profileImageUrl.isNotEmpty()){
                    Glide.with(this@ParentMainActivity).load(value.profileImageUrl).into(header.photoImageviewDrawer)
                }
            }
        })
    }

}


