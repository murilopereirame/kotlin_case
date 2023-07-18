package br.dev.murilopereira.todo.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.dev.murilopereira.todo.database.converter.Converters
import br.dev.murilopereira.todo.database.dao.SubTaskDao
import br.dev.murilopereira.todo.database.dao.TaskDao
import br.dev.murilopereira.todo.model.SubTask
import br.dev.murilopereira.todo.model.Task
import br.dev.murilopereira.todo.model.TaskAndSubTask

@Database(entities = [Task::class, SubTask::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun subtaskDao(): SubTaskDao

    companion object {
        @Volatile
        private lateinit var db: AppDatabase

        fun instance(context: Context): AppDatabase {
            if(::db.isInitialized) return db

            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "todo.db"
            ).allowMainThreadQueries().build().also {
                db = it
            }
        }
    }
}