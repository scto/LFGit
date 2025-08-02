package com.lfgit.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.lfgit.R
import com.lfgit.fragments.SettingsFragment

/** An activity providing preference settings. */
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity) //

        // Das SettingsFragment in den FrameLayout-Container einfügen.
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment()) //
                .commit() //
        }
    }
}