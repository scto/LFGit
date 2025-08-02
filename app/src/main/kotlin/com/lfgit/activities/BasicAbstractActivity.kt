package com.lfgit.activities

import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Basic activity providing common methods for other activites.
 */
abstract class BasicAbstractActivity : AppCompatActivity() {
    private var mProgressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST) { // If request is cancelled, the result arrays are empty.
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                showToastMsg("Permissions refused, exiting..")
                finishAffinity()
            }
        }
    }

    fun checkAndRequestPermissions(permission: String) {
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, so request it from user
            ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSIONS_REQUEST)
        }
    }

    fun showToastMsg(msg: String) {
        runOnUiThread {
            val length = if (msg.length > 40) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            Toast.makeText(this@BasicAbstractActivity, msg, length).show()
        }
    }

    fun showOptionsDialog(
        title: Int,
        optionsResource: Int,
        option_listeners: Array<onOptionClicked>
    ) {
        val options_values = resources.getStringArray(optionsResource)
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setItems(options_values) { _, which -> option_listeners[which].onClicked() }
            .create()
            .show()
    }

    fun interface onOptionClicked {
        fun onClicked()
    }

    fun showProgressDialog() {
        mProgressDialog = ProgressDialog.show(this, "Executing", "Please wait a moment...")
    }

    fun hideProgressDialog() {
        mProgressDialog?.takeIf { it.isShowing }?.dismiss()
    }

    fun toggleProgressDialog(show: Boolean) {
        if (show) {
            showProgressDialog()
        } else {
            hideProgressDialog()
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST = 1
    }
}