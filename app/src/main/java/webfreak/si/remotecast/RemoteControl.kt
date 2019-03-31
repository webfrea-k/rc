package webfreak.si.remotecast

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import su.litvak.chromecast.api.v2.ChromeCast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import su.litvak.chromecast.api.v2.MediaStatus


/**
 * Implementation of App Widget functionality.
 */

enum class Control {
    SKIP_PREVIOUS,
    BACK_30,
    PLAY_PAUSE,
    FORWARD_30,
    SKIP_NEXT
}

class RemoteControl : AppWidgetProvider() {
    private val job = Job()
    private val mainScope = CoroutineScope(Dispatchers.Default + job)
    private val uiScope = CoroutineScope(Dispatchers.Main)

    private var cc: ChromeCast? = null

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        mainScope.launch {
            cc = ChromeCast(Prefs(context).IP)
        }
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }


    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == null && intent?.hasExtra("ID") == true) {
            when(intent.extras?.getInt("ID")) {
                0 -> updateRemotePlay(context!!, Control.SKIP_PREVIOUS)
                1 -> updateRemotePlay(context!!, Control.BACK_30)
                2 -> updateRemotePlay(context!!, Control.PLAY_PAUSE)
                3 -> updateRemotePlay(context!!, Control.FORWARD_30)
                4 -> updateRemotePlay(context!!, Control.SKIP_NEXT)
                else -> Log.d("INTENT", "NULL")
            }

        } else {
            super.onReceive(context, intent)
        }
    }

    private fun updateRemotePlay(context: Context? ,control: Control) {
        if(context == null) { return }

        mainScope.launch {
            cc = ChromeCast(Prefs(context).IP)
            val mediaStatus = cc?.mediaStatus
            val duration = mediaStatus?.media?.duration ?: 0.0
            val currentTime = mediaStatus?.currentTime ?: 0.0
            when (control) {
                Control.SKIP_PREVIOUS -> {
                    cc?.seek(0.0)
                    showToast(context, "From beginning")
                }
                Control.BACK_30 -> {
                    cc?.seek(if(currentTime > 30) currentTime - 30 else 0.0)
                    showToast(context, "-30s")
                }
                Control.PLAY_PAUSE -> {
                    if(mediaStatus?.playerState == MediaStatus.PlayerState.PLAYING) cc?.pause() else cc?.play()
                    showToast(context,  "Toggle Play/Pause")
                }
                Control.FORWARD_30 -> {
                    cc?.seek(if((currentTime + 30) < duration) currentTime + 30 else duration)
                    showToast(context, "+30s")
                }
                Control.SKIP_NEXT -> {
                    cc?.seek(duration)
                    showToast(context, "To end")
                }
            }
        }
    }

    private fun showToast(context: Context?, message: String) {
        uiScope.launch {
            if(context != null)
            Toast.makeText(context, message,Toast.LENGTH_SHORT).show()
        }
    }

    private fun reconnectChromecastIfNeeded(context: Context?) {
        if(cc == null && context != null) {
            cc = ChromeCast(Prefs(context).IP)
        }
    }

    override fun onEnabled(context: Context) {
        mainScope.launch {
            if(cc == null) {
                cc = ChromeCast(Prefs(context).IP)
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

