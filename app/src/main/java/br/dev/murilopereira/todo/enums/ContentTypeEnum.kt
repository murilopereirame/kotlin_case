package br.dev.murilopereira.todo.enums

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType

enum class ContentTypeEnum(val media: MediaType) {
    JSON("application/json; charset=utf-8".toMediaType())
}