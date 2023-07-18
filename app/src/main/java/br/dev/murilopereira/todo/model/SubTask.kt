package br.dev.murilopereira.todo.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = arrayOf("uid"),
            childColumns = arrayOf("task"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@Parcelize
class SubTask(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0L,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "done") var done: Boolean,
    @ColumnInfo(name = "task") val task: Long
) : Parcelable