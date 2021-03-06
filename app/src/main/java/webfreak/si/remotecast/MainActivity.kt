package webfreak.si.remotecast

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.remote_control.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import su.litvak.chromecast.api.v2.ChromeCasts
import su.litvak.chromecast.api.v2.MediaStatus
import android.widget.ArrayAdapter
import su.litvak.chromecast.api.v2.ChromeCast
import webfreak.si.remotecast.Utils.Companion.updateRemotePlay


class MainActivity : AppCompatActivity() {
    private val job = Job()
    private val mainScope = CoroutineScope(Dispatchers.Default + job)
    var currentCCs: List<ChromeCast>? = null
    var currentCC: ChromeCast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        skip_previous.setColorFilter(Color.argb(255, 0,0,0))
        skip_previous.setOnClickListener {
            updateRemotePlay(this@MainActivity, currentCC, Control.SKIP_PREVIOUS)
        }
        rewind_30.setColorFilter(Color.argb(255, 0,0,0))
        rewind_30.setOnClickListener {
            updateRemotePlay(this@MainActivity, currentCC, Control.BACK_30)
        }
        play_pause.setColorFilter(Color.argb(255, 0,0,0))
        play_pause.setOnClickListener {
            updateRemotePlay(this@MainActivity, currentCC, Control.PLAY_PAUSE)
        }
        forward_30.setColorFilter(Color.argb(255, 0,0,0))
        forward_30.setOnClickListener {
            updateRemotePlay(this@MainActivity, currentCC, Control.FORWARD_30)
        }
        skip_next.setColorFilter(Color.argb(255, 0,0,0))
        skip_next.setOnClickListener {
            updateRemotePlay(this@MainActivity, currentCC, Control.SKIP_NEXT)
        }

        play_pause.setOnClickListener {
            togglePlaying()
        }

        val handler = Handler()
        val runnable = Runnable {
            currentCCs = ChromeCasts.get()
            currentCCs?.map { "${it.title} - ${it.model}" }?.toTypedArray()?.let {
                val ccAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, it)
                ccAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = ccAdapter
            }

        }
        handler.postDelayed(runnable, 2000)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentCCs?.get(position)?.let {
                    Prefs(this@MainActivity).addChromecast(CCModel(it.title, it.address))
                    currentCC = it
                }
            }
        }

        youtube.setOnClickListener {
            startApp("233637DE")
        }
        plex.setOnClickListener {
            startApp("9AC194DC")
        }
    }

    private fun startApp(id: String) {
        mainScope.launch {
            val status = currentCC?.status
            if (currentCC?.isAppAvailable(id) == true && status?.isAppRunning(id) == false) {
                currentCC?.launchApp(id)
            }
        }
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
