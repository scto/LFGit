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

class CredentialsDialog : DialogFragment() {
    private lateinit var viewModel: RepoTasksViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.credentials_dialog, null, false)

        viewModel = ViewModelProvider(activity).get(RepoTasksViewModel::class.java)

        val usernameEditText = view.findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = view.findViewById<EditText>(R.id.passwordEditText)
        val enterButton = view.findViewById<Button>(R.id.enterButton)

        val alertDialogBuilder = AlertDialog.Builder(requireActivity())
        alertDialogBuilder.setView(view)

        enterButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            viewModel.handleCredentials(username, password)
            dismiss()
        }

        return alertDialogBuilder.create()
    }

    override fun onCancel(dialog: DialogInterface) {
        viewModel.startState()
        super.onCancel(dialog)
    }

    companion object {
        fun newInstance(): CredentialsDialog {
            return CredentialsDialog()
        }
    }
}