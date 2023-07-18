package br.dev.murilopereira.todo.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import br.dev.murilopereira.todo.R
import br.dev.murilopereira.todo.database.AppDatabase
import br.dev.murilopereira.todo.databinding.ActivityMainBinding
import br.dev.murilopereira.todo.databinding.NewTaskDialogBinding
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
        adapter.update(AppDatabase.instance(this).taskDao().getAll())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val newTaskButton = binding.newTaskButton;
        newTaskButton.setOnClickListener { v ->
            NewTaskDialogBinding.inflate(LayoutInflater.from(this)).apply {
                MaterialAlertDialogBuilder(this.root.context)
                    .setView(root)
                    .setTitle(R.string.new_task_dialog_title)
                    .setPositiveButton(R.string.new_task_create) { _, _ ->
                        val name = newTaskDialogInputTitle.text.toString(); Log.d(
                        "[NEW TASK]",
                            name
                        )
                    }.show()
            }
            Log.d("[NEW_TASK_BUTTON]", "Hello World!");
        }

        val taskList = binding.taskList
        taskList.adapter = adapter

        setContentView(binding.root)
    }
}