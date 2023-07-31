package br.dev.murilopereira.todo.dto

import br.dev.murilopereira.todo.enums.StatusType
import br.dev.murilopereira.todo.interfaces.RequestTypeInterface
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class RequestSuccessDTO(
    val message: String,
    val data: List<Any>,
    val details: List<Any>
): RequestTypeInterface {
    override val status: StatusType = StatusType.SUCCESS
}