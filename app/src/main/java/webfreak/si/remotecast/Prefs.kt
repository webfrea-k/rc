package webfreak.si.remotecast

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class Prefs (context: Context) {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun addChromecast(pair: Pair<String, String>) {
        val ccPair = "${pair.first}@${pair.second}|"
        val existingCCs = prefs.getString(C.CHROMECASTS, "")?.replace(ccPair.dropLast(1),"")
        prefs.edit().putString(C.CHROMECASTS, "$existingCCs$ccPair".dropLast(1)).apply()
    }

    fun getChromecasts(): List<Pair<String, String>> {
        val ccList = mutableListOf<Pair<String,String>>()
        val ccJoined = prefs.getString(C.CHROMECASTS, "") ?: ""
        val ccPairs = ccJoined.split("|")
        for (pair in ccPairs) {
            val pair = pair.split("@")
            ccList.add(Pair(pair.first(),pair.last()))
        }
        return ccList
    }
}