package webfreak.si.remotecast

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.list

class Prefs (context: Context) {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun addChromecast(chromecast: CCModel) {
        val existingCCs = Json.parse(CCModel.serializer().list, prefs.getString(C.CHROMECASTS, "[]") ?: "[]")
        val serializedCCs = Json.stringify(CCModel.serializer().list, existingCCs.plus(chromecast).distinct())
        prefs.edit().putString(C.CHROMECASTS, serializedCCs).apply()
    }

    fun getChromecasts(): List<CCModel> {
        val existingCCs = prefs.getString(C.CHROMECASTS, "[]") ?: "[]"
        return Json.parse(CCModel.serializer().list, existingCCs)
    }
}