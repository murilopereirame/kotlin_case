package br.dev.murilopereira.todo.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.dev.murilopereira.todo.databinding.SubtaskItemBinding
import br.dev.murilopereira.todo.model.SubTask

class SubTaskAdapter(
    private val context: Context,
    subtasks: List<SubTask> = emptyList(),
    val onClick: (subtask: SubTask, isChecked: Boolean) -> Unit = {subtask, isChecked -> }
): RecyclerView.Adapter<SubTaskAdapter.ViewHolder>() {
    private val subtasks = subtasks.toMutableList()

    inner class ViewHolder(binding: SubtaskItemBinding): RecyclerView.ViewHolder(binding.root) {
        private lateinit var subtask: SubTask

        private val checkBox = binding.taskItem

        fun doBinding(subtask: SubTask) {
            this.subtask = subtask

            checkBox.setOnCheckedChangeListener {
                _, isChecked -> onClick(subtask, isChecked)
            }
            checkBox.text = subtask.content
            checkBox.isChecked = subtask.done
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SubtaskItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = subtasks.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subTask = subtasks[position]
        holder.doBinding(subTask)
    }

    fun update(subtasks: List<SubTask>) {
        this.subtasks.clear()
        this.subtasks.addAll(subtasks)
        this.notifyDataSetChanged()
    }

}