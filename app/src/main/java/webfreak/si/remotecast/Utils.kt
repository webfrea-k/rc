package webfreak.si.remotecast

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import su.litvak.chromecast.api.v2.ChromeCast
import su.litvak.chromecast.api.v2.MediaStatus

class Utils{
    companion object {
        private val uiScope = CoroutineScope(Dispatchers.Main)
        private val job = Job()
        private val mainScope = CoroutineScope(Dispatchers.Default + job)

        fun showToast(context: Context?, message: String) {
            uiScope.launch {
                if(context != null)
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        fun updateRemotePlay(context: Context?, currentCC: ChromeCast?, control: Control) {

            mainScope.launch {
                val mediaStatus = currentCC?.mediaStatus
                val duration = mediaStatus?.media?.duration ?: 0.0
                val currentTime = mediaStatus?.currentTime ?: 0.0
                when (control) {
                    Control.SKIP_PREVIOUS -> {
                        currentCC?.seek(0.0)
                        showToast(context, "From beginning")
                    }
                    Control.BACK_30 -> {
                        currentCC?.seek(if(currentTime > 30) currentTime - 30 else 0.0)
                        showToast(context, "-30s")
                    }
                    Control.PLAY_PAUSE -> {
                        if(mediaStatus?.playerState == MediaStatus.PlayerState.PLAYING) currentCC?.pause() else currentCC?.play()
                        showToast(context,  "Toggle Play/Pause")
                    }
                    Control.FORWARD_30 -> {
                        currentCC?.seek(if((currentTime + 30) < duration) currentTime + 30 else duration)
                        showToast(context, "+30s")
                    }
                    Control.SKIP_NEXT -> {
                        currentCC?.seek(duration)
                        showToast(context, "To end")
                    }
                }
            }
        }
    }
}

enum class Control {
    SKIP_PREVIOUS,
    BACK_30,
    PLAY_PAUSE,
    FORWARD_30,
    SKIP_NEXT
}