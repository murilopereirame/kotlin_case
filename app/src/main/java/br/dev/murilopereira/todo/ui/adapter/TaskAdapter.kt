package br.dev.murilopereira.todo.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.dev.murilopereira.todo.databinding.TaskItemBinding
import br.dev.murilopereira.todo.model.SubTask
import br.dev.murilopereira.todo.model.Task
import br.dev.murilopereira.todo.model.TaskAndSubTask

class TaskAdapter (
    private val context: Context,
    tasks: List<TaskAndSubTask> = emptyList(),
    val onClick: (task: TaskAndSubTask) -> Unit = { }
): RecyclerView.Adapter<TaskAdapter.ViewHolder>() {
    private val tasks = tasks.toMutableList()
    inner class ViewHolder(binding: TaskItemBinding): RecyclerView.ViewHolder(binding.root) {
        private lateinit var task: TaskAndSubTask;

        private val taskListItem = binding.taskListItem
        private val taskListTitle = binding.taskListTitle
        private val taskListDone = binding.taskListDone
        private val taskListPending = binding.taskListPending

        fun doBinding(task: TaskAndSubTask) {
            this.task = task

            val taskCount = countTasks(task.subtasks)

            taskListItem.setOnClickListener { _ ->
                onClick(task)
            }
            taskListTitle.text = task.task.title
            taskListDone.text = taskCount["done"].toString()
            taskListPending.text= taskCount["pending"].toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TaskItemBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = tasks[position]
        holder.doBinding(task)
    }

    fun countTasks(tasks: List<SubTask>): Map<String, Int> {
        var done: Int = 0
        var pending: Int = 0
        tasks.forEach {
            task -> if(task.done) done++ else pending++
        }

        return mapOf("done" to done, "pending" to pending)
    }

    fun update(tasks: List<TaskAndSubTask>) {
        this.tasks.clear()
        this.tasks.addAll(tasks)
        this.notifyDataSetChanged()
    }

    override fun getItemCount(): Int = tasks.size

}