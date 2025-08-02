package com.lfgit.fragments.dialogs

import com.lfgit.R

class CheckoutDialog : EnterTextDialog() {

    override fun handleText(text: String) {
        viewModel.handleCheckoutBranch(text)
    }

    override fun getDialogLayoutID(): Int {
        return R.layout.checkout_dialog
    }

    companion object {
        fun newInstance(): CheckoutDialog {
            return CheckoutDialog()
        }
    }
}