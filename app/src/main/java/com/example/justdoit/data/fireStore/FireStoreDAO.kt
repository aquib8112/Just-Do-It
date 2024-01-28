package com.example.justdoit.data.fireStore

import android.content.Context
import android.util.Log
import com.example.justdoit.data.TaskDatabase
import com.example.justdoit.data.entitie.Step
import com.example.justdoit.data.entitie.Task
import com.example.justdoit.util.isInternetAvailable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

interface FireStoreDAO {

    suspend fun downloadUserData(context: Context) {
        val db = Firebase.firestore
        val database = TaskDatabase.getDatabase(context)
        val userDocumentId = getUserDocumentId()

        Log.i("downloadUserData", "Starting download process...")

        try {
            if (userDocumentId != null) {
                Log.i("downloadUserData", "User document ID found: $userDocumentId")

                // Download tasks and steps
                db.collection("users")
                    .document(userDocumentId)
                    .collection("tasks")
                    .get()
                    .await()
                    .documents
                    .forEach { taskDocument ->
                        Log.d("downloadUserData", "Processing task document: ${taskDocument.id}")
                        val task = taskDocument.toObject(Task::class.java)
                        task?.let {
                            Log.d("downloadUserData", "Inserting task: $it")
                            database.taskDao().insertTask(it)

                            // Get steps directly using taskDocument
                            val stepsQuery = taskDocument.reference.collection("steps").get().await()
                            Log.d("downloadUserData", "Retrieved ${stepsQuery.documents.size} steps for task ${it.taskId}")
                            stepsQuery.documents
                                .mapNotNull { stepDocument -> stepDocument.toObject(Step::class.java) }
                                .forEach { step ->
                                    Log.d("downloadUserData", "Inserting step: $step")
                                    database.stepDao().insertStep(step)
                                }
                        }
                    }

                Log.i("downloadUserData", "Data download completed successfully.")

                // Notify UI on the main thread (uncomment when ready for UI updates)
                // withContext(Dispatchers.Main) {
                //     // Update UI elements to reflect the downloaded data
                // }
            } else {
                Log.w("downloadUserData", "User data not found.")
            }
        } catch (e: Exception) {
            Log.e("downloadUserData", "Error downloading user data: ${e.message}")
        }
    }

    private suspend fun getUserDocumentId(): String?{
        val db = Firebase.firestore
        val user = FirebaseAuth.getInstance().currentUser!!.uid
        val userQuery = db.collection("users")
            .whereEqualTo("uid",user)
            .get()
            .await()
        return if (!userQuery.isEmpty) {
            userQuery.documents[0].id
        } else {
            Log.d("getTaskDocumentId", "Task not found.") // Task not found
            null
        }
    }

    suspend fun userExist(uid:String): String?{
        val db = Firebase.firestore
        val user = FirebaseAuth.getInstance().currentUser!!.uid
        val userQuery = db.collection("users")
            .whereEqualTo("uid",user)
            .get()
            .await()
        return if (!userQuery.isEmpty) {
            userQuery.documents[0].id
        } else {
            Log.d("getTaskDocumentId", "Task not found.") // Task not found
            null
        }
    }

    private suspend fun getTaskAndUserDocumentId(taskId: Long): Pair<String?, String?> {
        val db = Firebase.firestore
        try {
            val userDocumentId = getUserDocumentId()
            val taskQuery = db.collection("users") // Find the task using taskId
                .document(userDocumentId!!)
                .collection("tasks")
                .whereEqualTo("taskId", taskId)
                .get()
                .await()

                val taskDocumentId = if (!taskQuery.isEmpty) taskQuery.documents[0].id else null
                return Pair(userDocumentId, taskDocumentId)

        } catch (e: Exception) {
            Log.e("getTaskDocumentId", "Error getting task document ID: ${e.message}")
        }
        return Pair(null, null)
    }

    private suspend fun getUserTaskAndStepDocumentIds(taskId: Long, stepId: Long): Array<String?> {
        val db = Firebase.firestore
        try {
            // Find the task document
            val (userDocumentId, taskDocumentId) = getTaskAndUserDocumentId(taskId)
            if (taskDocumentId != null && userDocumentId != null) {
                // Find the step document within the task
                val stepQuery = db.collection("users")
                    .document(userDocumentId)
                    .collection("tasks")
                    .document(taskDocumentId)
                    .collection("steps")
                    .whereEqualTo("stepId", stepId)
                    .get()
                    .await()

                val stepDocumentId = if (!stepQuery.isEmpty) stepQuery.documents[0].id else null
                return arrayOf(userDocumentId, taskDocumentId, stepDocumentId)
            }
        } catch (e: Exception) {
            Log.e("getTaskAndStepDocumentIds", "Error getting task and step document IDs: ${e.message}")
        }
        return emptyArray()
    }

    suspend fun addTaskInFirestore(task: Task) {
        val db = Firebase.firestore
        val userDocumentId = getUserDocumentId()// Find the task document
        if (userDocumentId != null) {
            db.collection("users")
                .document(userDocumentId)
                .collection("tasks")
                .add(task)
        }
    }

    suspend fun deleteTaskAndStepsInFirestore(taskId: Long) {
        val db = Firebase.firestore
        try {
            val documentIds = getTaskAndUserDocumentId(taskId)
            val userDocumentId = documentIds.first
            val taskDocumentId = documentIds.second

            if (taskDocumentId != null && userDocumentId != null) {
                // Delete all steps of the task first
                db.collection("users")
                    .document(userDocumentId)
                    .collection("tasks")
                    .document(taskDocumentId)
                    .collection("steps")
                    .get()
                    .await()
                    .documents
                    .forEach { stepDocument ->
                        stepDocument.reference.delete().await()
                    }

                // Then delete the task itself
                db.collection("users")
                    .document(userDocumentId)
                    .collection("tasks")
                    .document(taskDocumentId)
                    .delete()
                    .await()

                Log.i("deleteTaskAndStepsInFirestore", "Task and steps deleted successfully.")
            }
        } catch (e: Exception) {
            Log.e("deleteTaskAndStepsInFirestore", "Error deleting task and steps: ${e.message}")
        }
    }

    suspend fun updateTaskInFirestore(task: Task) {
        val db = Firebase.firestore
        try {
            val documentIds = getTaskAndUserDocumentId(task.taskId) // Get the task document ID
            val userDocumentId = documentIds.first
            val taskDocumentId = documentIds.second
            if (taskDocumentId != null && userDocumentId != null) {
                db.collection("users") // Update the task
                    .document(userDocumentId)
                    .collection("tasks")
                    .document(taskDocumentId)
                    .set(task) // Set the updated task
                    .await()
            }
        } catch (e: Exception) {
            Log.e("updateTaskInFirestore", "Error updating task: ${e.message}")
        }
    }

    suspend fun syncTask(context: Context) {
        val dao = TaskDatabase.getDatabase(context).offlineTaskDao()
        if (isInternetAvailable(context)) {
            try {
                val offlineTasks = dao.getAllOfflineTasks()
                for (offlineTask in offlineTasks) {
                    if (offlineTask.isDeleted) {
                        deleteTaskAndStepsInFirestore(offlineTask.taskId)
                    } else if (offlineTask.isUpdated) {
                        val newTask = Task(
                            offlineTask.taskId,
                            offlineTask.title,
                            offlineTask.description,
                            offlineTask.date
                        )
                        updateTaskInFirestore(newTask)
                    } else {
                        val newTask = Task(
                            offlineTask.taskId,
                            offlineTask.title,
                            offlineTask.description,
                            offlineTask.date
                        )
                        addTaskInFirestore(newTask)
                    }
                    dao.deleteOfflineTask(offlineTask)
                }
            } catch (e: Exception) {
                Log.e("syncTask", "Error syncing tasks: ${e.message}")
            }
        }
    }

    suspend fun addStepInFirestore(step: Step, context: Context) {
        val db = Firebase.firestore
        try {
            val documentIds = getTaskAndUserDocumentId(step.taskId) // Get the task document ID
            val userDocumentId = documentIds.first
            val taskDocumentId = documentIds.second// Find the task document
            if (taskDocumentId != null && userDocumentId != null) {
                // Add the step as a sub-collection of the specified task
                db.collection("users")
                    .document(userDocumentId)
                    .collection("tasks")
                    .document(taskDocumentId)
                    .collection("steps")
                    .add(step)
                    .await()
            }
        } catch (e: Exception) {
            Log.e("addStepInFirestore", "Error adding step: ${e.message}")
        }
    }

    suspend fun deleteStepInFirestore(taskId: Long, stepId: Long) {
        val db = Firebase.firestore
        try {
            val documentIds = getUserTaskAndStepDocumentIds(taskId,stepId)// Find the task and step documents
            val userDocumentId = documentIds[0]
            val taskDocumentId = documentIds[1]
            val stepDocumentId = documentIds[2]
            if (taskDocumentId != null && stepDocumentId != null && userDocumentId != null) {
                // Delete the step document
                db.collection("users")
                    .document(userDocumentId)
                    .collection("tasks")
                    .document(taskDocumentId)
                    .collection("steps")
                    .document(stepDocumentId)
                    .delete()
                    .await()
            }
        } catch (e: Exception) {
            Log.e("deleteStepInFirestore", "Error deleting step: ${e.message}")
        }
    }

    suspend fun updateStepInFirestore(step: Step) {
        val db = Firebase.firestore
        try {
            val documentIds = getUserTaskAndStepDocumentIds(step.taskId,step.stepId)// Find the task and step documents
            val userDocumentId = documentIds[0]
            val taskDocumentId = documentIds[1]
            val stepDocumentId = documentIds[2]
            if (taskDocumentId != null && stepDocumentId != null && userDocumentId != null) { // Update the step document
                db.collection("users")
                    .document(userDocumentId)
                    .collection("tasks")
                    .document(taskDocumentId)
                    .collection("steps")
                    .document(stepDocumentId)
                    .update("name", step.name, "check", step.check)
                    .await()
            }
        } catch (e: Exception) {
            Log.e("updateStepInFirestore", "Error updating step: ${e.message}")
        }
    }

    suspend fun syncStep(context: Context) {
        val dao = TaskDatabase.getDatabase(context).offlineStepDao()
        if (isInternetAvailable(context)) {
            val offlineSteps = dao.getAllOfflineSteps()
            for (offlineStep in offlineSteps) {
                if (offlineStep.isDeleted) {
                    deleteStepInFirestore(offlineStep.taskId, offlineStep.stepId)
                } else if (offlineStep.isUpdated) {
                    val newStep = Step(
                        offlineStep.stepId,
                        offlineStep.name,
                        offlineStep.check,
                        offlineStep.taskId
                    )
                    updateStepInFirestore(newStep)
                } else {
                    val newStep = Step(
                        offlineStep.stepId,
                        offlineStep.name,
                        offlineStep.check,
                        offlineStep.taskId
                    )
                    addStepInFirestore(newStep, context)
                }
                dao.deleteOfflineStep(offlineStep)
            }
        }
    }
}