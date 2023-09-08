package com.example.qrcode

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import java.util.Locale

class DialogBox : DialogFragment(), TextToSpeech.OnInitListener {

    private var amount: Double = 0.0
    private lateinit var textToSpeechMssg: String
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.activity_dialog_box, null)
        builder.setView(dialogView)

        val amountTextView = dialogView.findViewById<TextView>(R.id.amountPaid)
        val messageTextView = dialogView.findViewById<TextView>(R.id.message)

        amountTextView.text = amount.toString()
        messageTextView.text = textToSpeechMssg
        textToSpeech = TextToSpeech(requireContext(), this)

        val okayButton = dialogView.findViewById<Button>(R.id.btnOk)
        okayButton.setOnClickListener {
            dismiss()
        }
        return builder.create()
    }

    override fun onInit(status: Int) {
                    if (status == TextToSpeech.SUCCESS) {
                        val result = textToSpeech.setLanguage(Locale.US)

                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Toast.makeText(context, "language error", Toast.LENGTH_SHORT).show()
                        } else {
                            textToSpeech.speak(textToSpeechMssg, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        } else {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.stop()
        textToSpeech.shutdown()
    }

    companion object {
        fun newInstance( amount: Double): DialogBox {
            val dialogBox = DialogBox()
            dialogBox.amount = amount
            return dialogBox
        }
    }

    fun dialogMessage(message: String) {
        textToSpeechMssg = message

    }

}
