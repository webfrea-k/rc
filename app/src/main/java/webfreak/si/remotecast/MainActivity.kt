package webfreak.si.remotecast

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.mediarouter.media.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var mediaRouter: MediaRouter? = null
    private var mSelector: MediaRouteSelector? = null

    // Variables to hold the currently selected route and its playback client
    private var mRoute: MediaRouter.RouteInfo? = null
    private var remotePlaybackClient: RemotePlaybackClient? = null
    private val TAG = "MainActivity"
    private var playing = true
    // Define the Callback object and its methods, save the object in a class variable
    private val mediaRouterCallback = object : MediaRouter.Callback() {
        override fun onRouteChanged(router: MediaRouter?, route: MediaRouter.RouteInfo?) {
            super.onRouteChanged(router, route)
            mRoute = route
            remotePlaybackClient = RemotePlaybackClient(this@MainActivity, mRoute)
            remotePlaybackClient!!.startSession(Bundle.EMPTY, sessionCallback)
            Log.d(TAG, "ROUTE CHANGED")
        }
    }
    private val resumeCallback = object : RemotePlaybackClient.SessionActionCallback() {
        override fun onResult(data: Bundle?, sessionId: String?, sessionStatus: MediaSessionStatus?) {
            super.onResult(data, sessionId, sessionStatus)
            Log.d(TAG, "RESULT")
        }
    }

    private val pauseCallback = object : RemotePlaybackClient.SessionActionCallback() {
        override fun onResult(data: Bundle?, sessionId: String?, sessionStatus: MediaSessionStatus?) {
            super.onResult(data, sessionId, sessionStatus)
            Log.d(TAG, "RESULT")
        }
    }

    private val sessionCallback = object : RemotePlaybackClient.SessionActionCallback() {
        override fun onResult(data: Bundle?, sessionId: String?, sessionStatus: MediaSessionStatus?) {
            super.onResult(data, sessionId, sessionStatus)
            Log.d(TAG, "RESULT")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaRouter = MediaRouter.getInstance(this)
        mSelector = MediaRouteSelector.Builder()
            // These are the framework-supported intents
            .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
            .build()

        button2.setOnClickListener {
            if (playing) {
                remotePlaybackClient?.pause(Bundle.EMPTY, pauseCallback)
            } else {
                remotePlaybackClient?.resume(Bundle.EMPTY, resumeCallback)
            }
        }
    }
    override fun onStart() {
        mSelector?.also { selector ->
            mediaRouter?.addCallback(selector, mediaRouterCallback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY)
        }
        super.onStart()
    }

    override fun onStop() {
        mediaRouter?.removeCallback(mediaRouterCallback)
        mRoute?.also {
            remotePlaybackClient?.release()
            remotePlaybackClient = null
        }
        super.onStop()
    }
}
