package com.example.justdoit.activity

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.justdoit.R
import com.example.justdoit.data.TaskDatabase
import com.example.justdoit.data.daos.OfflineTaskDAO
import com.example.justdoit.data.daos.TaskDAO
import com.example.justdoit.data.entitie.Task
import com.example.justdoit.databinding.ActivityMainBinding
import com.example.justdoit.signUp.LoginActivity
import com.example.justdoit.adapter.HomeAdapter
import com.example.justdoit.repository.HomeRepo
import com.example.justdoit.viewmodelfactory.HomeViewHolderFactory
import com.example.justdoit.viewmodel.HomeViewModel
import com.example.justdoit.util.toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val adapter = HomeAdapter()
    private lateinit var database : TaskDatabase
    private lateinit var taskDAO : TaskDAO
    private lateinit var offlineTaskDAO : OfflineTaskDAO
    private lateinit var repository : HomeRepo
    private lateinit var viewModel : HomeViewModel

    private lateinit var editor : Editor
    private lateinit var switchItem : MenuItem
    private lateinit var logoutItem : MenuItem
    private lateinit var navView : NavigationView
    private lateinit var logoutBT : MaterialButton
    private lateinit var themeSwitch : MaterialSwitch
    private lateinit var sharedPref : SharedPreferences

    lateinit var rvTask: RecyclerView
    lateinit var updateButton: Button
    lateinit var updateDateEt: EditText
    lateinit var updateTitleEt: EditText
    lateinit var updateTaskView: CardView
    lateinit var cancelUpdateButton: Button
    lateinit var calenderView: CalendarView
    lateinit var updateDescriptionEt: EditText
    lateinit var dateUpdateButton: ImageButton

    lateinit var cancelTaskDeleteButton: Button
    lateinit var taskDeleteConfirmButton: Button
    lateinit var taskDeleteConfirmationCard: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rvTask = binding.rvTask
        navView = binding.navView
        calenderView = binding.calender
        cancelTaskDeleteButton = binding.cancelTaskDeleteButton
        taskDeleteConfirmButton = binding.taskDeleteConfirmButton
        taskDeleteConfirmationCard = binding.taskDeleteConfirmationCard

        updateDateEt = binding.updateDateEt
        updateButton = binding.updateButton
        updateTitleEt = binding.updateTitleEt
        updateTaskView = binding.updateTaskView
        dateUpdateButton = binding.updateCalenderButton
        cancelUpdateButton = binding.cancelUpdateButton
        updateDescriptionEt = binding.updateDescriptionEt

        switchItem  = navView.menu.findItem(R.id.menu_switch)
        themeSwitch = switchItem.actionView as MaterialSwitch
        logoutItem = navView.menu.findItem(R.id.menu_logout)
        logoutBT = logoutItem.actionView as MaterialButton
        logoutBT.text = getString(R.string.logout_text)
        logoutBT.background = ContextCompat.getDrawable(this, R.drawable.bt_box)

        sharedPref = getSharedPreferences("theme_pref",MODE_PRIVATE)
        editor = sharedPref.edit()
        themeSwitch.isChecked = sharedPref.getBoolean("theme_switch_is_checked",true)

        rvTask.layoutManager = LinearLayoutManager(this@HomeActivity)
        rvTask.adapter = adapter

        database = TaskDatabase.getDatabase(applicationContext)
        taskDAO = database.taskDao()
        offlineTaskDAO = database.offlineTaskDao()
        repository = HomeRepo(taskDAO, offlineTaskDAO)
        viewModel  = ViewModelProvider(this, HomeViewHolderFactory(repository))[HomeViewModel::class.java]

        loggedInOrNot()
        loadTask()
        loadTheme(switchItem,editor)
        viewModel.syncTask(this)
        logoutBT.setOnClickListener { binding.drawer.close();logout() }
        themeSwitch.setOnClickListener { changeTheme(themeSwitch.isChecked,switchItem,editor) }

        binding.apply {
            calender.visibility = View.GONE
            addButton.setOnClickListener { addTask(addDateEt,addTaskView,rvTask) }
            saveButton.setOnClickListener { saveTask(addTitleEt,addDescriptionEt,addDateEt,addTaskView,rvTask) }
            openMenu.setOnClickListener { drawer.open() }
            cancelTaskButton.setOnClickListener { cancelAddingTask(addTitleEt,addDescriptionEt,addDateEt,addTaskView,rvTask) }
            calenderButton.setOnClickListener { addTaskView.visibility = View.GONE;calenderView.visibility = View.VISIBLE }
            calender.setOnDateChangeListener { _, year, month, dayOfMonth -> onDateSelected(year, month, dayOfMonth,addDateEt,calender,addTaskView) }
        }
    }

    private fun changeTheme(isChecked: Boolean, switch: MenuItem, editor: Editor) {
        editor.putBoolean("theme_switch_is_checked", isChecked)
        loadTheme(switch, editor)
        editor.apply()
    }

    private fun loadTheme(switch: MenuItem, editor: Editor) {
        val isChecked = themeSwitch.isChecked
        if (isChecked) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            switch.setIcon(R.drawable.dark_mode_icon)
            switch.title = "Dark Mode"
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            switch.setIcon(R.drawable.light_mode_icon)
            switch.title = "Light Mode"
        }
        editor.apply() // Apply changes only once after loading theme
    }

    private fun onDateSelected(year: Int, month: Int, dayOfMonth: Int,dateET: EditText,calendarView: CalendarView,cardView: CardView) {
        val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
        val formattedDate = DateTimeFormatter.ofPattern("dd-MMM-yyyy").format(selectedDate)
        dateET.setText(formattedDate)
        calendarView.visibility = View.GONE
        cardView.visibility = View.VISIBLE
    }

    private fun loggedInOrNot() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
            finish()
        }
    }

    private fun logout(){
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
        finish()
    }

    private fun loadTask() {
        viewModel.getTask().observe(this) { tasks ->
            Log.d("Observer", "Observer triggered. Tasks: $tasks")
            tasks?.let {
                // Submit tasks to the adapter
                adapter.submitList(it)
            }
        }
    }

    private fun addTask(editText: EditText,cardView: CardView,recyclerView: RecyclerView) {
        val today = LocalDate.now()
        val formattedToday = DateTimeFormatter.ofPattern("dd-MMM-yyyy").format(today)
        editText.setText(formattedToday)
        if (cardView.visibility == View.VISIBLE) {
            cardView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.GONE
            cardView.visibility = View.VISIBLE
        }
    }

    private fun saveTask(title: EditText,description: EditText,date: EditText,cardView: CardView,recyclerView: RecyclerView) {
        if (title.text.isNullOrEmpty()) {
            toast("Please fill task name.")
        } else if (description.text.isNullOrEmpty()) {
            toast("Please fill the description.")
        } else if (date.text.isNullOrEmpty()) {
            toast("Please fill the due date.")
        } else {
            val task = Task(
                0,
                title.text.toString().replaceFirstChar { it.uppercase() },
                description.text.toString().replaceFirstChar { it.uppercase() },
                date.text.toString()
            )
            viewModel.insertTask(task, this)
            title.setText("")
            description.setText("")
            date.setText("")
            cardView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun cancelAddingTask(title: EditText,description: EditText,date: EditText,cardView: CardView,recyclerView: RecyclerView) {
        title.setText("")
        description.setText("")
        date.setText("")
        cardView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

}