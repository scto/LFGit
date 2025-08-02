package com.lfgit.fragments.dialogs

import com.lfgit.R

class RemoteDialog : EnterTextDialog() {

    override fun handleText(text: String) {
        viewModel.handleRemoteURL(text)
    }

    override fun getDialogLayoutID(): Int {
        return R.layout.remote_dialog
    }

    companion object {
        fun newInstance(): RemoteDialog {
            return RemoteDialog()
        }
    }
}