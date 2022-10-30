package com.example.weatherapp

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText

object DialogManager {
    fun locationSettingsDialog(context: Context, listener: Listener){
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle("Enable location ?")
        dialog.setMessage("Location disabled, do you want to enable location ?")

        // _,_ -> пропустить две переменные встроеного класса чтобы не пользоваться ими и рабоать
        // сразу со слушателем
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok"){_,_ ->
            listener.onClickSettings(null)
            dialog.dismiss()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel"){_,_ ->
            dialog.dismiss()
        }
        dialog.show()
    }

    fun searchByNameDialog(context: Context, listener: Listener){
        val builder = AlertDialog.Builder(context)
        val edName = EditText(context)
        builder.setView(edName)
        val dialog = builder.create()
        dialog.setTitle("City name:")
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok"){_,_ ->
            listener.onClickSettings(edName.text.toString())
            dialog.dismiss()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel"){_,_ ->
            dialog.dismiss()
        }
        dialog.show()
    }

    interface Listener{
        fun onClickSettings(name: String?)
    }
}