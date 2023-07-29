package br.dev.murilopereira.todo.util

import okhttp3.OkHttpClient

class OkHttpSingleton private constructor() {
    private var client: OkHttpClient = OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .build()

    fun getClient(): OkHttpClient {
        return client
    }

    fun setClient(client: OkHttpClient) {
        this.client = client
    }

    fun closeConnections() {
        client.dispatcher.cancelAll()
    }

    companion object {
        private var singletonInstance: OkHttpSingleton? = null
        val instance: OkHttpSingleton?
            get() {
                if (singletonInstance == null) {
                    singletonInstance = OkHttpSingleton()
                }
                return singletonInstance
            }
    }
}