package br.dev.murilopereira.todo.ui.activity


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.dev.murilopereira.todo.BuildConfig
import br.dev.murilopereira.todo.R
import br.dev.murilopereira.todo.databinding.ActivityLoginBinding
import br.dev.murilopereira.todo.dto.UserDTO
import br.dev.murilopereira.todo.ui.dialog.LoadingDialog
import br.dev.murilopereira.todo.util.DialogSingleton
import br.dev.murilopereira.todo.util.OkHttpSingleton
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.soloader.SoLoader
import com.squareup.moshi.Moshi
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class Login : AppCompatActivity() {
    private val JSON = "application/json; charset=utf-8".toMediaType();

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater);
    }

    private var loadingDialog: LoadingDialog? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SoLoader.init(this, false)
        if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(this)) {
            val client = AndroidFlipperClient.getInstance(this)

            val networkFlipperPlugin = NetworkFlipperPlugin();
            val okClient = OkHttpClient.Builder()
                .addNetworkInterceptor(FlipperOkhttpInterceptor(networkFlipperPlugin))
                .build();

            OkHttpSingleton.instance?.setClient(okClient)

            client.addPlugin(networkFlipperPlugin);

            client.start()
        }

        binding.loginSignInButton.setOnClickListener{_ ->
            val email = binding.loginUserNameInput.text.toString();
            val password = binding.loginPasswordInput.text.toString()

            login(email, password)
        }

        loadingDialog = DialogSingleton.getLoadingDialog(this)

        setContentView(binding.root)

        DialogSingleton.getLoadingDialog(this).show()

        CoroutineScope(IO).launch {
            checkIsLogged()
            handleLoadingDialog(false)
        }
    }

    private suspend fun checkIsLogged() {
        val sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("access_token", "")
        val validUntil = sharedPreferences.getLong("valid_until", 0L)

        if(token!!.isNotEmpty()) {
            if(Date(validUntil ?: 0).after(Date())) {
                navigateToList()
            }
        }
    }

    private fun navigateToList() {
        val intent = Intent(this@Login, ActivityTaskList::class.java)
        startActivity(intent)
    }

    private fun showErrorDialog(message: String) {
        runOnUiThread {
            val alert = AlertDialog.Builder(this@Login)

            alert.setTitle(R.string.generic_login_error_title)
                .setMessage(message)
                .setPositiveButton(R.string.generic_login_error_ok, null)
                .show()
        }
    }

    private fun handleLoadingDialog(show: Boolean) {
        if(show)
            runOnUiThread { loadingDialog?.show() }
        else
            runOnUiThread { loadingDialog?.dismiss() }
    }

    private fun login(email: String, password: String) {
        handleLoadingDialog(true)
        val client = OkHttpSingleton.instance?.getClient()

        val user: UserDTO = UserDTO(email, password)
        val body = user.toJson().toRequestBody(JSON)

        val request = Request.Builder().url("https://spring.murilopereira.dev.br:8443/users/auth").post(body).build()
        client?.newCall(request)?.enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                val message = getString(R.string.generic_login_error_content)
                handleLoadingDialog(false)
                return showErrorDialog(message)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.i("[LOGIN]", responseBody.toString())

                var message = getString(R.string.generic_login_error_content)

                if(response.body == null || responseBody.toString().isEmpty()) {
                    handleLoadingDialog(false)
                    return showErrorDialog(message)
                }

                val moshi: Moshi = Moshi.Builder().build()
                val parsedResponse = moshi.adapter(Map::class.java).fromJson(
                    responseBody.toString()
                )

                Log.i("[LOGIN]", parsedResponse.toString())

                if(!response.isSuccessful) {
                    if (parsedResponse != null && parsedResponse["message"] != null)
                        message = parsedResponse["message"].toString()

                    handleLoadingDialog(false)
                    return showErrorDialog(message)
                }

                val tokenData = parsedResponse?.get("data") as Map<*, *>
                val sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("access_token", tokenData["token"].toString())
                editor.putLong("valid_until", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault())
                    .parse(tokenData["validUntil"].toString())?.time ?: 0)
                editor.apply()
                handleLoadingDialog(false)
                navigateToList()
            }
        })
    }
}