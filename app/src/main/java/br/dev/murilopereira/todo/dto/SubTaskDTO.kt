package br.dev.murilopereira.todo.dto

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

class SubTaskDTO(
    private var uuid:String = "",
    var content: String = "",
    var done: Boolean? = false,
    private var tasks_idtask: String?
) {
    private val moshi: Moshi = Moshi.Builder().build()
    private val jsonAdapter: JsonAdapter<SubTaskDTO> = moshi.adapter(SubTaskDTO::class.java)

    fun toJson(): String {
        return jsonAdapter.toJson(this)
    }

    fun fromJson(json: String) {
        val tempDTO = jsonAdapter.fromJson(json)

        this.content = tempDTO?.content.toString()
        this.done = tempDTO?.done
        this.uuid = tempDTO?.uuid.toString()
        this.tasks_idtask = tempDTO?.tasks_idtask.toString()
    }
}