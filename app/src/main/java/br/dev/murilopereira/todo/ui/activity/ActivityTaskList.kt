package br.dev.murilopereira.todo.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.dev.murilopereira.todo.R
import br.dev.murilopereira.todo.database.AppDatabase
import br.dev.murilopereira.todo.databinding.ActivityMainBinding
import br.dev.murilopereira.todo.databinding.NewTaskDialogBinding
import br.dev.murilopereira.todo.dto.RequestErrorDTO
import br.dev.murilopereira.todo.dto.RequestSuccessDTO
import br.dev.murilopereira.todo.dto.TaskDTO
import br.dev.murilopereira.todo.enums.ContentTypeEnum
import br.dev.murilopereira.todo.enums.StatusType
import br.dev.murilopereira.todo.interfaces.RequestTypeInterface
import br.dev.murilopereira.todo.ui.adapter.TaskAdapter
import br.dev.murilopereira.todo.ui.dialog.LoadingDialog
import br.dev.murilopereira.todo.util.DialogSingleton
import br.dev.murilopereira.todo.util.OkHttpSingleton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import javax.net.ssl.SSLEngineResult.Status
import kotlin.streams.toList


class ActivityTaskList : AppCompatActivity() {
    private val adapter = TaskAdapter(context = this) {
        task ->
            val intent = Intent(this@ActivityTaskList, ActivitySubtaskList::class.java)
            intent.putExtra("taskId", task.uuid);
            startActivity(intent)
    }

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater);
    }

    private var loadingDialog: LoadingDialog? = null
    private var errorDialog: AlertDialog.Builder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        errorDialog = DialogSingleton.getErrorDialog(this)

        val newTaskButton = binding.newTaskButton;
        newTaskButton.setOnClickListener {showNewTaskDialog()}

        val taskList = binding.taskList
        taskList.adapter = adapter

        setContentView(binding.root)

        loadTasks()
    }

    private fun loadTasks() {
        loadingDialog = DialogSingleton.getLoadingDialog(this@ActivityTaskList)
        loadingDialog!!.show()

        val client = OkHttpSingleton.instance?.getClient()

        val token =
            getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE)
                .getString("access_token", "")

        val request = Request.Builder().url("https://spring.murilopereira.dev.br:8443/tasks/list")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        client?.newCall(request)?.enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                return handleRequestFailure(null, getString(R.string.generic_error))
            }

            override fun onResponse(call: Call, response: Response) {
                handleListTaskResponse(response)
            }
        })
    }

    private fun handleListTaskResponse(response: Response) {
        val responseBody = response.body?.string()

        var parsedResponse: Any? = null;

        if(response.body != null && responseBody.toString().isNotEmpty()) {
            val moshi: Moshi = Moshi.Builder().add(
                PolymorphicJsonAdapterFactory.of(RequestTypeInterface::class.java, "status")
                    .withSubtype(RequestSuccessDTO::class.java, StatusType.SUCCESS.name)
                    .withSubtype(RequestErrorDTO::class.java, StatusType.ERROR.name)
                    .withFallbackJsonAdapter(Moshi.Builder().build().adapter(Any::class.java))
            ).build()

            parsedResponse = moshi.adapter(RequestTypeInterface::class.java).fromJson(
                responseBody.toString()
            )
        }

        if(parsedResponse == null || !response.isSuccessful) {
            return handleRequestFailure(if(parsedResponse != null) parsedResponse as Map<*, *> else null, getString(R.string.generic_error))
        }

        if(parsedResponse is RequestSuccessDTO) {
            val taskList = (parsedResponse.data as List<Map<String, *>>).stream().map {
                    item -> TaskDTO(item["uuid"].toString(), item["title"].toString(), item["done"] as Boolean)
            }.toList()

            runOnUiThread {
                adapter.update(taskList)
                loadingDialog?.dismiss()
            }
        }
    }

    private fun showNewTaskDialog() {
        NewTaskDialogBinding.inflate(LayoutInflater.from(this)).apply {
            MaterialAlertDialogBuilder(this@ActivityTaskList)
                .setView(root)
                .setTitle(R.string.new_task_dialog_title)
                .setPositiveButton(R.string.new_task_create) { _, _ ->
                    val name = newTaskDialogInputTitle.text.toString();
                    createTask(name)

                    loadingDialog = DialogSingleton.getLoadingDialog(this@ActivityTaskList)
                    loadingDialog?.show()
                }.show()
        }
    }

    private fun handleRequestFailure(response: Map<*, *>?, message: String) {
        var msg = message
        if(response != null) {
            msg = response["message"].toString()
        }

        runOnUiThread {
            loadingDialog?.dismiss()
            errorDialog?.setTitle(R.string.generic_new_task_error_title)?.setMessage(msg)
                ?.setPositiveButton(R.string.generic_new_task_error_ok, null)?.show()
        }
    }

    private fun handleCreateTaskResponse(response: Response) {
        val responseBody = response.body?.string()
        Log.i("[NEW TASK]", responseBody.toString())

        var parsedResponse: Map<*, *>? = null;

        if(response.body != null && responseBody.toString().isNotEmpty()) {
            val moshi: Moshi = Moshi.Builder().build()
            parsedResponse = moshi.adapter(Map::class.java).fromJson(
                responseBody.toString()
            )
        }

        if(parsedResponse == null || !response.isSuccessful) {
            return handleRequestFailure(parsedResponse, getString(R.string.generic_new_task_error_content))
        }

        val taskData = parsedResponse["data"] as Map<*, *>
        runOnUiThread {
            loadingDialog?.dismiss()
            adapter.addTask(TaskDTO(taskData["uuid"].toString(), taskData["title"].toString(), taskData["done"] == true))
        }

        val intent = Intent(this@ActivityTaskList, ActivitySubtaskList::class.java)
        intent.putExtra("taskId", taskData["uuid"].toString())
        startActivity(intent)
    }

    private fun createTask(taskTitle: String) {
        val client = OkHttpSingleton.instance?.getClient()

        val task: TaskDTO = TaskDTO("", taskTitle, false)
        val body = task.toJson().toRequestBody(ContentTypeEnum.JSON.media)

        val token =
            getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE)
                .getString("access_token", "")

        val request = Request.Builder().url("https://spring.murilopereira.dev.br:8443/tasks/new")
            .addHeader("Authorization", "Bearer $token")
            .post(body).build()

        client?.newCall(request)?.enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                return handleRequestFailure(null, getString(R.string.generic_new_task_error_content))
            }

            override fun onResponse(call: Call, response: Response) {
                handleCreateTaskResponse(response)
            }
        })
    }
}