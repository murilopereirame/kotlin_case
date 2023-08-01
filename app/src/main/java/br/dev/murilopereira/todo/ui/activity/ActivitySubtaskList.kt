package br.dev.murilopereira.todo.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.dev.murilopereira.todo.R
import br.dev.murilopereira.todo.database.AppDatabase
import br.dev.murilopereira.todo.databinding.ActivitySubtaskListBinding
import br.dev.murilopereira.todo.databinding.NewSubtaskDialogBinding
import br.dev.murilopereira.todo.dto.RequestErrorDTO
import br.dev.murilopereira.todo.dto.RequestSuccessDTO
import br.dev.murilopereira.todo.dto.SubTaskDTO
import br.dev.murilopereira.todo.dto.TaskDTO
import br.dev.murilopereira.todo.enums.ContentTypeEnum
import br.dev.murilopereira.todo.enums.StatusType
import br.dev.murilopereira.todo.interfaces.RequestTypeInterface
import br.dev.murilopereira.todo.model.SubTask
import br.dev.murilopereira.todo.model.Task
import br.dev.murilopereira.todo.model.TaskAndSubTask
import br.dev.murilopereira.todo.ui.adapter.SubTaskAdapter
import br.dev.murilopereira.todo.ui.dialog.LoadingDialog
import br.dev.murilopereira.todo.util.DialogSingleton
import br.dev.murilopereira.todo.util.OkHttpSingleton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import kotlin.streams.toList

class ActivitySubtaskList : AppCompatActivity() {
    private var taskId = ""
    private val adapter = SubTaskAdapter(this) {
        subtask,isChecked -> if(subtask.done != isChecked) {subtask.done = isChecked; updateSubtask(subtask)}
    }
    private val binding by lazy {
        ActivitySubtaskListBinding.inflate(layoutInflater);
    }

    private var loadingDialog: LoadingDialog? = null
    private var errorDialog: AlertDialog.Builder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.taskId = this.intent.getStringExtra("taskId").toString();

        val addSubTaskButton = binding.subtaskListAddButton
        addSubTaskButton.setOnClickListener {
            _ ->
                NewSubtaskDialogBinding.inflate(LayoutInflater.from(this)).apply {
                    MaterialAlertDialogBuilder(this@ActivitySubtaskList)
                        .setTitle(R.string.new_subtask_dialog_title)
                        .setView(root)
                        .setPositiveButton(R.string.new_subtask_dialog_create) { _, _ ->
                            val content = newSubTaskDialogInputTitle.text.toString()
                            val isDone = newSubTaskDialogDone.isChecked
                            createSubTask(content, isDone)
                        }.show()
                }
        }

        binding.subtaskList.adapter = adapter

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        errorDialog = DialogSingleton.getErrorDialog(this)
        loadSubTasks()
        setContentView(binding.root)
    }

    private fun updateSubtask(subtask: SubTaskDTO) {
        loadingDialog = DialogSingleton.getLoadingDialog(this@ActivitySubtaskList)
        loadingDialog!!.show()

        val client = OkHttpSingleton.instance?.getClient()

        val token =
            getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE)
                .getString("access_token", "")

        val body = subtask.toJson().toRequestBody(ContentTypeEnum.JSON.media)

        val request = Request.Builder()
            .url("https://spring.murilopereira.dev.br:8443/subtasks/update/$taskId/${subtask.uuid}")
            .addHeader("Authorization", "Bearer $token")
            .patch(body).build()

        client?.newCall(request)?.enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                return handleRequestFailure(null)
            }

            override fun onResponse(call: Call, response: Response) {
                handleUpdateSubtaskResponse(response)
            }
        })
    }

    private fun handleUpdateSubtaskResponse(response: Response) {
        val responseBody = response.body?.string()
        Log.i("[UPDATE SUBTASK]", responseBody.toString())

        var parsedResponse: Map<*, *>? = null;

        if(response.body != null && responseBody.toString().isNotEmpty()) {
            val moshi: Moshi = Moshi.Builder().build()
            parsedResponse = moshi.adapter(Map::class.java).fromJson(
                responseBody.toString()
            )
        }

        if(parsedResponse == null || !response.isSuccessful) {
            return handleRequestFailure(parsedResponse)
        }

        runOnUiThread {
            loadingDialog?.dismiss()
        }
    }

    private fun createSubTask(content: String, done: Boolean) {
        loadingDialog = DialogSingleton.getLoadingDialog(this@ActivitySubtaskList)
        loadingDialog!!.show()

        val client = OkHttpSingleton.instance?.getClient()

        val token =
            getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE)
                .getString("access_token", "")

        val subtask = SubTaskDTO("", content, done, taskId)
        val body = subtask.toJson().toRequestBody(ContentTypeEnum.JSON.media)

        val request = Request.Builder().url("https://spring.murilopereira.dev.br:8443/subtasks/new/$taskId")
            .addHeader("Authorization", "Bearer $token")
            .post(body).build()

        client?.newCall(request)?.enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                return handleRequestFailure(null)
            }

            override fun onResponse(call: Call, response: Response) {
                handleCreateSubTaskResponse(response)
            }
        })
    }

    private fun handleCreateSubTaskResponse(response: Response) {
        val responseBody = response.body?.string()
        Log.i("[NEW SUBTASK]", responseBody.toString())

        var parsedResponse: Map<*, *>? = null;

        if(response.body != null && responseBody.toString().isNotEmpty()) {
            val moshi: Moshi = Moshi.Builder().build()
            parsedResponse = moshi.adapter(Map::class.java).fromJson(
                responseBody.toString()
            )
        }

        if(parsedResponse == null || !response.isSuccessful) {
            return handleRequestFailure(parsedResponse)
        }

        val taskData = parsedResponse["data"] as Map<*, *>
        runOnUiThread {
            adapter.addSubTask(
                SubTaskDTO(
                    taskData["uuid"].toString(),
                    taskData["content"].toString(),
                    taskData["done"] == true,
                    taskData["tasks_idtask"].toString()
                )
            )
            loadingDialog?.dismiss()
        }
    }

    private fun handleRequestFailure(response: Map<*, *>?) {
        var message = getString(R.string.generic_new_task_error_content)

        if(response != null) {
            message = response["message"].toString()
        }

        runOnUiThread {
            loadingDialog?.dismiss()
            errorDialog?.setTitle(R.string.generic_new_task_error_title)?.setMessage(message)
                ?.setPositiveButton(R.string.generic_new_task_error_ok, null)?.show()
        }
    }

    private fun loadSubTasks() {
        loadingDialog = DialogSingleton.getLoadingDialog(this@ActivitySubtaskList)
        loadingDialog!!.show()

        val client = OkHttpSingleton.instance?.getClient()

        val token =
            getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE)
                .getString("access_token", "")

        val request = Request.Builder().url("https://spring.murilopereira.dev.br:8443/subtasks/list/$taskId")
            .addHeader("Authorization", "Bearer $token")
            .get().build()

        client?.newCall(request)?.enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                return handleRequestFailure(null)
            }

            override fun onResponse(call: Call, response: Response) {
                handleListSubTasksResponse(response)
            }
        })
    }

    private fun handleListSubTasksResponse(response: Response) {
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
            return handleRequestFailure(parsedResponse as Map<*, *>)
        }

        if(parsedResponse is RequestSuccessDTO) {
            val subtaskList = (parsedResponse.data as List<Map<String, *>>).stream().map {
                    item -> SubTaskDTO(
                        item["uuid"].toString(),
                        item["content"].toString(),
                        item["done"] as Boolean,
                        item["tasks_idtask"].toString()
                    )
            }.toList()

            val taskTitle = (parsedResponse.details[0] as Map<String, *>)["title"]

            runOnUiThread {
                adapter.update(subtaskList)
                this.actionBar?.title = taskTitle.toString()
                this.supportActionBar?.title = taskTitle.toString()
                loadingDialog?.dismiss()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home)
            finish()

        return super.onOptionsItemSelected(item)
    }
}