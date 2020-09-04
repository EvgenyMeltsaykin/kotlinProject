package com.diplom.kotlindiplom

import android.app.Activity
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
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.diplom.kotlindiplom.childFragments.RequestParentFragment
import com.diplom.kotlindiplom.childFragments.mySchedule.AddLessonFragment
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.FunctionsUI
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.accept_parent.view.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.header.view.*

class MainActivity : AppCompatActivity(), ActivityCallback {

    private lateinit var drawer: DrawerLayout
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private lateinit var navigationView: NavigationView
    private lateinit var menuNavigationView: Menu
    private lateinit var optionsMenu: MenuItem
    private var back_pressed: Long = 0
    private var roleUser = ""
    override fun getRoleUser(): String? {
        return intent.getStringExtra("role").toString()
    }

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {

        roleUser = intent.getStringExtra("role").toString()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navFragment) as NavHostFragment
        navController = navHostFragment.navController
        drawer = findViewById(R.id.drawerLayout)

        navigationView = findViewById(R.id.navView)
        navigationView.setupWithNavController(navController)
        menuNavigationView = navigationView.menu
        if (roleUser == "child") {
            settingsChild()
        }
        if (roleUser == "parent") {
            settingsParent()
        }
    }

    fun settingsParent() {
        navView.inflateMenu(R.menu.drawer_menu_parent)
        setupDrawerAndToolbar()
        val header = navView.getHeaderView(0)
        val usernameTextViewHeader = header.findViewById<TextView>(R.id.usernameTextviewDrawer)
        usernameTextViewHeader.setOnClickListener {
            navController.navigate(R.id.parentMyProfileFragment)
            drawer.closeDrawer(GravityCompat.START)
        }
        if (!intent.getStringExtra("taskId").isNullOrBlank()) {
            val bundle = bundleOf()
            bundle.putString("taskId", intent.getStringExtra("taskId"))
            bundle.putString("title", intent.getStringExtra("title"))
            navController.navigate(R.id.action_mainFragment_to_parentTaskContentFragment, bundle)
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
                        firebase.setFieldUserDatabase(firebase.uidUser, "acceptAnswer", "-1")
                    }
                    if (p0.value.toString() == "1") {
                        Toast.makeText(
                            this@MainActivity,
                            "Ваш запрос принят",
                            Toast.LENGTH_SHORT
                        ).show()
                        firebase.setFieldUserDatabase(firebase.uidUser, "acceptAnswer", "-1")
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
    }

    fun settingsChild() {
        navView.inflateMenu(R.menu.drawer_menu_child)
        //Нажатие на аватарку в боковом меню
        val header = navView.getHeaderView(0)
        val usernameTextViewHeader = header.findViewById<TextView>(R.id.usernameTextviewDrawer)
        usernameTextViewHeader.setOnClickListener {
            navController.navigate(R.id.childMyProfileFragment)
            drawer.closeDrawer(GravityCompat.START)
        }
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
                val requestParentFragment = RequestParentFragment()
                val bundle =  bundleOf()
                if (p0.key.toString() == "acceptName") {
                    if (p0.value.toString().isNotEmpty()) {
                        Log.d("TAG", "Найден пользователь")
                        firebase.getFieldUserDatabase(
                            firebase.uidUser!!,
                            "acceptName",
                            object : FirebaseCallback<String> {
                                override fun onComplete(parentName: String) {
                                    bundle.putString("parentName",parentName)
                                    requestParentFragment.arguments = bundle
                                    requestParentFragment.show(supportFragmentManager,"requestParentFragment")
                                }
                            })
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
        if (!intent.getStringExtra("taskId").isNullOrBlank()) {
            val bundle = bundleOf()
            bundle.putString("taskId", intent.getStringExtra("taskId"))
            bundle.putString("title", intent.getStringExtra("title"))
            navController.navigate(R.id.action_mainFragment_to_childTaskContentFragment, bundle)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuPointsChild -> navController.navigate(
                R.id.childMyProfileFragment
            )
            R.id.updateInformation -> {
                if (navController.currentDestination?.id  == R.id.listAwardsFragment){
                    navController.navigate(R.id.listAwardsFragment)
                }
            }
            R.id.editScheduleDay->{
                navController.navigate(R.id.action_myScheduleDayFragment_to_editScheduleFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        if (roleUser == "child") {
            val item = menu?.findItem(R.id.menuPointsChild)
            val firebase = FunctionsFirebase()
            firebase.getPointChild(
                firebase.uidUser!!,
                object : FirebaseCallback<String> {
                    override fun onComplete(value: String) {
                        item?.title = value
                    }
                })
        }else{
            val item = menu?.findItem(R.id.menuPointsChild)
            item?.isVisible = false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun invalidateOptionsMenu() {
        super.invalidateOptionsMenu()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (roleUser == "child") {
            val item = menu?.findItem(R.id.menuPointsChild)
            val firebase = FunctionsFirebase()
            firebase.getPointChild(
                firebase.uidUser!!,
                object : FirebaseCallback<String> {
                    override fun onComplete(value: String) {
                        item?.title = value
                    }
                })
        }
        if (navController.currentDestination?.id == R.id.listAwardsFragment){
            val item = menu?.findItem(R.id.updateInformation)
            item?.isVisible = true
        }
        if (navController.currentDestination?.id == R.id.myScheduleDayFragment){
            val item = menu?.findItem(R.id.editScheduleDay)
            item?.isVisible = true
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
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
            return
        }
        if (navController.currentDestination?.id == R.id.weekdayFragment){
            if (roleUser == "child"){
                while(navController.backStack.size >= 5){
                    navController.popBackStack()
                }
            }
            if(roleUser == "parent"){
                while(navController.backStack.size >= 6){
                    navController.popBackStack()
                }
            }
            return
        }
        if (navController.currentDestination?.id == R.id.scheduleDayFragment){
            val bundle = bundleOf()
            bundle.putBoolean("updateSchedule",false)

            navController.navigate(R.id.action_scheduleDayFragment_to_weekdayFragment,bundle)
            return
        }
        if (navController.currentDestination?.id == R.id.parentMyProfileFragment || navController.currentDestination?.id == R.id.childMyProfileFragment || navController.currentDestination?.id == R.id.childTasksFragment
            || navController.currentDestination?.id == R.id.listAwardsFragment || navController.currentDestination?.id == R.id.diaryFragment || navController.currentDestination?.id == R.id.loginDiaryFragment
            || navController.currentDestination?.id == R.id.mailFragment || navController.currentDestination?.id == R.id.parentTasksFragment || navController.currentDestination?.id == R.id.parentNodeChildrenFragment){
            navController.navigate(R.id.mainFragment)
            return
        }
        if (navController.currentDestination?.id == R.id.mainFragment){
            if (back_pressed + 2000 > System.currentTimeMillis()) {
                finishAffinity()
                //super.onBackPressed()
            } else {
                Toast.makeText(this, "Для выхода нажмите \"Назад\" ещё раз", Toast.LENGTH_SHORT).show()
                back_pressed = System.currentTimeMillis()
                return
            }
        }else{
            navController.popBackStack()
            return
        }

    }

    private fun setupDrawerAndToolbar() {
        val myToolBar = findViewById<Toolbar>(R.id.myToolbar)
        setSupportActionBar(myToolBar)
        val toggle = ActionBarDrawerToggle(
            this,
            drawer,
            myToolBar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        val header = navView.getHeaderView(0)
        val firebase = FunctionsFirebase()
        //Загрузка фото и имени в боковое меню при запуске приложения
        firebase.getFieldUserDatabase(
            firebase.uidUser!!,
            "username",
            object : FirebaseCallback<String> {
                override fun onComplete(value: String) {
                    header.usernameTextviewDrawer.text = value
                }
            })
        //Обработка нажатия выхода в боковом меню
        val quit = menuNavigationView.findItem(R.id.exitApplication)
        quit?.setOnMenuItemClickListener {
            FirebaseAuth.getInstance().signOut()
            intent = Intent(this, ChooseActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            return@setOnMenuItemClickListener true
        }
        /*if (role == "child"){
            firebase.getChild(firebase.uidUser!!, object : FirebaseCallback<Child> {
                override fun onComplete(value: Child) {

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
        }*/
        drawer.addDrawerListener(toggle)
        toggle.syncState()
    }

}

