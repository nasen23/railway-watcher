package com.nasen.railwaywatcher.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.afollestad.vvalidator.form
import com.afollestad.vvalidator.form.Form
import com.afollestad.vvalidator.form.FormResult
import com.nasen.railwaywatcher.R

class NewSubRangeDialogFragment : DialogFragment() {
    internal lateinit var listener: Listener
    lateinit var posForm: Form
    lateinit var newPosKm: EditText
    lateinit var newPosM: EditText

    interface Listener {
        fun onDialogPositiveClick(dialog: DialogFragment, result: FormResult)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_new_subrange, null)
            newPosKm = view.findViewById(R.id.newPosKm)
            newPosM = view.findViewById(R.id.newPosM)
            posForm = form {
                useRealTimeValidation()
                input(newPosKm) {
                    isNumber().atLeast(0)
                }
                input(newPosM) {
                    isNumber().atLeast(0).lessThan(1000)
                }
            }
            builder.setView(view)
                .setPositiveButton(R.string.confirm) { dialog, _ ->
                    val result = posForm.validate()
                    if (result.success()) {
                        listener.onDialogPositiveClick(this, result)
                        dialog.dismiss()
                    }
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        try {
            listener = targetFragment as Listener
        } catch (e: ClassCastException) {
            throw ClassCastException("$targetFragment must implement Listener")
        }
    }

}