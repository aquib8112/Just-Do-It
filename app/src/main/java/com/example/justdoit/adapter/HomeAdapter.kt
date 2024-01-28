package com.example.justdoit.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.justdoit.R
import com.example.justdoit.activity.HomeActivity
import com.example.justdoit.activity.StepsActivity
import com.example.justdoit.data.TaskDatabase
import com.example.justdoit.data.entitie.OfflineTask
import com.example.justdoit.data.entitie.Task
import com.example.justdoit.data.fireStore.FireStoreCO
import com.example.justdoit.util.isInternetAvailable
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeAdapter : ListAdapter<Task, HomeAdapter.TaskViewHolder>(DiffUtil()) {

    class DiffUtil : androidx.recyclerview.widget.DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.taskId == newItem.taskId
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var title = view.findViewById<TextView>(R.id.tv_title)
        private var description = view.findViewById<TextView>(R.id.tv_description)
        private var date = view.findViewById<TextView>(R.id.tv_date)

        fun bind(task: Task) {
            title.text = task.title
            description.text = task.description
            date.text = task.date

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, StepsActivity::class.java)
                intent.putExtra("id", task.taskId)
                intent.putExtra("title", task.title)
                intent.putExtra("description", task.description)
                intent.putExtra("date", task.date)
                itemView.context.startActivity(intent)
            }

            itemView.setOnLongClickListener {
                val bottomSheetDialog = BottomSheetDialog(itemView.context)
                val bottomSheetView = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.edit_delete_card, null)
                bottomSheetDialog.setContentView(bottomSheetView)

                bottomSheetView.findViewById<TextView>(R.id.edit_text_tv).setOnClickListener {
                    val activity = itemView.context as HomeActivity
                    bottomSheetDialog.dismiss()
                    activity.rvTask.visibility = View.GONE
                    activity.updateTaskView.visibility = View.VISIBLE
                    activity.updateTitleEt.setText(title.text.toString())
                    activity.updateDescriptionEt.setText(description.text.toString())
                    activity.updateDateEt.setText(date.text.toString())

                    activity.dateUpdateButton.setOnClickListener {
                        activity.updateTaskView.visibility = View.GONE
                        activity.calenderView.visibility = View.VISIBLE
                    }

                    activity.calenderView.setOnDateChangeListener { _, year, month, dayOfMonth ->
                        val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                        val formattedDate =
                            DateTimeFormatter.ofPattern("dd-MMM-yyyy").format(selectedDate)
                        activity.updateDateEt.setText(formattedDate)
                        activity.calenderView.visibility = View.GONE
                        activity.updateTaskView.visibility = View.VISIBLE
                    }

                    activity.updateButton.setOnClickListener {
                        Log.d("updateButton", "working")
                        val updatedTask = Task(
                            task.taskId,
                            activity.updateTitleEt.text.toString(),
                            activity.updateDescriptionEt.text.toString(),
                            activity.updateDateEt.text.toString()
                        )
                        if(nothingNull(
                                activity.updateTitleEt,
                                activity.updateDescriptionEt,
                                activity.updateDateEt
                            )
                        ) {
                            CoroutineScope(Dispatchers.IO).launch {
                                if (isInternetAvailable(itemView.context)) {
                                    FireStoreCO().updateTaskInFirestore(updatedTask)
                                }
                                updateTaskInRoom(updatedTask, itemView.context)
                                withContext(Dispatchers.Main) {
                                    activity.updateTaskView.visibility = View.INVISIBLE
                                    activity.updateTitleEt.setText("")
                                    activity.updateDescriptionEt.setText("")
                                    activity.updateDateEt.setText("")
                                    activity.rvTask.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                    activity.cancelUpdateButton.setOnClickListener {
                        activity.updateTaskView.visibility = View.INVISIBLE
                        activity.updateTitleEt.setText("")
                        activity.updateDescriptionEt.setText("")
                        activity.updateDateEt.setText("")
                        activity.rvTask.visibility = View.VISIBLE
                    }
                }

                bottomSheetView.findViewById<TextView>(R.id.delete_text_tv).setOnClickListener {
                    val activity = itemView.context as HomeActivity
                    activity.taskDeleteConfirmationCard.visibility = View.VISIBLE

                    activity.taskDeleteConfirmButton.setOnClickListener {
                        CoroutineScope(Dispatchers.IO).launch {
                            deleteTaskInRoom(task, itemView.context)
                            if (isInternetAvailable(itemView.context)){
                                FireStoreCO().deleteTaskAndStepsInFirestore(task.taskId)
                            }
                        }
                            activity.taskDeleteConfirmationCard.visibility = View.GONE
                        }

                    activity.cancelTaskDeleteButton.setOnClickListener {
                        activity.taskDeleteConfirmationCard.visibility = View.GONE
                    }
                        bottomSheetDialog.dismiss()
                    }
                    bottomSheetDialog.show()
                    true
                }

            }

        }
    }

private fun nothingNull(title: EditText, description: EditText, date: EditText):Boolean{
    if (title.text.isNullOrEmpty()){
        title.error = "please enter task name"
        return false
    }else if (description.text.isNullOrEmpty()){
        description.error = "please enter task description"
        return false
    }else if (date.text.isNullOrEmpty()){
        date.error = "please enter date"
        return false
    }
    return true
}

private suspend fun updateTaskInRoom(task: Task,context: Context){
    val database = TaskDatabase.getDatabase(context)
    if (!isInternetAvailable(context)){
        updateTaskInOfflineTask(task,context)
    }
    database.taskDao().updateTask(task)
}

private suspend fun deleteTaskInRoom(task: Task,context: Context){
    val database = TaskDatabase.getDatabase(context).taskDao()
    database.deleteStepsForTask(task.taskId)
    deleteTaskInOfflineTask(task,context)
    database.deleteTask(task)
}

private suspend fun updateTaskInOfflineTask(task: Task,context: Context) {
    val database = TaskDatabase.getDatabase(context).offlineTaskDao()
    if (database.getOfflineTaskById(task.taskId) != null){
        val offlineTask = OfflineTask(task.taskId,task.title,task.description,task.date,isDeleted = false,isUpdated = false)
        database.updateOfflineTask(offlineTask)
    }else{
        val offlineTask = OfflineTask(task.taskId,task.title,task.description,task.date,isDeleted = false,isUpdated = true)
        database.insertOfflineTask(offlineTask)
    }
}

private suspend fun deleteTaskInOfflineTask(task: Task,context: Context) {
    val database = TaskDatabase.getDatabase(context).offlineTaskDao()
    if (database.getOfflineTaskById(task.taskId) != null){
        //if the task exist in offlineTask DB than that means task was created when the device was offline
        // and the device was never online after it was created then just delete it
        val offlineTask = OfflineTask(task.taskId,task.title,task.description,task.date, isDeleted = false,isUpdated = false)
        database.deleteOfflineTask(offlineTask)
    }else{
        //if the task does not exist in offlineTask DB than that means task was created when the device was online
        //and now when the device is offline it is being deleted so we will just insert it delete value of true so it will be deleted by sync function
        val offlineTask = OfflineTask(task.taskId,task.title,task.description,task.date, isDeleted = true,isUpdated = false)
        database.insertOfflineTask(offlineTask)
    }
}
