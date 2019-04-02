package webfreak.si.remotecast

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import su.litvak.chromecast.api.v2.ChromeCast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import su.litvak.chromecast.api.v2.MediaStatus
import webfreak.si.remotecast.Utils.Companion.showToast
import webfreak.si.remotecast.Utils.Companion.updateRemotePlay


/**
 * Implementation of App Widget functionality.
 */

class RemoteControl : AppWidgetProvider() {
    private val job = Job()
    private val mainScope = CoroutineScope(Dispatchers.Default + job)
    private val uiScope = CoroutineScope(Dispatchers.Main)

    private var cc: ChromeCast? = null

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        mainScope.launch {
            cc = ChromeCast(Prefs(context).getChromecasts().firstOrNull()?.address)
        }
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }


    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == null && intent?.hasExtra("ID") == true) {
            cc = ChromeCast(Prefs(context!!).getChromecasts().firstOrNull()?.address)

            when(intent.extras?.getInt("ID")) {
                0 -> updateRemotePlay(context!!, cc, Control.SKIP_PREVIOUS)
                1 -> updateRemotePlay(context!!, cc,Control.BACK_30)
                2 -> updateRemotePlay(context!!, cc,Control.PLAY_PAUSE)
                3 -> updateRemotePlay(context!!, cc,Control.FORWARD_30)
                4 -> updateRemotePlay(context!!, cc,Control.SKIP_NEXT)
                else -> Log.d("INTENT", "NULL")
            }

        } else {
            super.onReceive(context, intent)
        }
    }

    private fun reconnectChromecastIfNeeded(context: Context?) {
        if(cc == null && context != null) {
            cc = ChromeCast(Prefs(context).getChromecasts().firstOrNull()?.address)
        }
    }

    override fun onEnabled(context: Context) {
        mainScope.launch {
            if(cc == null) {
                cc = ChromeCast(Prefs(context).getChromecasts().firstOrNull()?.address)
            }
        }
    }

    override fun onDisabled(context: Context) {
        mainScope.launch {
            cc?.disconnect()
            cc = null
        }
    }

    companion object {
        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {

            val views = RemoteViews(context.packageName, R.layout.remote_control)

            views.setImageViewResource(R.id.skip_previous, R.drawable.skip_previous)
            addIntent(context, 0, views, R.id.skip_previous)

            views.setImageViewResource(R.id.rewind_30, R.drawable.rewind_30)
            addIntent(context, 1, views, R.id.rewind_30)

            views.setImageViewResource(R.id.play_pause, R.drawable.play_pause)
            addIntent(context, 2, views, R.id.play_pause)

            views.setImageViewResource(R.id.forward_30, R.drawable.fast_forward_30)
            addIntent(context, 3, views, R.id.forward_30)

            views.setImageViewResource(R.id.skip_next, R.drawable.skip_next)
            addIntent(context, 4, views, R.id.skip_next)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun addIntent(context: Context, value: Int, view: RemoteViews, buttonId: Int) {
            val intent = Intent(context, RemoteControl::class.java)
            intent.putExtra("ID", value)
            val pendingIntent = PendingIntent.getBroadcast(context, value, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            view.setOnClickPendingIntent(buttonId, pendingIntent)

        }
    }
}

