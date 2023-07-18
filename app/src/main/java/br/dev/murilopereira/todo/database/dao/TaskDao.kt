package br.dev.murilopereira.todo.database.dao;

import androidx.room.*

import br.dev.murilopereira.todo.model.Task;
import br.dev.murilopereira.todo.model.TaskAndSubTask

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun getAll(): List<TaskAndSubTask>

    @Query("SELECT * FROM tasks WHERE uid IN (:ids)")
    fun loadAllByIds(ids: IntArray): List<TaskAndSubTask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(vararg task: Task)

    @Delete
    fun delete(task: Task)

    @Query("DELETE FROM tasks WHERE uid=:id")
    fun deleteById(id: Long)
}
