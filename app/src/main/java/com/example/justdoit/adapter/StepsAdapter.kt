package com.example.justdoit.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.justdoit.R
import com.example.justdoit.activity.StepsActivity
import com.example.justdoit.data.TaskDatabase
import com.example.justdoit.data.entitie.OfflineStep
import com.example.justdoit.data.entitie.Step
import com.example.justdoit.data.fireStore.FireStoreCO
import com.example.justdoit.util.isInternetAvailable
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StepsAdapter : ListAdapter<Step, StepsAdapter.StepsViewHolder>(DiffUtil()) {

    class DiffUtil : androidx.recyclerview.widget.DiffUtil.ItemCallback<Step> (){
        override fun areItemsTheSame(oldItem: Step, newItem: Step): Boolean {
            return oldItem.stepId  == newItem.stepId
        }

        override fun areContentsTheSame(oldItem: Step, newItem: Step): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepsViewHolder {
        return StepsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.steps_item,parent,false))
    }

    override fun onBindViewHolder(holder: StepsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StepsViewHolder(view : View): RecyclerView.ViewHolder(view){
        private val stepName = view.findViewById<TextView>(R.id.step_name)
        private val stepCheck = view.findViewById<CheckBox>(R.id.step_complete_check)

        fun bind(step: Step){
            stepName.text = step.name
            stepCheck.isChecked = step.check

            stepCheck.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    val updateStep = Step(
                        step.stepId,
                        stepName.text.toString(),
                        stepCheck.isChecked,
                        step.taskId
                    )
                    updateStepInRoom(updateStep, itemView.context)
                    if (isInternetAvailable(itemView.context)) {
                        FireStoreCO().updateStepInFirestore(updateStep)
                    }
                }
            }

            itemView.setOnClickListener {
                val bottomSheetDialog = BottomSheetDialog(itemView.context)
                val bottomSheetView = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.edit_delete_card, null)
                bottomSheetDialog.setContentView(bottomSheetView)

                bottomSheetView.findViewById<TextView>(R.id.edit_text_tv).setOnClickListener {
                    val activity = itemView.context as StepsActivity
                    bottomSheetDialog.dismiss()
                    activity.stepRV.visibility = View.GONE
                    activity.updateStepCard.visibility = View.VISIBLE
                    activity.updateTitle.setText(stepName.text.toString())

                    activity.updateStepButton.setOnClickListener {
                        CoroutineScope(Dispatchers.IO).launch {
                            val updatedStep = Step(
                                step.stepId,
                                activity.updateTitle.text.toString(),
                                stepCheck.isChecked,
                                step.taskId
                            )
                            updateStepInRoom(updatedStep, itemView.context)
                            if(isInternetAvailable(itemView.context)){
                                FireStoreCO().updateStepInFirestore(updatedStep)
                            }
                            withContext(Dispatchers.Main) {
                                activity.updateStepCard.visibility = View.INVISIBLE
                                activity.updateTitle.setText("")
                                activity.stepRV.visibility = View.VISIBLE
                            }
                        }
                    }
                    activity.cancelUpdateButton.setOnClickListener {
                        activity.updateStepCard.visibility = View.INVISIBLE
                        activity.updateTitle.setText("")
                        activity.stepRV.visibility = View.VISIBLE
                    }
                }

                bottomSheetView.findViewById<TextView>(R.id.delete_text_tv).setOnClickListener {
                    val activity = itemView.context as StepsActivity
                    activity.stepDeleteConfirmationCard.visibility = View.VISIBLE

                    activity.stepDeleteConfirmButton.setOnClickListener {
                        CoroutineScope(Dispatchers.IO).launch {
                            deleteStepInRoom(step, itemView.context)
                            if (isInternetAvailable(itemView.context)){
                                FireStoreCO().deleteStepInFirestore(step.taskId,step.stepId)
                            }
                        }
                        activity.stepDeleteConfirmationCard.visibility = View.GONE
                    }

                    activity.cancelStepDeleteButton.setOnClickListener {
                        activity.stepDeleteConfirmationCard.visibility = View.GONE
                    }
                    bottomSheetDialog.dismiss()
                }

                bottomSheetDialog.show()
            }

       }
    }
}

private suspend fun updateStepInRoom(step: Step, context: Context) {
    val database = TaskDatabase.getDatabase(context)
    if (!isInternetAvailable(context)){
        updateStepInOfflineSteps(step, context)
    }
    database.stepDao().updateStep(step)
}

private suspend fun deleteStepInRoom(step: Step, context: Context){
    val database = TaskDatabase.getDatabase(context)
    if(!isInternetAvailable(context)){
        deleteStepInOfflineSteps(step,context)
    }
    database.stepDao().deleteStep(step)
}

private suspend fun updateStepInOfflineSteps(step: Step, context: Context) {
    val database = TaskDatabase.getDatabase(context).offlineStepDao()
    if (database.getOfflineStepById(step.stepId) != null) {
        val offlineStep = OfflineStep(step.stepId,step.name,step.check,step.taskId,isDeleted = false,isUpdated = false)
        database.updateOfflineStep(offlineStep)
    } else {
        val offlineStep = OfflineStep(step.stepId,step.name,step.check,step.taskId,isDeleted = false,isUpdated = true)
        database.insertOfflineStep(offlineStep)
    }
}

private suspend fun deleteStepInOfflineSteps(step: Step, context: Context) {
    val database = TaskDatabase.getDatabase(context).offlineStepDao()
    if (database.getOfflineStepById(step.stepId) != null) {
        val offlineStep = OfflineStep(step.stepId,step.name,step.check,step.taskId,isDeleted = true,isUpdated = false)
        database.deleteOfflineStep(offlineStep)
    } else {
        val offlineStep = OfflineStep(step.stepId,step.name,step.check,step.taskId,isDeleted = true,isUpdated = false)
        database.insertOfflineStep(offlineStep)
    }
}