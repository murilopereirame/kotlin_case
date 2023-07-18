package br.dev.murilopereira.todo.model

import androidx.room.Embedded
import androidx.room.Relation

class TaskAndSubTask(
    @Embedded
    val task: Task,
    @Relation(
        parentColumn = "uid",
        entityColumn = "task"
    )
    val subtasks: List<SubTask>
)