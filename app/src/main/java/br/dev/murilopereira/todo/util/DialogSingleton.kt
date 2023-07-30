package br.dev.murilopereira.todo.util

import android.content.Context
import androidx.appcompat.app.AlertDialog
import br.dev.murilopereira.todo.ui.dialog.LoadingDialog

class DialogSingleton {
    companion object {
        fun getLoadingDialog(context: Context): LoadingDialog {
            val loadingDialog = LoadingDialog(context)
            loadingDialog.setCancelable(false)
            loadingDialog.setCanceledOnTouchOutside(false)
            loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            return loadingDialog;
        }

        fun getErrorDialog(context: Context): AlertDialog.Builder {
            return AlertDialog.Builder(context)
        }
    }
}