package br.dev.murilopereira.todo.dto

import br.dev.murilopereira.todo.enums.StatusType
import br.dev.murilopereira.todo.interfaces.RequestTypeInterface
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class RequestErrorDTO(
    val message: String,
    val code: String,
    val details: List<String>): RequestTypeInterface {

    override val status: StatusType = StatusType.ERROR
}