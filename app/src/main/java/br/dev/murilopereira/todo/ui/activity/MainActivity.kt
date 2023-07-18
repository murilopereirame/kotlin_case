package br.dev.murilopereira.todo.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import br.dev.murilopereira.todo.R
import br.dev.murilopereira.todo.database.AppDatabase
import br.dev.murilopereira.todo.databinding.ActivityMainBinding
import br.dev.murilopereira.todo.databinding.NewTaskDialogBinding
import br.dev.murilopereira.todo.model.Task
import br.dev.murilopereira.todo.ui.adapter.TaskAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class MainActivity : AppCompatActivity() {
    private val adapter = TaskAdapter(context = this) {
        task -> Log.d("[TASK TAP]", task.toString())
    }

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater);
    }

    override fun onResume() {
        super.onResume()
        Log.d("[RESUME]", "App Resumed")
        Log.d("[TASKS]", AppDatabase.instance(this).taskDao().getAll().toString())
        adapter.update(AppDatabase.instance(this).taskDao().getAll())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val newTaskButton = binding.newTaskButton;
        newTaskButton.setOnClickListener { v ->
            NewTaskDialogBinding.inflate(LayoutInflater.from(this)).apply {
                MaterialAlertDialogBuilder(this@MainActivity)
                    .setView(root)
                    .setTitle(R.string.new_task_dialog_title)
                    .setPositiveButton(R.string.new_task_create) { _, _ ->
                        val name = newTaskDialogInputTitle.text.toString();
                        val taskId = AppDatabase
                            .instance(this@MainActivity)
                            .taskDao()
                            .save(
                                Task(title = name, done = false)
                            )

                        val intent = Intent(this@MainActivity, ActivitySubtaskList::class.java)
                        intent.putExtra("taskId", taskId)

                        startActivity(intent)
                    }.show()
            }
        }

        val taskList = binding.taskList
        taskList.adapter = adapter

        setContentView(binding.root)
    }
}