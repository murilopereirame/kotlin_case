package br.dev.murilopereira.todo.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.dev.murilopereira.todo.databinding.TaskItemBinding
import br.dev.murilopereira.todo.dto.TaskDTO
import br.dev.murilopereira.todo.model.SubTask
import br.dev.murilopereira.todo.model.Task
import br.dev.murilopereira.todo.model.TaskAndSubTask

class TaskAdapter (
    private val context: Context,
    tasks: List<TaskDTO> = emptyList(),
    val onClick: (task: TaskDTO) -> Unit = { }
): RecyclerView.Adapter<TaskAdapter.ViewHolder>() {
    private val tasks = tasks.toMutableList()
    inner class ViewHolder(binding: TaskItemBinding): RecyclerView.ViewHolder(binding.root) {
        private lateinit var task: TaskDTO;

        private val taskListItem = binding.taskListItem
        private val taskListTitle = binding.taskListTitle
        private val taskListDone = binding.taskListDone
        private val taskListPending = binding.taskListPending

        fun doBinding(task: TaskDTO) {
            this.task = task

            taskListItem.setOnClickListener { _ ->
                onClick(task)
            }
            taskListTitle.text = task.title
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

    fun addTask(task: TaskDTO) {
        this.tasks.add(task)
        this.notifyDataSetChanged()
    }

    fun update(tasks: List<TaskDTO>) {
        this.tasks.clear()
        this.tasks.addAll(tasks)
        this.notifyDataSetChanged()
    }

    override fun getItemCount(): Int = tasks.size

}