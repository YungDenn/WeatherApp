package com.example.weatherapp

import android.app.AlertDialog
import android.content.Context

object DialogManager {
    fun locationSettingsDialog(context: Context, listener: Listener){
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle("Enable location ?")
        dialog.setMessage("Location disabled, do you want to enable location ?")

        // _,_ -> пропустить две переменные встроеного класса чтобы не пользоваться ими и рабоать
        // сразу со слушателем
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok"){_,_ ->
            listener.onClickSettings()
            dialog.dismiss()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel"){_,_ ->
            dialog.dismiss()
        }
        dialog.show()
    }

    interface Listener{
        fun onClickSettings()
    }
}