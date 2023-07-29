package br.dev.murilopereira.todo.dto

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi

@JsonClass(generateAdapter = true)
data class UserDTO(val email: String? = null, val password: String? = null) {
    fun toJson(): String {
        val moshi: Moshi = Moshi.Builder().build()
        val jsonAdapter: JsonAdapter<UserDTO> = moshi.adapter<UserDTO>(UserDTO::class.java)

        return jsonAdapter.toJson(this)
    }
}