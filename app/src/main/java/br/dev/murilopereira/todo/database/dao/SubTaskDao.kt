package br.dev.murilopereira.todo.database.dao

import androidx.room.*
import br.dev.murilopereira.todo.model.SubTask
import br.dev.murilopereira.todo.model.TaskAndSubTask

@Dao
interface SubTaskDao {
    @Query("SELECT * FROM subtasks")
    fun getAll(): List<SubTask>

    @Query("SELECT * FROM subtasks WHERE uid IN(:ids)")
    fun getAllByIds(ids: IntArray): List<SubTask>

    @Query("SELECT * FROM tasks WHERE uid=:id")
    fun getAllByTask(id: Long): List<TaskAndSubTask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(subtask: SubTask): Long

    @Delete
    fun delete(task: SubTask)

    @Query("DELETE FROM subtasks WHERE uid=:id")
    fun deleteById(id: Long)
}