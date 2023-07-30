package br.dev.murilopereira.todo.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import br.dev.murilopereira.todo.R
import br.dev.murilopereira.todo.database.AppDatabase
import br.dev.murilopereira.todo.databinding.ActivitySubtaskListBinding
import br.dev.murilopereira.todo.databinding.NewSubtaskDialogBinding
import br.dev.murilopereira.todo.model.SubTask
import br.dev.murilopereira.todo.model.Task
import br.dev.murilopereira.todo.model.TaskAndSubTask
import br.dev.murilopereira.todo.ui.adapter.SubTaskAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ActivitySubtaskList : AppCompatActivity() {
    private var _taskId = 0L
    private val adapter = SubTaskAdapter(this) {
        subtask,isChecked -> subtask.done = isChecked; AppDatabase.instance(this).subtaskDao().save(subtask)
    }
    private val binding by lazy {
        ActivitySubtaskListBinding.inflate(layoutInflater);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val taskId = this.intent.getStringExtra("taskId");

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
                        }.show()
                }
        }

        binding.subtaskList.adapter = adapter

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home)
            finish()

        return super.onOptionsItemSelected(item)
    }
}