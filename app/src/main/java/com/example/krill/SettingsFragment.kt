package com.example.krill

import android.os.Bundle
import android.widget.Switch
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        val notifications = findPreference("notifications") as SwitchPreferenceCompat?
        notifications?.setDefaultValue(true)
        val externalBrowser = findPreference("externalBrowser") as SwitchPreferenceCompat?
        externalBrowser?.setDefaultValue(false)
        val jsBrowser = findPreference("jsBrowser") as SwitchPreferenceCompat?
        jsBrowser?.setDefaultValue(true)
    }
}