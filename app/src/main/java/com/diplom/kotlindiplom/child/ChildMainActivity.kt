package com.diplom.kotlindiplom.child

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
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
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.FunctionsUI
import com.diplom.kotlindiplom.models.Task
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.accept_parent.view.*
import kotlinx.android.synthetic.main.activity_child_main.*
import kotlinx.android.synthetic.main.header.*

class ChildMainActivity : AppCompatActivity() {
    private var drawer: DrawerLayout? = null
    private var back_pressed: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_main)
        verifyUserIsLoggedIn()
        setupDrawerAndToolbar()
        //Нажатие на аватарку в боковом меню
        val header = navViewChild.getHeaderView(0);
        val photo = header.findViewById<CircleImageView>(R.id.photoImageviewDrawer)
        photo.setOnClickListener {
            val navController = Navigation.findNavController(
                this,
                R.id.navFragmentChild
            )
            navController.navigate(R.id.childMyProfileFragment)
            drawer = findViewById(R.id.drawerLayoutChild)
            drawer?.closeDrawer(GravityCompat.START)
        }
        //Обработка нажатия выхода в боковом меню
        val menu = navViewChild.menu
        val quit = menu.findItem(R.id.childExitApplication)
        quit.setOnMenuItemClickListener {
            FirebaseAuth.getInstance().signOut()
            intent = Intent(this, ChooseActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            return@setOnMenuItemClickListener true
        }

        val firebase = FunctionsFirebase()
        val uiFunctions = FunctionsUI()
        val requestRef = firebase.childRef.child(firebase.uidUser!!)
        requestRef.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                var window = PopupWindow(this@ChildMainActivity)
                val view = layoutInflater.inflate(R.layout.accept_parent, null)
                val uid = FirebaseAuth.getInstance().uid
                if (p0.key.toString() == "acceptName") {
                    if (p0.value.toString().isNotEmpty()) {
                        Log.d("TAG", "Найден пользователь")
                        firebase.getFieldDatabaseChild(
                            uid!!,
                            "acceptName",
                            object : FirebaseCallback {
                                override fun onCallBackString(value: String) {
                                    view.invitationTextView.setText("Родитель ${value} запрашивает привязку аккаунта")
                                }

                                override fun onCallBackTasks(value: List<Task>) {
                                    TODO("Not yet implemented")
                                }

                                override fun onCallBackTask(value: Task) {
                                    TODO("Not yet implemented")
                                }
                            })
                        window.contentView = view
                        window.showAtLocation(getWindow().decorView, Gravity.CENTER, 0, 0)
                        view.rejectButton.setOnClickListener {
                            window.dismiss()
                            firebase.getFieldDatabaseChild(
                                uid,
                                "acceptUid",
                                object : FirebaseCallback {
                                    override fun onCallBackString(value: String) {
                                        firebase.setFieldDatabaseParent(value, "acceptAnswer", "0")
                                    }

                                    override fun onCallBackTasks(value: List<Task>) {
                                        TODO("Not yet implemented")
                                    }

                                    override fun onCallBackTask(value: Task) {
                                        TODO("Not yet implemented")
                                    }

                                })
                            firebase.clearAcceptRequest()
                        }
                        view.acceptButton.setOnClickListener {
                            window.dismiss()
                            firebase.getFieldDatabaseChild(
                                uid,
                                "acceptUid",
                                object : FirebaseCallback {
                                    override fun onCallBackString(value: String) {
                                        Log.d("TAG", "$value")
                                        firebase.setFieldDatabaseChild(uid, "parentUid", value)
                                        firebase.setFieldDatabaseParent(value, "acceptAnswer", "1")
                                    }

                                    override fun onCallBackTasks(value: List<Task>) {
                                        TODO("Not yet implemented")
                                    }

                                    override fun onCallBackTask(value: Task) {
                                        TODO("Not yet implemented")
                                    }

                                })
                            firebase.clearAcceptRequest()
                        }
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
                val task = firebase.getTask(p0)

                if (task.showNotification == 2 && task.status == -1) {
                    uiFunctions.createNotificationChild(
                        applicationContext,
                        ChildMainActivity::class.java,
                        R.drawable.ic_launcher_background,
                        "Задание не принято",
                        "Задание не принято",
                        task.title,
                        task
                    )
                }
                if (task.showNotification == 0 && task.status == 1) {
                    uiFunctions.createNotificationChild(
                        applicationContext,
                        ChildMainActivity::class.java,
                        R.drawable.ic_launcher_background,
                        "Задание принято",
                        "Задание принято",
                        task.title,
                        task
                    )
                }
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val task = firebase.getTask(p0)
                if (task.showNotification == 0 && task.status == -1) {
                    uiFunctions.createNotificationChild(
                        applicationContext,
                        ChildMainActivity::class.java,
                        R.drawable.ic_launcher_background,
                        "Новое задание",
                        "Новое задание",
                        task.title,
                        task
                    )
                }
            }
            override fun onChildRemoved(p0: DataSnapshot) {
                return
            }

        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = Navigation.findNavController(
            this,
            R.id.navFragmentChild
        )
        when (item.itemId) {
            R.id.menuPointsChild -> navController.navigate(
                R.id.childMyProfileFragment
            )
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        val item = menu?.findItem(R.id.menuPointsChild)
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/children/$uid")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    if (it.key.toString() == "point") {
                        val points = it.value.toString()
                        item?.setTitle(points)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun invalidateOptionsMenu() {
        super.invalidateOptionsMenu()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val item = menu?.findItem(R.id.menuPointsChild)
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/children/$uid")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    if (it.key.toString() == "point") {
                        val points = it.value.toString()
                        item?.setTitle(points)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
        return super.onPrepareOptionsMenu(menu)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        //Отключение клавиатуры
        if (currentFocus != null) {
            val imn = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imn.hideSoftInputFromWindow(window.decorView.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onBackPressed() {
        val navController = Navigation.findNavController(
            this,
            R.id.navFragmentChild
        )
        if (navController.currentDestination?.id == R.id.childAllTasksFragment) {
            navController.popBackStack()
            setTitle("Задания")
            return
        }
        if (navController.currentDestination?.id == R.id.childTaskContentFragment) {
            navController.popBackStack()
            setTitle("")
            return
        }
        if (drawer?.isDrawerOpen(GravityCompat.START) == true) {
            drawer?.closeDrawer(GravityCompat.START)
        } else if (back_pressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
            finishAffinity()
        } else {
            Toast.makeText(this, "Для выхода нажмите \"Назад\" ещё раз", Toast.LENGTH_SHORT)
                .show()
        }

        back_pressed = System.currentTimeMillis();

    }

    private fun setupDrawerAndToolbar() {
        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.navFragmentChild) as NavHostFragment? ?: return
        val navController = host.navController
        val sideBar = findViewById<NavigationView>(R.id.navViewChild)
        sideBar?.setupWithNavController(navController)
        val toolBar = findViewById<Toolbar>(R.id.childToolbar)
        setSupportActionBar(toolBar)
        drawer = findViewById(R.id.drawerLayoutChild)
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
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/children/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    if (it.key.toString() == "profileImageUrl") {
                        val profileImageUrl = it.value.toString()
                        if (profileImageUrl != "") {
                            try {
                                Glide.with(this@ChildMainActivity).load(profileImageUrl)
                                    .into(photoImageviewDrawer)
                            } catch (e: Exception) {
                                return
                            }
                        }
                    }
                    if (it.key.toString() == "username") {
                        usernameTextviewDrawer.text = it.value.toString().toUpperCase()
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }

    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, ChooseActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

}

