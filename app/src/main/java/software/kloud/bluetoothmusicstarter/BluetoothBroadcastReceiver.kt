package software.kloud.bluetoothmusicstarter

import android.app.SearchManager
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.MediaStore
import android.support.v4.content.ContextCompat.startActivity
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import software.kloud.fridge_android.app.settings.SettingsManager

class BluetoothBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when {
            BluetoothDevice.ACTION_ACL_CONNECTED == intent?.action -> handleBluetoothConnect(context, intent)
            BluetoothDevice.ACTION_ACL_DISCONNECTED == intent?.action -> handleBluetoothDisconnect(context, intent)
        }
    }

    fun handleBluetoothConnect(context: Context?, intent: Intent?) {
        SettingsManager.loadSettings()
        val device = intent?.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
        if (device != null) {
            Log.d(javaClass.simpleName, "Bluetooth Device connected: " + device.name)
            // start music
            startPlaylist(context, SettingsManager.playlist)

            var speaker = SettingsManager.getSpeaker(device.address)
            if (speaker != null) {
                // we already know this bluetooth speaker

                // set music volume
                val audioService = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioService.setStreamVolume(AudioManager.STREAM_MUSIC, speaker.volume, AudioManager.FLAG_SHOW_UI)
                SettingsManager.connectedDevice = speaker
            } else {
                // unknown bluetooth speaker
                speaker = BluetoothSpeaker()
                speaker.name = device.name
                speaker.address = device.address

                // use current music volume for this speaker
                val audioService = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                speaker.volume = audioService.getStreamVolume(AudioManager.STREAM_MUSIC)

                // add speaker
                SettingsManager.connectedDevice = speaker
                SettingsManager.saveConnectedSpeaker()
            }
        }

    }

    fun handleBluetoothDisconnect(context: Context?, intent: Intent?) {
        SettingsManager.loadSettings()
        Log.d(javaClass.simpleName, "Bluetooth Device disconnected")
        SettingsManager.connectedDevice = null
    }

    /**
     * Starts the playlist with the given name. The playlist always starts with the first song. If
     * the playlist should be shuffled just add a silent dummy mp3 as first track.
     */
    fun startPlaylist(context: Context?, playlist: String) {
        val intent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)
        intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, "vnd.android.cursor.item/*")
        intent.putExtra(MediaStore.EXTRA_MEDIA_PLAYLIST, playlist)
        intent.putExtra(SearchManager.QUERY, playlist)
        startActivity(context, intent, null)
    }

}