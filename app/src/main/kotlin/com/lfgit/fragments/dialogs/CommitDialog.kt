package com.lfgit.fragments.dialogs

import com.lfgit.R

class CommitDialog : EnterTextDialog() {

    override fun handleText(text: String) {
        viewModel.handleCommitMsg(text)
    }

    override fun getDialogLayoutID(): Int {
        return R.layout.commit_dialog
    }

    companion object {
        fun newInstance(): CommitDialog {
            return CommitDialog()
        }
    }
}