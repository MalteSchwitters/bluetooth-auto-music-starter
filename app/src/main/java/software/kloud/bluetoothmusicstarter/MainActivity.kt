package software.kloud.bluetoothmusicstarter

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import kotlinx.android.synthetic.main.activity_main.*
import software.kloud.fridge_android.app.settings.SettingsManager
import android.media.AudioManager
import android.database.ContentObserver
import android.os.Handler
import android.provider.Settings
import android.util.Log
import java.util.*


class MainActivity : AppCompatActivity(), Observer {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // init settings
        SettingsManager.init(getSharedPreferences("application_properties", 0))
        SettingsManager.loadSettings()

        // build view
        setContentView(R.layout.activity_main)

        // register actions
        txtPlaylist.setOnEditorActionListener { _, _, _ -> updatePlaylist()}

        // audio volume observer
        val settingsContentObserver = SettingsContentObserver(applicationContext, Handler())
        applicationContext.contentResolver.registerContentObserver(
                Settings.System.CONTENT_URI, true,
                settingsContentObserver)
    }


    override fun onStart() {
        SettingsManager.loadSettings()
        SettingsManager.addObserver(this)
        fillUi()
        super.onStart()
    }

    override fun onStop() {
        SettingsManager.deleteObservers()
        SettingsManager.saveSettings()
        super.onStop()
    }

    override fun update(o: Observable?, arg: Any?) {
        if (arg == SettingsManager.ACTION_ADD_SPEAKER) {
            addSpeakerFragment(SettingsManager.connectedDevice!!)
        }
    }

    private fun updatePlaylist(): Boolean {
        SettingsManager.playlist = txtPlaylist.text.toString()
        return false
    }

    private fun fillUi() {
        txtPlaylist.setText(SettingsManager.playlist)
        lstDevices.removeAllViews()
        for (speaker in SettingsManager.devices) {
            addSpeakerFragment(speaker)
        }
    }

    private fun addSpeakerFragment(speaker: BluetoothSpeaker) {
        val fragTransaction = fragmentManager.beginTransaction()
        val fragment = BluetoothDeviceFragment.newInstance(speaker.address!!)
        fragTransaction.add(lstDevices.id, fragment, "Fragment " + speaker.address)
        fragTransaction.commitAllowingStateLoss()

    }

    inner class SettingsContentObserver(internal var context: Context, handler: Handler) : ContentObserver(handler) {

        var lastVolume = -1

        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            Log.d(javaClass.simpleName, "Sound Volume changed")
            val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
            if (SettingsManager.connectedDevice != null && currentVolume != lastVolume) {
                SettingsManager.updateSpeakerVolume(SettingsManager.connectedDevice!!.address!!, currentVolume)
                lastVolume = currentVolume
            }
        }
    }
}
