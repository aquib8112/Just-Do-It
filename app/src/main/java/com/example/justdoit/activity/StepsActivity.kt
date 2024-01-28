package com.example.justdoit.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.justdoit.data.TaskDatabase
import com.example.justdoit.data.daos.OfflineStepDAO
import com.example.justdoit.data.daos.StepDAO
import com.example.justdoit.data.entitie.Step
import com.example.justdoit.databinding.ActivityStepsBinding
import com.example.justdoit.adapter.StepsAdapter
import com.example.justdoit.repository.StepsRepo
import com.example.justdoit.viewmodelfactory.StepsViewHolderFactory
import com.example.justdoit.viewmodel.StepsViewModel
import com.example.justdoit.util.toast

class StepsActivity : AppCompatActivity() {

    private var id: Long = 0
    private lateinit var title: String
    private lateinit var description: String
    private lateinit var binding: ActivityStepsBinding

    private val adapter = StepsAdapter()
    private lateinit var database: TaskDatabase
    private lateinit var stepDao: StepDAO
    private lateinit var offlineStepDao: OfflineStepDAO
    private lateinit var repository: StepsRepo
    private lateinit var stepsViewModel: StepsViewModel

    lateinit var stepRV: RecyclerView
    lateinit var updateTitle: EditText
    lateinit var updateStepButton: Button
    lateinit var updateStepCard: CardView
    lateinit var cancelUpdateButton: Button

    lateinit var stepDeleteConfirmationCard: CardView
    lateinit var stepDeleteConfirmButton: Button
    lateinit var cancelStepDeleteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStepsBinding.inflate(layoutInflater)
        setContentView(binding.editStepActivity)
        stepRV = binding.stepsRv
        updateTitle = binding.updateStepTitle
        updateStepCard = binding.updateStepCard
        updateStepButton = binding.updateStepButton
        cancelUpdateButton = binding.cancelUpdateButton
        cancelStepDeleteButton = binding.cancelStepDeleteButton
        stepDeleteConfirmButton = binding.stepDeleteConfirmButton
        stepDeleteConfirmationCard = binding.stepDeleteConfirmationCard

        stepRV.layoutManager = LinearLayoutManager(this)
        stepRV.adapter = adapter

        id = intent.getLongExtra("id", 0)
        title = intent.getStringExtra("title")!!
        description = intent.getStringExtra("description")!!

        database = TaskDatabase.getDatabase(applicationContext)
        stepDao = database.stepDao()
        offlineStepDao = database.offlineStepDao()
        repository = StepsRepo(stepDao, offlineStepDao)
        stepsViewModel =
            ViewModelProvider(this, StepsViewHolderFactory(repository))[StepsViewModel::class.java]

        loadSteps()
        stepsViewModel.syncStep(this)
        binding.apply {
            toolbarTv.text = title.uppercase()
            descriptionTv.text = description
            backButton.setOnClickListener { goToHomePage() }
            addStepsButton.setOnClickListener { addStep(addStepCard, stepsRv) }
            saveStepButton.setOnClickListener { saveStep(stepTitle, addStepCard, stepsRv) }
            cancelStepButton.setOnClickListener { cancelStep(stepTitle, addStepCard, stepsRv) }
        }

    }

    private fun addStep(cardView: CardView, recyclerView: RecyclerView) {
        if (cardView.isVisible) {
            cardView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        } else {
            cardView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }
    }

    private fun saveStep(title: EditText, cardView: CardView, recyclerView: RecyclerView) {
        if (title.text.isNullOrEmpty()) {
            toast("Please fill step name.")
        } else {
            val newStep = Step(0, title.text.toString(), false, id)
            stepsViewModel.insertStep(newStep, this)
            cardView.visibility = View.GONE
            title.setText("")
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun cancelStep(title: EditText, cardView: CardView, recyclerView: RecyclerView) {
        cardView.visibility = View.GONE
        title.setText("")
        recyclerView.visibility = View.VISIBLE
    }

    private fun goToHomePage() {
        startActivity(Intent(this@StepsActivity, HomeActivity::class.java))
        finish()
    }

    private fun loadSteps() {
        stepsViewModel.getStep(id).observe(this) { taskWithStepsList ->
            Log.d("Observer", "Observer triggered. TaskWithSteps: $taskWithStepsList")
            taskWithStepsList?.let { tasksWithSteps ->
                val steps = tasksWithSteps.steps // Access the List<Step> directly
                adapter.submitList(steps)
            }
        }
    }
}
