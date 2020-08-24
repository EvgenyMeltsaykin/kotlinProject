package com.diplom.kotlindiplom

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
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
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.diplom.kotlindiplom.models.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.accept_parent.view.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.header.view.*

class MainActivity : AppCompatActivity(), ActivityCallback {
    private var drawer: DrawerLayout? = null
    private var back_pressed: Long = 0
    private var role = ""
    override fun getRole(): String {
        return intent.getStringExtra("role")
    }

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        role = intent.getStringExtra("role")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (role == "child") {
            settingsChild()
        }
        if (role == "parent"){
            settingsParent()
        }

    }

    fun settingsParent(){
        navView.inflateMenu(R.menu.drawer_menu_parent)
        setupDrawerAndToolbar()
        //Нажатие на аватарку в боковом меню
        val header = navView.getHeaderView(0);
        /*val photo = header.findViewById<CircleImageView>(R.id.photoImageviewDrawer)
        photo.setOnClickListener {
            val navController = Navigation.findNavController(
                this,
                R.id.navFragment
            )
            navController?.navigate(R.id.parentMyProfileFragment)
            drawer = findViewById(R.id.drawerLayout)
            drawer?.closeDrawer(GravityCompat.START)
        }*/
        //Обработка нажатия выхода в боковом меню
        val menu = navView.menu
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
                            this@MainActivity,
                            "Ваш запрос отклонен",
                            Toast.LENGTH_SHORT
                        ).show()
                        firebase.setFieldUserDatabase(firebase.uidUser!!, "acceptAnswer", "-1")
                    }
                    if (p0.value.toString() == "1") {
                        Toast.makeText(
                            this@MainActivity,
                            "Ваш запрос принят",
                            Toast.LENGTH_SHORT
                        ).show()
                        firebase.setFieldUserDatabase(firebase.uidUser!!, "acceptAnswer", "-1")
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
                    MainActivity::class.java,
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
        if (!intent.getStringExtra("taskId").isNullOrBlank()){
            val bundle = bundleOf()
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.navFragment) as NavHostFragment
            val navController = navHostFragment.navController
            bundle.putString("taskId",intent.getStringExtra("taskId"))
            bundle.putString("title",intent.getStringExtra("title"))
            navController.navigate(R.id.action_mainFragment_to_parentTaskContentFragment,bundle)
        }
    }
    fun settingsChild(){
        navView.inflateMenu(R.menu.drawer_menu_child)
        //Нажатие на аватарку в боковом меню
        val header = navView.getHeaderView(0);
        /*val photo = header.findViewById<CircleImageView>(R.id.photoImageviewDrawer)
        photo.setOnClickListener {
            val navController = Navigation.findNavController(
                this,
                R.id.navFragment
            )
            navController?.navigate(R.id.childMyProfileFragment)
            drawer = findViewById(R.id.drawerLayout)
            drawer?.closeDrawer(GravityCompat.START)
        }*/
        setupDrawerAndToolbar()
        //Обработка нажатия выхода в боковом меню
        val menu = navView.menu
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
                var window = PopupWindow(this@MainActivity)
                val view = layoutInflater.inflate(R.layout.accept_parent, null)
                if (p0.key.toString() == "acceptName") {
                    if (p0.value.toString().isNotEmpty()) {
                        Log.d("TAG", "Найден пользователь")
                        firebase.getFieldUserDatabase(
                            firebase.uidUser!!,
                            "acceptName",
                            object : FirebaseCallback<String> {
                                override fun onComplete(parentName: String) {
                                    view.invitationTextView.setText("Пользователь ${parentName} запрашивает привязку аккаунта")
                                }
                            })
                        window.contentView = view
                        window.showAtLocation(getWindow().decorView, Gravity.CENTER, 0, 0)
                        view.rejectButton.setOnClickListener {
                            window.dismiss()
                            firebase.getFieldUserDatabase(
                                firebase.uidUser!!,
                                "acceptUid",
                                object : FirebaseCallback<String> {
                                    override fun onComplete(value: String) {
                                        firebase.setFieldUserDatabase(value, "acceptAnswer", "0")
                                        firebase.clearAcceptRequest()
                                    }
                                })

                        }
                        view.acceptButton.setOnClickListener {
                            window.dismiss()
                            firebase.getFieldUserDatabase(
                                firebase.uidUser!!,
                                "acceptUid",
                                object : FirebaseCallback<String> {
                                    override fun onComplete(parentUid: String) {
                                        Log.d("TAG", "$parentUid")
                                        firebase.setFieldUserDatabase(firebase.uidUser!!, "parentUid", parentUid)
                                        firebase.setFieldUserDatabase(parentUid, "acceptAnswer", "1")
                                        firebase.clearAcceptRequest()
                                    }
                                })

                        }
                    }
                }
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                return
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                return
            }

        })

        firebase.taskRef.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("Not yet implemented")
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val task = firebase.getAllFieldsTask(p0)

                if (task.showNotification == 2 && task.status == -1) {
                    uiFunctions.createNotificationChild(
                        applicationContext,
                        MainActivity::class.java,
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
                        MainActivity::class.java,
                        R.drawable.ic_launcher_background,
                        "Задание принято",
                        "Задание принято",
                        task.title,
                        task
                    )
                }
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val task = firebase.getAllFieldsTask(p0)
                if (task.showNotification == 0 && task.status == -1) {
                    uiFunctions.createNotificationChild(
                        applicationContext,
                        MainActivity::class.java,
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
        if (!intent.getStringExtra("taskId").isNullOrBlank()){
            val bundle = bundleOf()
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.navFragment) as NavHostFragment
            val navController = navHostFragment.navController
            bundle.putString("taskId",intent.getStringExtra("taskId"))
            bundle.putString("title",intent.getStringExtra("title"))
            navController.navigate(R.id.action_mainFragment_to_childTaskContentFragment,bundle)
        }

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = Navigation.findNavController(
            this,
            R.id.navFragment
        )
        when (item.itemId) {
            R.id.menuPointsChild -> navController?.navigate(
                R.id.childMyProfileFragment
            )
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (role == "child") {
            menuInflater.inflate(R.menu.nav_menu, menu)
            val item = menu?.findItem(R.id.menuPointsChild)
            val firebase = FunctionsFirebase()
            firebase.getFieldUserDatabase(firebase.uidUser!!,"point",object : FirebaseCallback<String>{
                override fun onComplete(value: String) {
                    item?.title = value
                }
            })
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun invalidateOptionsMenu() {
        super.invalidateOptionsMenu()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (role == "child"){
            val item = menu?.findItem(R.id.menuPointsChild)
            val firebase = FunctionsFirebase()
            firebase.getFieldUserDatabase(firebase.uidUser!!,"point",object : FirebaseCallback<String>{
                override fun onComplete(value: String) {
                    item?.title = value
                }
            })
        }
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
            R.id.navFragment
        )
        if (navController?.currentDestination?.id == R.id.listSubjectsFragment) {
            navController?.popBackStack()
            title = "Выберите класс"
            return
        }
        if (navController?.currentDestination?.id == R.id.schoolBooksFragment) {
            navController?.popBackStack()
            title = "Выберите предмет"
            return
        }
        if (navController?.currentDestination?.id == R.id.parentAllTasksFragment || navController?.currentDestination?.id == R.id.parentNewTaskFragment) {
            navController?.popBackStack()
            setTitle("Задания")
            return
        }
        if (navController?.currentDestination?.id == R.id.detailsMarksFragment) {
            navController?.popBackStack()
            setTitle("Предметы")
            return
        }
        if (navController?.currentDestination?.id == R.id.weekdayFragment) {
            if (role == "child") {
                navController.navigate(R.id.action_weekdayFragment_to_diaryFragment)
                setTitle("Электронный дневник")
            }
            if (role == "parent"){
                navController.navigate(R.id.action_weekdayFragment_to_chooseChildSheduleFragment)
                setTitle("Выберите ребенка")
            }
            return
        }
        if (navController?.currentDestination?.id == R.id.chooseChildSheduleFragment) {
            navController.navigate(R.id.action_chooseChildSheduleFragment_to_diaryFragment)
            setTitle("Электронный дневник")
            return
        }
        if (navController?.currentDestination?.id == R.id.chooseSemestrElschoolFragment) {
            if (role == "child") {
                navController.navigate(R.id.action_chooseSemestrElschoolFragment_to_diaryFragment)
                setTitle("Электронный дневник")
            }
            if (role == "parent"){
                navController.navigate(R.id.action_chooseSemestrElschoolFragment_to_chooseChildMarkFragment)
                setTitle("Выберите ребенка")
            }
            return
        }
        if (navController?.currentDestination?.id == R.id.lessonsMarkFragment) {
            navController.navigate(R.id.action_lessonsMarkFragment_to_chooseSemestrElschoolFragment)
            setTitle("Выберите семестр")
            return
        }
        if (navController?.currentDestination?.id == R.id.chooseChildMarkFragment) {
            navController.navigate(R.id.action_chooseChildMarkFragment_to_diaryFragment)
            setTitle("Электронный дневник")
            return
        }
        if (navController?.currentDestination?.id == R.id.parentTaskContentFragment) {
            navController?.popBackStack()
            setTitle("")
            return
        }

        if (navController?.currentDestination?.id == R.id.childAllTasksFragment) {
            navController?.popBackStack()
            setTitle("Задания")
            return
        }
        if (navController?.currentDestination?.id == R.id.childTaskContentFragment) {
            navController?.popBackStack()
            setTitle("")
            return
        }
        if (navController?.currentDestination?.id == R.id.sheduleDayFragment) {
            val bundle = bundleOf()
            bundle.putBoolean("updateShedule", false)
            navController?.navigate(R.id.action_sheduleDayFragment_to_weekdayFragment, bundle)
            setTitle("Расписание")
            return
        }
        if (navController?.currentDestination?.id == R.id.homeworkFragment) {
            navController?.navigate(R.id.action_homeworkFragment_to_sheduleDayFragment)
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
        back_pressed = System.currentTimeMillis()

    }

    private fun setupDrawerAndToolbar() {
        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.navFragment) as NavHostFragment? ?: return
        val navController = host.navController
        val sideBar = findViewById<NavigationView>(R.id.navView)
        sideBar?.setupWithNavController(navController)
        val toolBar = findViewById<Toolbar>(R.id.Toolbar)
        setSupportActionBar(toolBar)
        drawer = findViewById(R.id.drawerLayout)
        val toggle = ActionBarDrawerToggle(
            this,
            drawer,
            toolBar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        val header = navView.getHeaderView(0)
        val firebase = FunctionsFirebase()
        //Загрузка фото и имени в боковое меню при запуске приложения
        if (role == "child"){
            firebase.getChild(firebase.uidUser!!, object : FirebaseCallback<Child> {
                override fun onComplete(value: Child) {
                    header.usernameTextviewDrawer.text = value.username
                    /*if (value.profileImageUrl.isNotEmpty()) {
                        Glide.with(this@MainActivity).load(value.profileImageUrl)
                            .into(header.photoImageviewDrawer)
                    }*/
                }
            })
        }
        if(role == "parent"){
            firebase.getParent(firebase.uidUser!!, object : FirebaseCallback<Parent> {
                override fun onComplete(value: Parent) {

                    header.usernameTextviewDrawer.text = value.username
                    /*if (value.profileImageUrl.isNotEmpty()) {
                        Glide.with(this@MainActivity).load(value.profileImageUrl)
                            .into(header.photoImageviewDrawer)
                    }*/
                }
            })
        }
        drawer?.addDrawerListener(toggle)
        toggle.syncState()
    }
}