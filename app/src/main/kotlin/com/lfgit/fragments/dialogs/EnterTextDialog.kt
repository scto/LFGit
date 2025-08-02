package com.lfgit.fragments.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.lfgit.R
import com.lfgit.view_models.RepoTasksViewModel

abstract class EnterTextDialog : DialogFragment() {
    protected lateinit var viewModel: RepoTasksViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(getDialogLayoutID(), null, false)

        viewModel = ViewModelProvider(requireActivity()).get(RepoTasksViewModel::class.java)

        val builder = AlertDialog.Builder(activity)
        builder.setView(view)

        val enterButton = view.findViewById<Button>(R.id.enterButton)
        enterButton.setOnClickListener {
            val editText = view.findViewById<EditText>(R.id.editText)
            val text = editText.text.toString()
            handleText(text)
            dismiss() // Dialog nach Eingabe schließen
        }

        return builder.create()
    }

    override fun onCancel(dialog: DialogInterface) {
        viewModel.startState()
        super.onCancel(dialog)
    }

    /** Overwrite to handle EditText text */
    abstract fun handleText(text: String)

    /** Overwrite to set a dialog layout */
    abstract fun getDialogLayoutID(): Int
}