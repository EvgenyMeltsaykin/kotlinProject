package com.diplom.kotlindiplom

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.diplom.kotlindiplom.MainActivity.FirebaseSingleton.firebase
import com.diplom.kotlindiplom.MainActivity.FunctionUiSingleton.functionsUI
import com.diplom.kotlindiplom.childFragments.RequestParentFragment
import com.diplom.kotlindiplom.models.FunctionsApi
import com.diplom.kotlindiplom.models.FunctionsFirebase
import com.diplom.kotlindiplom.models.FunctionsUI
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.header.view.*

class MainActivity : AppCompatActivity(), ActivityCallback {

    private lateinit var drawer: DrawerLayout
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private lateinit var navigationView: NavigationView
    private lateinit var menuNavigationView: Menu
    private var onlyDiary = false
    lateinit var prefs: SharedPreferences
    private var backPressed: Long = 0
    private var roleUser = ""
    override fun getRoleUser(): String {
        return prefs.getString(functionsUI.APP_PREFERENCES_ROLE, "").toString()
    }

    object FunctionUiSingleton {
        val functionsUI = FunctionsUI()
    }

    object FirebaseSingleton {
        var firebase = FunctionsFirebase()
        fun newInstance() {
            firebase = FunctionsFirebase()
        }
    }

    object Network {
        val network = FunctionsApi()
    }


    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        roleUser = prefs.getString(functionsUI.APP_PREFERENCES_ROLE, "").toString()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (prefs.contains(functionsUI.APP_PREFERENCES_MODE)) {
            onlyDiary =
                prefs.getBoolean(functionsUI.APP_PREFERENCES_MODE, false)
        }
        FirebaseSingleton.newInstance()
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navFragment) as NavHostFragment
        navController = navHostFragment.navController
        drawer = findViewById(R.id.drawerLayout)

        navigationView = findViewById(R.id.navView)
        navigationView.setupWithNavController(navController)
        menuNavigationView = navigationView.menu
        setMessageListener()
        if (roleUser == "child") {
            settingsChild()
        }
        if (roleUser == "parent") {
            settingsParent()
        }
    }

    private fun setMessageListener() {

        firebase.feedbackRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val feedback = firebase.getAllFieldsFeedback(snapshot)
                if (feedback.userUid == firebase.userUid) {
                    if (feedback.messages.isNotEmpty()){
                        if (feedback.messages.last().author != "user" && feedback.messages.last().readStatus == "0") {
                            functionsUI.createNotificationNewMessage(
                                applicationContext,
                                MainActivity::class.java,
                                "Служба поддержки ответила на Ваш вопрос",
                                feedback.topic,
                                feedback
                            )
                        }
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    private fun settingsParent() {
        navView.inflateMenu(R.menu.drawer_menu_parent)
        navController.navigate(R.id.loginDiaryFragment)
        setupDrawerAndToolbar()
        val header = navView.getHeaderView(0)
        val usernameTextViewHeader = header.findViewById<TextView>(R.id.usernameTextviewDrawer)
        usernameTextViewHeader.setOnClickListener {
            navController.navigate(R.id.parentMyProfileFragment)
            drawer.closeDrawer(GravityCompat.START)
        }
        val uiFunctions = FunctionsUI()
        val ref = firebase.userRef.child(firebase.userUid)
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
                    }
                    if (p0.value.toString() == "1") {
                        Toast.makeText(
                            this@MainActivity,
                            "Ваш запрос принят",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    firebase.setFieldUserDatabase(firebase.userUid, "acceptAnswer", "-1")
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
                if (task.parentUid == firebase.userUid) {
                    if (task.showNotification == 1 || task.status != 0) return
                    uiFunctions.createNotificationParent(
                        applicationContext,
                        MainActivity::class.java,
                        "Ребенок выполнил задание",
                        task.title,
                        task
                    )
                }
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                return
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                return
            }
        })

        firebase.awardsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                return
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val award = firebase.getAllFieldAward(snapshot)
                if (award.parentUid == firebase.userUid) {
                    if (award.showNotification != 1) return
                    uiFunctions.createNotificationChildTakeAward(
                        applicationContext,
                        MainActivity::class.java,
                        "Ребенок выбрал вознаграждение",
                        award.name,
                        award
                    )
                    firebase.setFieldAward(award.awardId, "showNotification", 0)
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                return
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })

        if (!intent.getStringExtra("feedbackId").isNullOrBlank()) {
            val bundle = bundleOf()
            bundle.putString("feedbackId", intent.getStringExtra("feedbackId"))
            bundle.putString("topic", intent.getStringExtra("topic"))
            navController.navigate(
                R.id.action_loginDiaryFragment_to_feedbackDetailsFragment,
                bundle
            )
        }
        if (!intent.getStringExtra("taskId").isNullOrBlank()) {
            val bundle = bundleOf()
            bundle.putString("taskId", intent.getStringExtra("taskId"))
            bundle.putString("title", intent.getStringExtra("title"))
            navController.navigate(
                R.id.action_loginDiaryFragment_to_parentTaskContentFragment,
                bundle
            )
        }
        if (!intent.getStringExtra("awardId").isNullOrBlank()) {
            val bundle = bundleOf()
            bundle.putString("awardId", intent.getStringExtra("awardId"))
            bundle.putString("nameAward", intent.getStringExtra("nameAward"))
            bundle.putString("costAward", intent.getStringExtra("costAward"))
            bundle.putString("status", intent.getStringExtra("status"))
            navController.navigate(R.id.action_loginDiaryFragment_to_detailAwardFragment, bundle)
        }
    }

    private fun settingsChild() {
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
        val uiFunctions = FunctionsUI()
        val requestRef = firebase.userRef.child(firebase.userUid)
        requestRef.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                if (p0.key.toString() == "acceptName") {
                    if (p0.value.toString().isNotEmpty()) {
                        val requestParentFragment = RequestParentFragment()
                        val bundle = bundleOf()
                        bundle.putString("parentName", p0.value.toString())
                        requestParentFragment.arguments = bundle
                        requestParentFragment.show(supportFragmentManager, "requestParentFragment")
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

                if (task.childUid == firebase.userUid) {
                    if (task.showNotification == 2 && task.status == -1) {
                        uiFunctions.createNotificationChild(
                            applicationContext,
                            MainActivity::class.java,
                            "Задание не принято",
                            task.title,
                            task
                        )
                        firebase.setFieldDatabaseTask(task.taskId, "childUid", "")
                    }
                    if (task.showNotification == 0 && task.status == 1) {
                        uiFunctions.createNotificationChild(
                            applicationContext,
                            MainActivity::class.java,
                            "Задание принято",
                            task.title,
                            task
                        )
                    }
                }

            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val task = firebase.getAllFieldsTask(p0)
                firebase.getFieldUserDatabase(
                    firebase.userUid,
                    "parentUid",
                    object : Callback<String> {
                        override fun onComplete(value: String) {
                            if (task.parentUid == value) {
                                if (task.showNotification == 0 && task.status == -1) {
                                    uiFunctions.createNotificationChild(
                                        applicationContext,
                                        MainActivity::class.java,
                                        "Новое задание",
                                        task.title,
                                        task
                                    )
                                }
                            }
                        }
                    })
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
        if (!intent.getStringExtra("feedbackId").isNullOrBlank() ) {
            val bundle = bundleOf()
            bundle.putString("feedbackId", intent.getStringExtra("feedbackId"))
            bundle.putString("topic", intent.getStringExtra("topic"))
            navController.navigate(
                R.id.action_mainFragment_to_feedbackDetailsFragment,
                bundle
            )
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuPointsChild -> navController.navigate(
                R.id.childMyProfileFragment
            )
            R.id.updateInformation -> {
                if (navController.currentDestination?.id == R.id.listAwardsFragment) {
                    navController.navigate(R.id.listAwardsFragment)
                }
                if (navController.currentDestination?.id == R.id.parentNodeChildrenFragment) {
                    navController.navigate(R.id.parentNodeChildrenFragment)
                }
            }
            R.id.editScheduleDay -> {
                navController.navigate(R.id.action_myScheduleDayFragment_to_editScheduleFragment)
            }
            /*R.id.listFeedback->{
                navController.navigate(R.id.action_mailFragment_to_listFeedbackFragment)
            }*/
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        if (roleUser == "child") {
            val item = menu?.findItem(R.id.menuPointsChild)
            firebase.getPointChild(
                firebase.userUid,
                object : Callback<String> {
                    override fun onComplete(value: String) {
                        item?.title = value
                    }
                })
        } else {
            val item = menu?.findItem(R.id.menuPointsChild)
            item?.isVisible = false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (roleUser == "child") {
            val item = menu?.findItem(R.id.menuPointsChild)
            if (prefs.contains(FunctionUiSingleton.functionsUI.APP_PREFERENCES_MODE)) {
                val onlyDiary =
                    prefs.getBoolean(FunctionUiSingleton.functionsUI.APP_PREFERENCES_MODE, false)
                item?.isVisible = !onlyDiary
            }
            if (item?.isVisible!!) {
                val firebase = FunctionsFirebase()
                firebase.getPointChild(
                    firebase.userUid,
                    object : Callback<String> {
                        override fun onComplete(value: String) {
                            item.title = value
                        }
                    })
            }

        }
        if (navController.currentDestination?.id == R.id.listAwardsFragment) {
            val item = menu?.findItem(R.id.updateInformation)
            item?.isVisible = true
        }
        if (navController.currentDestination?.id == R.id.parentNodeChildrenFragment) {
            val item = menu?.findItem(R.id.updateInformation)
            item?.isVisible = true
        }
        if (navController.currentDestination?.id == R.id.myScheduleDayFragment) {
            val item = menu?.findItem(R.id.editScheduleDay)
            item?.isVisible = true
        }

        /*if (navController.currentDestination?.id == R.id.mailFragment){
            val item = menu?.findItem(R.id.listFeedback)
            item?.isVisible = true
        }*/
        return super.onPrepareOptionsMenu(menu)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        //Отключение клавиатуры
        /*if (currentFocus != null) {
            val imn = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imn.hideSoftInputFromWindow(window.decorView.windowToken, 0)
        }*/
        return super.dispatchTouchEvent(ev)
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
            return
        }
        if (navController.currentDestination?.id == R.id.scheduleDayFragment) {
            val bundle = bundleOf()
            bundle.putBoolean("updateSchedule", false)
            navController.navigate(R.id.action_scheduleDayFragment_to_weekdayFragment, bundle)
            return
        }
        when (navController.currentDestination?.id) {
            R.id.parentMyProfileFragment -> {
                navController.navigate(R.id.action_parentMyProfileFragment_to_loginDiaryFragment)
                return
            }
            R.id.childMyProfileFragment -> {
                navController.navigate(R.id.action_childMyProfileFragment_to_mainFragment)
                return
            }
            R.id.childTasksFragment -> {
                navController.navigate(R.id.action_childTasksFragment_to_mainFragment)
                return
            }
            R.id.listAwardsFragment -> {
                moveFragment(
                    R.id.action_listAwardsFragment_to_mainFragment,
                    R.id.action_listAwardsFragment_to_loginDiaryFragment
                )
                return
            }
            R.id.diaryFragment -> {
                if (roleUser == "child") {
                    navController.navigate(R.id.action_diaryFragment_to_mainFragment)
                    return
                }
            }
            R.id.loginDiaryFragment -> {
                if (roleUser == "child") {
                    navController.navigate(R.id.action_loginDiaryFragment_to_mainFragment)
                    return
                }
            }
            R.id.parentTasksFragment -> {
                navController.navigate(R.id.action_parentTasksFragment_to_loginDiaryFragment)
                return
            }
            R.id.parentNodeChildrenFragment -> {
                navController.navigate(R.id.action_parentNodeChildrenFragment_to_loginDiaryFragment)
                return
            }
            R.id.settingsFragment -> {
                moveFragment(
                    R.id.action_settingsFragment_to_mainFragment,
                    R.id.action_settingsFragment_to_loginDiaryFragment
                )
                return
            }
            /*R.id.listFeedbackFragment->{
                moveFragment(R.id.action_listFeedbackFragment_to_mainFragment,R.id.action_listFeedbackFragment_to_loginDiaryFragment)
                return
            }
            R.id.mailFragment -> {
                moveFragment(
                    R.id.action_mailFragment_to_mainFragment,
                    R.id.action_mailFragment_to_loginDiaryFragment
                )
                return
            }*/
        }
        if (roleUser == "child") {
            if (navController.currentDestination?.id == R.id.mainFragment) {
                if (backPressed + 2000 > System.currentTimeMillis()) {
                    finishAffinity()
                    //super.onBackPressed()
                } else {
                    Toast.makeText(this, "Для выхода нажмите \"Назад\" ещё раз", Toast.LENGTH_SHORT)
                        .show()
                    backPressed = System.currentTimeMillis()
                    return
                }
            } else {
                navController.popBackStack()
                return
            }
        }
        if (roleUser == "parent") {
            if (navController.currentDestination?.id == R.id.diaryFragment || navController.currentDestination?.id == R.id.loginDiaryFragment) {
                if (backPressed + 2000 > System.currentTimeMillis()) {
                    finishAffinity()
                    //super.onBackPressed()
                } else {
                    Toast.makeText(this, "Для выхода нажмите \"Назад\" ещё раз", Toast.LENGTH_SHORT)
                        .show()
                    backPressed = System.currentTimeMillis()
                    return
                }
            } else {
                navController.popBackStack()
                return
            }
        }


    }

    private fun moveFragment(childActionId: Int, parentActionId: Int) {
        if (roleUser == "child") {
            navController.navigate(childActionId)
        }
        if (roleUser == "parent") {
            navController.navigate(parentActionId)
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
            firebase.userUid,
            "username",
            object : Callback<String> {
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
            firebase.removeAllListener()
            return@setOnMenuItemClickListener true
        }
        //Настройка отображение параметров
        FunctionUiSingleton.functionsUI.changeMode(this)

        drawer.addDrawerListener(toggle)
        toggle.syncState()
        /*if (role == "child"){
            firebase.getChild(firebase.uidUser, object : FirebaseCallback<Child> {
                override fun onComplete(value: Child) {

                    /*if (value.profileImageUrl.isNotEmpty()) {
                        Glide.with(this@MainActivity).load(value.profileImageUrl)
                            .into(header.photoImageviewDrawer)
                    }*/
                }
            })
        }
        if(role == "parent"){
            firebase.getParent(firebase.uidUser, object : FirebaseCallback<Parent> {
                override fun onComplete(value: Parent) {

                    header.usernameTextviewDrawer.text = value.username
                    /*if (value.profileImageUrl.isNotEmpty()) {
                        Glide.with(this@MainActivity).load(value.profileImageUrl)
                            .into(header.photoImageviewDrawer)
                    }*/
                }
            })
        }*/
    }

}

