package webfreak.si.remotecast

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class Prefs (context: Context) {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var IP: String
        get() = prefs.getString("CHROMECAST_IP", "0") ?: ""
        set(value) = prefs.edit().putString("CHROMECAST_IP", value).apply()
}