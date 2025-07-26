package com.lfgit.fragments.dialogs

import com.lfgit.R

class PatternDialog : EnterTextDialog() {

    override fun handleText(text: String) {
        viewModel.handlePattern(text)
    }

    override fun getDialogLayoutID(): Int {
        return R.layout.pattern_dialog
    }

    companion object {
        fun newInstance(): PatternDialog {
            return PatternDialog()
        }
    }
}