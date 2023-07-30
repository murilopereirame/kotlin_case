package br.dev.murilopereira.todo.dto

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi

@JsonClass(generateAdapter = true)
class TaskDTO(var uuid: String? = "", var title: String, var done: Boolean? = false) {
    val moshi: Moshi = Moshi.Builder().build()
    val jsonAdapter: JsonAdapter<TaskDTO> = moshi.adapter(TaskDTO::class.java)

    fun toJson(): String {
        return jsonAdapter.toJson(this)
    }

    fun fromJson(json: String) {
        val tempDTO = jsonAdapter.fromJson(json)

        this.title = tempDTO?.title.toString()
        this.done = tempDTO?.done
        this.uuid = tempDTO?.uuid.toString()
    }
}