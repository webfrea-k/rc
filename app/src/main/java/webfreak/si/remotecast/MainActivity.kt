package webfreak.si.remotecast

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import su.litvak.chromecast.api.v2.ChromeCasts
import su.litvak.chromecast.api.v2.MediaStatus


class MainActivity : AppCompatActivity() {
    private val job = Job()
    private val mainScope = CoroutineScope(Dispatchers.Default + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button2.setOnClickListener {
            togglePlaying()
        }

        val handler = Handler()
        val runnable = Runnable {
            Prefs(this@MainActivity).IP = ChromeCasts.get()?.first()?.address ?: ""
        }
        handler.postDelayed(runnable, 2000)

    }

    private fun togglePlaying() {
        mainScope.launch {
            val status = ChromeCasts.get()?.first()?.mediaStatus?.playerState
            if (status == MediaStatus.PlayerState.PLAYING) ChromeCasts.get()?.first()?.pause() else ChromeCasts.get()?.first()?.play()
        }
    }

    override fun onStart() {
        mainScope.launch {
            ChromeCasts.startDiscovery()

        }
        super.onStart()
    }

    override fun onStop() {
        mainScope.launch {
            ChromeCasts.stopDiscovery()
        }
        super.onStop()
    }
}
